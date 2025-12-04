package net.villagerzock;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.opengl.GL33.*;

public final class ScissorStack {

    public void popAll() {
        if (STACK.isEmpty()) return;
        while (!STACK.isEmpty()) {
            STACK.pop();
        }
        apply(null,0);
    }

    private record Rect(int x, int y, int w, int h) {}

    private final Deque<Rect> STACK = new ArrayDeque<>();

    private Rect intersect(Rect a, Rect b) {
        int x1 = Math.max(a.x, b.x);
        int y1 = Math.max(a.y, b.y);
        int x2 = Math.min(a.x + a.w, b.x + b.w);
        int y2 = Math.min(a.y + a.h, b.y + b.h);

        int w = Math.max(0, x2 - x1);
        int h = Math.max(0, y2 - y1);
        return new Rect(x1, y1, w, h);
    }

    private void apply(Rect r, int windowHeight) {
        if (r == null) {
            glDisable(GL_SCISSOR_TEST);
            return;
        }

        glEnable(GL_SCISSOR_TEST);

        // UI: (0,0) oben links
        // GL: (0,0) unten links
        int sx = r.x;
        int sy = windowHeight - (r.y + r.h);
        glScissor(sx, sy, r.w, r.h);
    }

    public void push(int x, int y, int w, int h) {

        Rect newRect = new Rect(x, y, w, h);
        if (!STACK.isEmpty()) {
            newRect = intersect(STACK.peek(), newRect);
        }
        STACK.push(newRect);
        apply(newRect, Main.getSize().y);
    }

    public void pop() {
        if (STACK.isEmpty()) return;

        STACK.pop();

        Rect top = STACK.isEmpty() ? null : STACK.peek();
        apply(top, Main.getSize().y);
    }

    public boolean isEmpty() {
        return STACK.isEmpty();
    }
}
