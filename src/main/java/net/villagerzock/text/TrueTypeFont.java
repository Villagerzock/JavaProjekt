package net.villagerzock.text;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class TrueTypeFont {

    public static final int ATLAS_WIDTH  = 1024;   // ruhig etwas größer
    public static final int ATLAS_HEIGHT = 1024;
    public static final int FIRST_CHAR   = 32;     // ' '
    public static final int CHAR_COUNT   = 96;     // 32..127

    @Getter
    private final int textureId;
    private final STBTTBakedChar.Buffer charData;
    private final float pixelHeight;
    @Getter
    private final String name;
    @Getter
    @Setter
    private int layer;

    /**
     * @param is z.B. "/fonts/roboto.ttf" (muss im resources-Ordner liegen)
     * @param pixelHeight  Zielgröße in Pixel (z.B. 32.0f)
     */
    public TrueTypeFont(InputStream is, float pixelHeight, String fontName) {
        this.pixelHeight = pixelHeight;
        this.name = fontName;

        // 1) TTF aus dem Classpath lesen
        byte[] ttfBytes = readResourceBytes(is);
        ByteBuffer ttf = BufferUtils.createByteBuffer(ttfBytes.length);
        ttf.put(ttfBytes).flip();

        // 2) Bitmap für den Font-Atlas
        ByteBuffer bitmap = BufferUtils.createByteBuffer(ATLAS_WIDTH * ATLAS_HEIGHT);

        charData = STBTTBakedChar.malloc(CHAR_COUNT);

        // → hier passiert die TrueType-Rasterisierung
        int res = STBTruetype.stbtt_BakeFontBitmap(
                ttf,
                pixelHeight,
                bitmap,
                ATLAS_WIDTH,
                ATLAS_HEIGHT,
                FIRST_CHAR,
                charData
        );
        if (res <= 0) {
            throw new RuntimeException("stbtt_BakeFontBitmap failed: " + res);
        }

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        saveAtlasAsPNG(bitmap.duplicate(),ATLAS_WIDTH,ATLAS_HEIGHT,"font.png");


        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RED,
                ATLAS_WIDTH,
                ATLAS_HEIGHT,
                0,
                GL_RED,
                GL_UNSIGNED_BYTE,
                bitmap
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);
        this.textureId = texture;


    }

    private byte[] readResourceBytes(InputStream in) {
        if (in == null) {
            throw new RuntimeException("Font resource not found");
        }
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public STBTTBakedChar.Buffer charData() {
        return charData;
    }

    public float pixelHeight() {
        return pixelHeight;
    }

    private static void saveAtlasAsPNG(ByteBuffer bitmap, int width, int height, String path) {
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            // ByteBuffer → BufferedImage
            // ByteBuffer ist nicht garantiert array-backed → wir lesen Pixel für Pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int value = bitmap.get(y * width + x) & 0xFF;

                    int rgb = (value << 16) | (value << 8) | value; // grau → RGB
                    img.setRGB(x, y, rgb);
                }
            }

            File output = new File(path);
            ImageIO.write(img, "png", output);
            System.out.println("Saved font atlas PNG to " + output.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}