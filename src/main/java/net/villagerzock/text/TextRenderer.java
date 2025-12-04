package net.villagerzock.text;

import net.villagerzock.Color;
import net.villagerzock.VertexBatch;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class TextRenderer {

    private final TrueTypeFont font;
    private final float layer; // dein Texture-Layer / Slot im Shader

    public TextRenderer(TrueTypeFont font) {
        this.font = font;
        this.layer = font.getLayer();
    }

    public void drawText(VertexBatch batch,
                         Text text,
                         float x,
                         float y,
                         float r, float g, float b, float a) {

        if (text == null || text.isEmpty()) return;

        STBTTBakedChar.Buffer cdata = font.charData();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xpos = stack.floats(x);
            FloatBuffer ypos = stack.floats(y);

            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            for (Text.ITextPart part : text.getParts()) {
                for (int i = 0; i < part.getText().length(); i++) {
                    char ch = part.getText().charAt(i);

                    if (ch == '\n') {
                        xpos.put(0, x);
                        ypos.put(0, ypos.get(0) + font.pixelHeight());
                        continue;
                    }

                    int codePoint = ch;
                    if (codePoint < TrueTypeFont.FIRST_CHAR ||
                            codePoint >= TrueTypeFont.FIRST_CHAR + TrueTypeFont.CHAR_COUNT) {
                        continue;
                    }
                    STBTruetype.stbtt_GetBakedQuad(
                            cdata,
                            TrueTypeFont.ATLAS_WIDTH,
                            TrueTypeFont.ATLAS_HEIGHT,
                            codePoint - TrueTypeFont.FIRST_CHAR,
                            xpos,
                            ypos,
                            q,
                            true
                    );

                    float x0 = q.x0();
                    float y0 = q.y0();
                    float x1 = q.x1();
                    float y1 = q.y1();

                    float s0 = q.s0();
                    float t0 = q.t0();
                    float s1 = q.s1();
                    float t1 = q.t1();

                    // packen als Quad in deinen Batch
                    Color color = part.getStyle().getColor(new Color(r,g,b,a));
                    batch.quadFont(
                            x0, y0, x1, y1,
                            s0, t0,   // u0,v0
                            s1, t1,   // u1,v1
                            color.x, color.y, color.z, color.w
                    );
                }
            }
        }
    }
    public float getWidth(String text){
        return this.getWidth(Text.literal(text));
    }
    public float getWidth(Text text) {
        if (text == null || text.isEmpty()) return 0.0f;

        STBTTBakedChar.Buffer cdata = font.charData();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // wir starten bei x=0, y=0
            FloatBuffer xpos = stack.floats(0.0f);
            FloatBuffer ypos = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            float maxX = 0.0f;

            for (Text.ITextPart part : text.getParts()) {
                for (int i = 0; i < part.getText().length(); i++) {
                    char ch = part.getText().charAt(i);

                    if (ch == '\n') {
                        // neue Zeile: x wieder 0, y hoch → Breite ist maxX der bisherigen Zeilen
                        xpos.put(0, 0.0f);
                        ypos.put(0, ypos.get(0) + font.pixelHeight());
                        continue;
                    }

                    int codePoint = ch;
                    if (codePoint < TrueTypeFont.FIRST_CHAR ||
                            codePoint >= TrueTypeFont.FIRST_CHAR + TrueTypeFont.CHAR_COUNT) {
                        continue;
                    }

                    STBTruetype.stbtt_GetBakedQuad(
                            cdata,
                            TrueTypeFont.ATLAS_WIDTH,
                            TrueTypeFont.ATLAS_HEIGHT,
                            codePoint - TrueTypeFont.FIRST_CHAR,
                            xpos,
                            ypos,
                            q,
                            true
                    );

                    // x1 ist das rechte Ende des aktuellen Zeichens
                    if (q.x1() > maxX) {
                        maxX = q.x1();
                    }
                }
            }

            return maxX; // da wir bei 0 starten, ist maxX direkt die Breite in Pixeln
        }
    }
}