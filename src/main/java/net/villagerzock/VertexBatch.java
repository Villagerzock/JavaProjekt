package net.villagerzock;

import lombok.Getter;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

public class VertexBatch {

    // pos2 + uv4 + color4 + layer1
    private static final int FLOATS_PER_VERTEX = 2 + 2 + 4 + 4 + 1 + 1; // = 14
    private static final int BYTES_PER_VERTEX  = FLOATS_PER_VERTEX * Float.BYTES;

    @Getter
    private final int vao;
    @Getter
    private final int vbo;
    private final int maxVertices;

    private final FloatBuffer buffer;
    private int vertexCount = 0;
    private boolean began = false;

    public VertexBatch(int maxVertices) {
        this.maxVertices = maxVertices;
        this.buffer = memAllocFloat(maxVertices * FLOATS_PER_VERTEX);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER,
                (long) maxVertices * BYTES_PER_VERTEX,
                GL_DYNAMIC_DRAW);

        int stride = BYTES_PER_VERTEX;

        // location 0: vec2 aPos
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(
                0,
                2,
                GL_FLOAT,
                false,
                stride,
                0L
        );

        // location 1: vec4 aUv (u0,v0,u1,v1 oder was auch immer du willst)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(
                1,
                2,
                GL_FLOAT,
                false,
                stride,
                2L * Float.BYTES
        );

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(
                2,
                4,
                GL_FLOAT,
                false,
                stride,
                4L * Float.BYTES
        );

        // location 2: vec4 aColor
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(
                3,
                4,
                GL_FLOAT,
                false,
                stride,
                8L * Float.BYTES
        );

        // location 3: float aLayer
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(
                4,
                1,
                GL_FLOAT,
                false,
                stride,
                12L * Float.BYTES
        );
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(
                5,
                1,
                GL_FLOAT,
                false,
                stride,
                13L * Float.BYTES
        );

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void begin() {
        if (began) {
            throw new IllegalStateException("begin() already called without flush()");
        }
        began = true;
        vertexCount = 0;
        buffer.clear();
    }

    public void vertex(float x, float y,
                       float u, float v,
                       float u0,float v0,float u1,float v1,
                       float r, float g, float b, float a,
                       float layer,boolean isText) {

        if (!began) {
            throw new IllegalStateException("Call begin() before vertex()");
        }
        if (vertexCount >= maxVertices) {
            throw new IllegalStateException("VertexBatch overflow: " + maxVertices);
        }

        buffer.put(x).put(y);                  // pos vec2
        buffer.put(u).put(v);                  // uv vec2
        buffer.put(u0).put(v0).put(u1).put(v1);// AtlasUVValues vec4
        buffer.put(r).put(g).put(b).put(a);    // color
        buffer.put(layer);                     // layer (oder -1)
        buffer.put(isText ? 1f : 0f);          // is text boolean

        vertexCount++;
    }

    /**
     * Quad in deinem 2D-Space (NDC oder Screen, je nach Shader)
     * UV: u0,v0,u1,v1 (z.B. Min/Max im Texture-Atlas oder 0..1)
     */
    public void quad(float x, float y, float w, float h,
                     float u0, float v0u, float u1, float v1u,
                     float r, float g, float b, float a,
                     float layer) {
        // Standard-Quad: ohne Tiling (xa = ya = 1)
        this.quad(x, y, w, h,
                u0, v0u, u1, v1u,
                r, g, b, a,
                layer,
                1f, 1f);
    }

    public void quad(float x, float y, float w, float h,
                     float u0, float v0u, float u1, float v1u,
                     float r, float g, float b, float a,
                     float layer,
                     float xa, float ya) {
        float v0 = 1f - v0u;
        float v1 = 1f - v1u;

        // Tri 1
        vertex(x,     y,
                0f,     0f,          // vUv: 0..xa / 0..ya
                u0, v0, u1, v1,
                r, g, b, a, layer, false);

        vertex(x + w, y,
                xa,     0f,
                u0, v0, u1, v1,
                r, g, b, a, layer, false);

        vertex(x + w, y + h,
                xa,     ya,
                u0, v0, u1, v1,
                r, g, b, a, layer, false);

        // Tri 2
        vertex(x,     y,
                0f,     0f,
                u0, v0, u1, v1,
                r, g, b, a, layer, false);

        vertex(x + w, y + h,
                xa,     ya,
                u0, v0, u1, v1,
                r, g, b, a, layer, false);

        vertex(x,     y + h,
                0f,     ya,
                u0, v0, u1, v1,
                r, g, b, a, layer, false);
    }

    public void quadFont(float x0, float y0, float x1, float y1,
                         float s0, float t0, float s1, float t1,
                         float r, float g, float b, float a) {

        // Tri 1
        vertex(x0, y0, s0, t0, s0, t0, s1, t1, r,g,b,a, -1, true);
        vertex(x1, y0, s1, t0, s0, t0,  s1, t1, r,g,b,a, -1, true);
        vertex(x1, y1, s1, t1, s0, t0,  s1, t1, r,g,b,a, -1, true);

        // Tri 2
        vertex(x0, y0, s0, t0, s0, t0,  s1, t1, r,g,b,a, -1, true);
        vertex(x1, y1, s1, t1, s0, t0,  s1, t1, r,g,b,a, -1, true);
        vertex(x0, y1, s0, t1, s0, t0,  s1, t1, r,g,b,a, -1, true);
    }

    /**
     * Einfarbiges Quad (keine Textur) → layer = -1
     * UV ist egal, wird im Fragment ignoriert.
     */
    public void quadColored(float x, float y, float w, float h,
                            float r, float g, float b, float a) {
        float u0 = 0f, v0 = 0f, u1 = 0f, v1 = 0f;
        float layer = -1f; // wichtig: -1 markiert "kein Texture-Sample"
        quad(x, y, w, h, u0, v0, u1, v1, r, g, b, a, layer);
    }

    public void flush() {
        if (!began) return;
        if (vertexCount == 0) {
            began = false;
            buffer.clear();
            return;
        }

        buffer.flip();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        began = false;
        vertexCount = 0;
        buffer.clear();
    }

    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        memFree(buffer);
    }

}

