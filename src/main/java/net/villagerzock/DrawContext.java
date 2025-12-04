package net.villagerzock;

import lombok.Getter;
import net.villagerzock.text.Text;
import net.villagerzock.text.TextRenderer;
import net.villagerzock.ui.Screen;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4f;

public class DrawContext {
    @Getter
    private final VertexBatch batch;
    private static final ScissorStack stack = new ScissorStack();
    public DrawContext(VertexBatch batch) {
        this.batch = batch;
    }
    public DrawContext enableScissor(int x0, int y0, int x1, int y1){
        batch.flush();
        stack.push(x0,y0,x1-x0,y1-y0);
        batch.begin();
        return this;
    }
    public DrawContext disableScissor(){
        batch.flush();
        stack.pop();
        batch.begin();
        return this;
    }
    public DrawContext drawSprite(String texture, int x, int y, int w, int h){
        TextureLoader.TexturePart part = Main.getTextures().getPart(texture);

        float u0 = (float) part.u() / (float) part.tw();
        float v0 = (float) part.v() / (float) part.th();
        float u1 = ((float) part.u() + (float) part.width()) / (float) part.tw();
        float v1 = ((float) part.v() + (float) part.height()) / (float) part.th();
        batch.quad(
                x, y, w, h,
                u0, v0, u1, v1,
                1f, 1f, 1f, 1f,
                part.layer()
        );
        return this;
    }
    public DrawContext texture(String texture,int x, int y){
        return this.texture(texture,x,y,0,0);
    }

    public DrawContext texture(String texture,int x, int y, int u, int v){
        return this.texture(texture,x,y,u,v,2048,2048);
    }

    public DrawContext texture(String texture,int x, int y, int u, int v, int w, int h){
        return this.texture(texture,x,y,u,v,w,h,2048,2048);
    }

    public DrawContext texture(String texture,int x, int y, int u, int v, int w, int h, int tw, int th) {
        // rw/rh = w/h ⇒ kein Tiling (xa=ya=1)
        return this.texture(texture,x,y,w,h,u,v,w,h,tw,th);
    }

    public DrawContext texture(String texture,int x, int y,int rw,int rh,
                               int u, int v, int w, int h, int tw, int th) {
        // halber Texel in Atlas-Koordinaten
        float epsU = 0.5f / (float) tw;
        float epsV = 0.5f / (float) th;

        float u0 = ((float) u + epsU)          / (float) tw;
        float v0 = ((float) v + epsV)          / (float) th;
        float u1 = ((float) (u + w) - epsU)    / (float) tw;
        float v1 = ((float) (v + h) - epsV)    / (float) th;

        float xa = (float) rw / (float) w;
        float ya = (float) rh / (float) h;

        batch.quad(
                x, y, rw, rh,
                u0, v0, u1, v1,
                1f, 1f, 1f, 1f,
                Main.getTextures().getLayer(texture),
                xa, ya
        );
        return this;
    }


    public DrawContext text(TextRenderer renderer, String text, int x, int y, Color color){
        return this.text(renderer,Text.literal(text),x,y,color);
    }
    public DrawContext text(TextRenderer renderer, Text text, int x, int y, Color color){
        renderer.drawText(batch, text,x,y,color.x,color.y,color.z,color.w);
        return this;
    }

    public DrawContext fill(int x0, int y0, int x1, int y1,Color color){
        batch.quadColored(x0,y0, x1-x0, y1-y0,
                color.x,color.y,color.z,color.w);
        return this;
    }

    public static void forceEndFrame(){
        stack.popAll();
    }

    public void setShaderColor(Color color) {
        batch.flush();
        Main.setUniformVec4(Main.invertedBasic,"shaderColor",color);
        Main.setUniformVec4(Main.fragBasic,"shaderColor",color);
        Main.setUniformVec4(Main.uiBasic,"shaderColor",color);
        batch.begin();
    }
}
