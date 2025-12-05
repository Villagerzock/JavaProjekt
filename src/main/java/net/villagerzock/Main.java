package net.villagerzock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.villagerzock.input.KeyBindingManager;
import net.villagerzock.input.KeyBinds;
import net.villagerzock.text.Text;
import net.villagerzock.text.TextRenderer;
import net.villagerzock.text.TrueTypeFont;
import net.villagerzock.tileWorld.Tile;
import net.villagerzock.tileWorld.TileWorld;
import net.villagerzock.tileWorld.TileWorldLoadable;
import net.villagerzock.ui.screens.EditorScreen;
import net.villagerzock.ui.Screen;
import net.villagerzock.ui.screens.StartScreen;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Math;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarFile;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static List<Runnable> executeOnNextRenderThreadCall = new ArrayList<>();
    private static ICamera camera;
    private static Camera editorCamera;
    private static Camera overworldCamera;
    private static Camera underworldCamera;
    public static int fragBasic;
    public static int invertedBasic;
    public static int uiBasic;
    private static int testBasic;

    @Getter
    private static TileWorld currentLevel;
    @Getter
    private static TileWorldLoadable[] loadables;
    static {
        try {
            String jsonText = new String(Resources.readFile("res://levels.json").readAllBytes());
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(jsonText, JsonArray.class);
            loadables = new TileWorldLoadable[array.size()];
            for (int i = 0; i<array.size();i++) {
                JsonElement jsonElement = array.get(i);
                JsonObject obj = jsonElement.getAsJsonObject();
                TileWorldLoadable tileWorldLoadable = new TileWorldLoadable(Text.fromJson(obj.get("name")),obj.get("file").getAsString());
                loadables[i] = tileWorldLoadable;
            }
            System.out.println(jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static void loadLevel(TileWorldLoadable loadable){
        TileWorld tileWorld = new TileWorld();
        Gson gson = new Gson();
        InputStreamReader reader = new InputStreamReader(Resources.readFile(loadable.filePath()));
        try {
            tileWorld.load(gson,reader);
            Main.currentLevel = tileWorld;
            Main.setScreen(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Vector2i lastSize = new Vector2i();
    private static boolean paused = true;


    public static Vector2i getSize() {
        return lastSize;
    }

    @Getter
    private static Screen currentScreen;
    private static long handle;

    public static void setScreen(Screen currentScreen) {
        Main.currentScreen = currentScreen;
        if (currentScreen != null) {
            currentScreen.clearAndInit(lastSize.x, lastSize.y);
        }
    }
    private static boolean drawingUnderworld;
    @Getter
    private static TextureLoader.TextureArray textures;
    private static String[] args;
    public static int run(String[] args) {
        Main.args = args;

        System.out.println(String.format("Amount of Loadables: %d\nLoadables: %s", loadables.length, Arrays.toString(loadables)));

        Thread renderThread = new Thread(() -> {
            if (!GLFW.glfwInit()) {
                System.out.println("Couldn't initialize GLFW");
                System.exit(-1);
            }

            handle = GLFW.glfwCreateWindow(800,600,"Test",0,0);
            editorCamera = new Camera(new Vector2f(),new Vector2i(800,600), new AtomicFloat(1));
            overworldCamera = new Camera(new Vector2f(),new Vector2i(800,600), new AtomicFloat(1));
            underworldCamera = new Camera(new Vector2f(),new Vector2i(800,600), new AtomicFloat(1));
            camera = new SwapCamera(editorCamera, overworldCamera,underworldCamera){
                @Override
                public int getCamera() {
                    return Main.currentScreen instanceof EditorScreen ? 0 : drawingUnderworld ? 2 : 1;
                }
            };
            GLFW.glfwMakeContextCurrent(handle);
            GL.createCapabilities();

            loadShaders();
            loadTextures();

            setUniformSampler2DArray(uiBasic,"textures",textures);
            setUniformVec4(uiBasic,"shaderColor", new Vector4f(1f,1f,1f,1f));
            setUniformSampler2DArray(fragBasic,"textures",textures);
            setUniformVec4(fragBasic,"shaderColor", new Vector4f(1f,1f,1f,1f));
            setUniformSampler2DArray(invertedBasic,"textures",textures);
            setUniformVec4(invertedBasic,"shaderColor", new Vector4f(1f,1f,1f,1f));
            setUniformSampler2DArray(testBasic,"textures",textures);
            setUniformVec4(testBasic,"shaderColor", new Vector4f(1f,1f,1f,1f));

            double lastX = -1;
            double lastY = -1;

            int px = 0;
            glfwSetScrollCallback(handle, (l, v, v1) -> {
                double scroll = v1;

                float zoom = camera.sizeMultiplier().getValue();
                float stepFactor = 1.1f;

                zoom *= (float) Math.pow(stepFactor, -scroll);

                zoom = Math.max(0.1f, Math.min(10f, zoom));

                if (currentScreen instanceof EditorScreen) {
                    editorCamera.sizeMultiplier().setValue(zoom);
                }

                if (currentScreen != null) {
                    Vector2d mousePos = getMousePosition();
                    currentScreen.mouseScrolled(mousePos.x, mousePos.y, v1);
                }
                if (currentScreen == null || currentScreen.canMove()){
                    KeyBindingManager.MouseScrolled((int) scroll);
                }

            });
            glfwSetKeyCallback(handle, (l, key, scancode, action, mods) -> {
                if (currentScreen != null) {
                    if (action == GLFW_RELEASE) {
                        currentScreen.keyReleased(key, scancode, mods);
                    }else if (action == GLFW_PRESS) {
                        currentScreen.keyPressed(key, scancode, mods);
                    }
                }else {
                    if (action == GLFW_PRESS) {
                        if (key == GLFW_KEY_ESCAPE)
                            setScreen(new StartScreen(Text.literal("Test")));
                        if (key == GLFW_KEY_0){
                            setScreen(new EditorScreen(Text.literal("Editor")));
                        }
                    }
                }

                if (action == GLFW_PRESS) {
                    if (currentScreen == null || currentScreen.canMove()){
                        KeyBindingManager.KeyPressed(key);
                    }
                }else if (action == GLFW_RELEASE) {
                    if (currentScreen == null || currentScreen.canMove()){
                        KeyBindingManager.KeyReleased(key);
                    }
                }else if (action == GLFW_REPEAT){
                    if (currentScreen == null || currentScreen.canMove()){
                        KeyBindingManager.KeyRepeated(key);
                    }
                }
            });

            glfwSetMouseButtonCallback(handle, (l, button, action, mods) -> {
                if (currentScreen != null) {
                    if (action == GLFW_RELEASE) {
                        Vector2d mousePos = getMousePosition();
                        currentScreen.mouseReleased(mousePos.x,mousePos.y, button);
                    }else if (action == GLFW_PRESS) {
                        Vector2d mousePos = getMousePosition();
                        currentScreen.mouseClicked(mousePos.x,mousePos.y, button);
                    }
                }

                if (action == GLFW_PRESS) {
                    if (currentScreen == null || currentScreen.canMove()){
                        KeyBindingManager.MouseClicked(button);
                    }
                }else if (action == GLFW_RELEASE) {
                    if (currentScreen == null || currentScreen.canMove()){
                        KeyBindingManager.MouseReleased(button);
                    }
                }
            });

            glfwSetCharCallback(handle, (l, character) -> {
                char typed = (char)character;
                if (currentScreen != null) {
                    currentScreen.charTyped(character);
                }
            });

            windowResized(handle,800,600);
            glfwSetCursorPosCallback(handle, (l, x, y) -> {
                if (currentScreen != null) {
                    currentScreen.mouseMoved(x,y);
                }
            });
            glfwSetWindowSizeCallback(handle, Main::windowResized);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            setScreen(new StartScreen(Text.literal("Hi")));
            paused = false;
            while (!GLFW.glfwWindowShouldClose(handle)) {
                GLFW.glfwPollEvents();

                paused = currentScreen != null && currentScreen.shouldPause();

                double[] x = new double[1];
                double[] y = new double[1];
                GLFW.glfwGetCursorPos(handle, x, y);
                double mouseX = x[0];
                double mouseY = y[0];

                double dx = 0;
                double dy = 0;

                if (lastX >= 0 && lastY >= 0) {
                    dx = mouseX - lastX;
                    dy = mouseY - lastY;
                }

                lastX = mouseX;
                lastY = mouseY;
                GL33.glClearColor(0.36078432f, 0.36078432f, 0.36078432f, 1);
                GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
                for (Runnable runnable : Main.executeOnNextRenderThreadCall) {
                    runnable.run();
                }
                executeOnNextRenderThreadCall.clear();
                if (currentScreen instanceof EditorScreen editorScreen){
                    if (glfwGetMouseButton(handle,GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS){
                        System.out.println(dx + " " + dy);
                        editorCamera.position().x += (int) (dx * editorCamera.sizeMultiplier().getValue());
                    }
                    editorCamera.position().y = -(editorCamera.getSize().y / 2 - 64) + (editorScreen.getYOff() * 128);
                }

                int xOff = (getKeyBool(GLFW_KEY_A,handle) - getKeyBool(GLFW_KEY_D,handle));
                px -= xOff;


                glUseProgram(fragBasic);
                drawingUnderworld = false;
                setUniformVec2(fragBasic,"uCamPos",new Vector2f(camera.position()));
                setUniformVec2(fragBasic,"viewportSize",new Vector2f(camera.getSize()));
                DrawContext ctx = new DrawContext(batch);
                batch.begin();
                drawWorld(ctx,false);
                batch.flush();


                glUseProgram(invertedBasic);
                drawingUnderworld = true;
                setUniformVec2(invertedBasic,"uCamPos",new Vector2f(camera.position()));
                setUniformVec2(invertedBasic,"viewportSize",new Vector2f(camera.getSize()));

                batch.begin();
                drawWorld(ctx,true);
                batch.flush();

                drawUi();

                GLFW.glfwSwapBuffers(handle);
            }
            GLFW.glfwTerminate();
            GLFW.glfwTerminate();
            batch.destroy();
            System.exit(0);
        });
        renderThread.start();
        KeyBinds.init();
        Timer timer = new Timer("Main-0");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentScreen != null){
                    currentScreen.tick();
                }
                if (!paused) {
                    if (currentLevel != null){
                        currentLevel.tick();
                        overworldCamera.position().x = overworldCamera.getSize().x - ((currentLevel.getOverworldPlayer().getPosition().x + currentLevel.getUnderworldPlayer().getPosition().x) / 2) - (overworldCamera.getSize().x / 2);
                        overworldCamera.position().y = (currentLevel.getOverworldPlayer().getPosition().y) - (overworldCamera.getSize().y / 4);
                        underworldCamera.position().x = underworldCamera.getSize().x - ((currentLevel.getOverworldPlayer().getPosition().x + currentLevel.getUnderworldPlayer().getPosition().x) / 2) - (underworldCamera.getSize().x / 2);
                        underworldCamera.position().y = (currentLevel.getUnderworldPlayer().getPosition().y) - ((underworldCamera.getSize().y / 4) * 3);
                    }
                }
            }
        },0,10);
        return 0;
    }

    private static Vector2i onScreenPosToWorldPos(Vector2i screenPos) {
        float zoom = camera.sizeMultiplier().getValue();

        // screen (px) -> world (px)
        float worldX = -camera.position().x + screenPos.x * zoom;
        float worldY = camera.position().y + screenPos.y * zoom;

        return new Vector2i((int) worldX, (int) worldY);
    }


    private static void drawWorld(DrawContext context, boolean underworld) {
        // Welt-Tiles zeichnen – aPos = Welt-Pixelkoordinaten
        if (currentLevel == null)return;
        for (Map.Entry<Vector2i, TileWorld.Chunk> entry : currentLevel) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    Tile tile = entry.getValue().getTileLocal(x,y, underworld);
                    if (tile != null) {
                        int ox = (entry.getKey().x * 16) + x;
                        int oy = (entry.getKey().y * 16) + y;
                        context.drawSprite(
                                tile.getTexture(),
                                ox * 128,
                                oy * 128,
                                128,
                                128
                        );
                    }
                }
            }
        }
        if (underworld) {
            currentLevel.getUnderworldPlayer().render(context);
        }else {
            currentLevel.getOverworldPlayer().render(context);
        }



        Vector2d mousePos = getMousePosition();
        if (currentScreen instanceof EditorScreen editorScreen
        && !editorScreen.isInside(mousePos.x,mousePos.y)) {

            Vector2i worldMousePos = onScreenPosToWorldPos(
                    new Vector2i((int) mousePos.x, (int) mousePos.y)
            );

            int tileX = Math.floorDiv(worldMousePos.x, 128);
            int tileY = Math.floorDiv(worldMousePos.y, 128);

            int snappedX = tileX * 128;
            int snappedY = tileY * 128;
            if (editorScreen.getSelectedTile() != null) {
                context.drawSprite(editorScreen.getSelectedTile().getTexture(), snappedX, snappedY, 128, 128);
            }
            if ((glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS || glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS)
                    && (underworld ? mousePos.y > (double) lastSize.y / 2
                    : mousePos.y < (double) lastSize.y / 2)) {

                Vector2i tilePos = new Vector2i(tileX, tileY);
                currentLevel.setTileAt(tilePos, glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS ? editorScreen.getSelectedTile() : null, underworld);
            }

            editorScreen.drawOutLine(context, snappedX, snappedY, 0xFFFFFFFF);
        }
    }


    private static void windowResized(long handle, int width, int height) {
        lastSize.x=width;
        lastSize.y=height;
        overworldCamera.size().x =width;
        overworldCamera.size().y =height;
        underworldCamera.size().x =width;
        underworldCamera.size().y =height;
        editorCamera.size().x =width;
        editorCamera.size().y =height;
        GL33.glViewport(0, 0, width, height);
        setUniformVec2(uiBasic,"viewportSize",new Vector2f(width,height));
        setUniformVec2(fragBasic,"viewportSize",new Vector2f(width,height));
        setUniformVec2(invertedBasic,"viewportSize",new Vector2f(width,height));
        setUniformVec2(testBasic,"viewportSize",new Vector2f(width,height));
        if (currentScreen != null){
            currentScreen.clearAndInit(width,height);
        }
    }

    private static final InGameHud hud = new InGameHud();

    private static VertexBatch batch = null;
    @Getter
    private static TextRenderer textRenderer = null;

    private static void drawUi() {

        glUseProgram(uiBasic);

        batch.begin();

        DrawContext context = new DrawContext(batch);

        hud.render(context);

        if (currentScreen != null){
            if (!currentScreen.canMove()){
                batch.quadColored(0,0,lastSize.x,lastSize.y,
                        0f,0f,0f,0.66f);
            }

            Vector2d mpos = getMousePosition();
            currentScreen.render(context, mpos.x, mpos.y);
        }
        DrawContext.forceEndFrame();
        batch.flush();
    }

    private static Vector2d getMousePosition() {
        double[] xBuffer =  new double[1];
        double[] yBuffer = new double[1];
        glfwGetCursorPos(handle,xBuffer,yBuffer);
        double mx  = xBuffer[0];
        double my  = yBuffer[0];
        return new Vector2d(mx,my);
    }

    private static int getKeyBool(int key, long windowHandle) {
        return glfwGetKey(windowHandle,key);
    }

    private static void setUniformMatrix4f(int program,String name, Matrix4f mat) {
        glUseProgram(program);
        int loc = glGetUniformLocation(program, name);
        glUniformMatrix4fv(loc,false,mat.get(new float[16]));
    }
    private static void setUniformVec2(int program,String name, Vector2f vec) {
        glUseProgram(program);
        int loc = glGetUniformLocation(program, name);
        glUniform2f(loc,vec.x,vec.y);
    }
    private static void setUniformFloat(int program,String name, float f) {
        glUseProgram(program);
        int loc = glGetUniformLocation(program, name);
        glUniform1f(loc,f);
    }
    private static void setUniformSampler2DArray(int program,String name, TextureLoader.TextureArray textureArray) {
        glUseProgram(program);
        int texUnit = 0; // wir benutzen Texture Unit 0

        glActiveTexture(GL_TEXTURE0 + texUnit);
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray.textureId);

        glUseProgram(uiBasic);
        int loc = glGetUniformLocation(uiBasic, name);
        glUniform1i(loc, texUnit);
    }
    private static void setUniformSampler2D(int program,String name, int textureId) {
        glUseProgram(program);
        int texUnit = 1; // wir benutzen Texture Unit 0

        glActiveTexture(GL_TEXTURE0 + texUnit);
        glBindTexture(GL_TEXTURE_2D, textureId);
        int loc = glGetUniformLocation(program, name);
        glUniform1i(loc, texUnit);
    }

    public static void setUniformVec4(int program,String name, Vector4f vec) {
        glUseProgram(program);
        int loc = glGetUniformLocation(program, name);
        glUniform4f(loc,vec.x,vec.y,vec.z,vec.w);
    }

    public static JarFile getCurrentJar(){
        CodeSource src = Main.class.getProtectionDomain().getCodeSource();
        if (src == null) {
            throw new IllegalStateException("No CodeSource");
        }

        try {
            URI uri = src.getLocation().toURI();
            File file = new File(uri);
            if (file.getName().endsWith(".jar")) {
                throw new IllegalStateException("Im not running in a Jar");
            }
            JarFile jarFile = new JarFile(file);
            return jarFile;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void loadTextures(){
        if (batch == null) {
            batch = new VertexBatch(6 * 1024);
        }

        List<String> pngFiles = new ArrayList<>();
        InputStream stream = Main.class.getResourceAsStream("/assets.idx");
        if (stream != null) {
            Scanner scanner = new Scanner(stream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                pngFiles.add(line);
            }
        }

        try {
            TextureLoader.TextureArray textureArray = TextureLoader.loadFromJarTextures(pngFiles);
            Main.textures = textureArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TrueTypeFont font = new TrueTypeFont(Main.class.getResourceAsStream("/NeonSans.ttf"),64, "NeonSans");
        setUniformSampler2D(uiBasic,"font",font.getTextureId());
        setUniformSampler2D(fragBasic,"font",font.getTextureId());
        setUniformSampler2D(invertedBasic,"font",font.getTextureId());
        setUniformSampler2D(testBasic,"font",font.getTextureId());
        textRenderer = new TextRenderer(font);
    }

    private static void checkShader(int shader, String name) {
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            System.err.println("SHADER COMPILE ERROR in " + name + ":\n" + log);
        }
    }

    private static boolean checkProgram(int program, String name) {
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            throw new ShaderLinkException(name,log);
        }
        return status == GL_TRUE;
    }
    private static Map<String,Integer> shaderCache = new HashMap<>();
    private static void loadShaders() {
        fragBasic = loadShader("normal_basic","normal_basic");
        invertedBasic = loadShader("normal_basic","inverted_basic");
        testBasic = loadShader("ui_basic","test");
        Main.uiBasic = loadShader("ui_basic","ui_basic");
    }
    private static int loadShader(String vertexPath, String fragmentPath) {
        try {
            int gridBasic;
            if(!shaderCache.containsKey(fragmentPath + ".fsh")){
                String string = new String(Main.class.getResourceAsStream("/" + fragmentPath + ".fsh").readAllBytes());
                gridBasic = GL33.glCreateShader(GL20.GL_FRAGMENT_SHADER);
                GL33.glShaderSource(gridBasic,string);
                GL33.glCompileShader(gridBasic);

                checkShader(gridBasic,"/" + fragmentPath + ".fsh");
                shaderCache.put(fragmentPath + ".fsh",gridBasic);
            }else {
                gridBasic = shaderCache.get(fragmentPath + ".fsh");
            }

            int gridBasicV;
            if(!shaderCache.containsKey(vertexPath + ".vsh")){
                String string = new String(Main.class.getResourceAsStream("/" + vertexPath + ".vsh").readAllBytes());
                gridBasicV = GL33.glCreateShader(GL20.GL_VERTEX_SHADER);
                GL33.glShaderSource(gridBasicV,string);
                GL33.glCompileShader(gridBasicV);

                checkShader(gridBasicV,"/" + vertexPath + ".vsh");
                shaderCache.put(vertexPath + ".vsh",gridBasicV);
            }else {
                gridBasicV = shaderCache.get(vertexPath + ".vsh");
            }

            int prog = glCreateProgram();
            glAttachShader(prog,gridBasicV);
            glAttachShader(prog,gridBasic);
            glLinkProgram(prog);

            checkProgram(prog,"GLProgram{" + vertexPath + ":" + fragmentPath + "}");
            return prog;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void executeOnRenderThread(Runnable runnable){
        executeOnNextRenderThreadCall.add(runnable);
    }
}