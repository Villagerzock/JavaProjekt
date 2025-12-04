package net.villagerzock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.villagerzock.text.TrueTypeFont;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.lwjgl.opengl.GL33C.*;

public class TextureLoader {
    public record TexturePart(int width,int height,int u,int v,int layer, int tw, int th) {
    }
    public static class TextureArray {
        public final int textureId;
        public final Map<String, TexturePart> layerMap;
        public final int width;
        public final int height;

        public TextureArray(int textureId, Map<String, TexturePart> layerMap, int width, int height) {
            this.textureId = textureId;
            this.layerMap = layerMap;
            this.width = width;
            this.height = height;
            System.out.println("Finished Generating TextureArray, All Textures loaded: " + Arrays.toString(layerMap.keySet().toArray(String[]::new)));
        }
        public TexturePart getPart(String path){
            TexturePart texturePart = layerMap.get(path);
            if (texturePart == null) {
                throw new IllegalArgumentException("No layer for path: " + path + "\nPossible Layers: " + Arrays.toString(layerMap.keySet().toArray(String[]::new)));
            }
            return texturePart;
        }
        public int getLayer(String path) {
            Integer idx = layerMap.get(path).layer;
            if (idx == null) {
                throw new IllegalArgumentException("No layer for path: " + path + "\nPossible Layers: " + Arrays.toString(layerMap.keySet().toArray(String[]::new)));
            }
            return idx;
        }
    }

    public static TextureArray loadFromJarTextures(List<String> paths) throws IOException {
        // 1) Alle Textur-Pfade aus der JAR ermitteln
        List<String> texturePaths = paths;


        STBImage.stbi_set_flip_vertically_on_load(true);

        int width = -1;
        int height = -1;
        int layers = texturePaths.size();

        ByteBuffer[] pixelData = new ByteBuffer[layers];
        Map<String, TexturePart> layerMap = new HashMap<>();


        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // 2) Alle Texturen dekodieren
            for (int i = 0; i < layers; i++) {
                String path = texturePaths.get(i);

                ByteBuffer fileBytes;
                try (InputStream is = Main.class.getResourceAsStream("/" + path)) {
                    byte[] bytes = is.readAllBytes();
                    fileBytes = MemoryUtil.memAlloc(bytes.length);
                    fileBytes.put(bytes).flip();
                }

                ByteBuffer img = STBImage.stbi_load_from_memory(fileBytes, w, h, comp, 4);
                MemoryUtil.memFree(fileBytes);

                if (img == null) {
                    // cleanup
                    for (int j = 0; j < i; j++) {
                        STBImage.stbi_image_free(pixelData[j]);
                    }
                    throw new RuntimeException("Failed to load image " + path + " : " + STBImage.stbi_failure_reason());
                }

                if (i == 0) {
                    width = w.get(0);
                    height = h.get(0);
                } else {
                    if (w.get(0) != width || h.get(0) != height) {
                        // cleanup
                        STBImage.stbi_image_free(img);
                        for (int j = 0; j < i; j++) {
                            STBImage.stbi_image_free(pixelData[j]);
                        }
                        throw new IllegalArgumentException("All textures in a GL_TEXTURE_2D_ARRAY must have same size");
                    }
                }

                pixelData[i] = img;

                try (InputStream subTextures = Main.class.getResourceAsStream("/" + path + ".json")) {
                    if (subTextures != null){
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(new InputStreamReader(subTextures), JsonObject.class);
                        int tileSize = jsonObject.get("tile_size").getAsInt();
                        JsonObject values = jsonObject.getAsJsonObject("values");
                        for (String key : values.keySet()) {
                            JsonArray array = values.getAsJsonArray(key);
                            layerMap.put(path + "->" + key, new TexturePart(array.get(2).getAsInt() * tileSize,array.get(3).getAsInt() * tileSize, array.get(0).getAsInt() * tileSize, array.get(1).getAsInt() * tileSize,i,width, height));
                        }
                    }
                }

                layerMap.put(path, new TexturePart(width,height,0,0,i,width,height)); // Map: Pfad -> Layerindex
            }

            // 3) GL_TEXTURE_2D_ARRAY erstellen
            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D_ARRAY, texId);

            glTexImage3D(
                    GL_TEXTURE_2D_ARRAY,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    layers,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    (ByteBuffer) null
            );

            // 4) Alle Layers hochladen
            for (int layer = 0; layer < layers; layer++) {
                glTexSubImage3D(
                        GL_TEXTURE_2D_ARRAY,
                        0,
                        0, 0, layer,
                        width, height, 1,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        pixelData[layer]
                );
            }

            // 5) Filter/Wrapping + Mipmaps
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glGenerateMipmap(GL_TEXTURE_2D_ARRAY);

            glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

            // 6) STB Buffers freigeben
            for (int i = 0; i < layers; i++) {
                STBImage.stbi_image_free(pixelData[i]);
            }

            return new TextureArray(texId, layerMap, width, height);
        }
    }
}
