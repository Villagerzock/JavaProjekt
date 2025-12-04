package net.villagerzock.ui;

import net.villagerzock.DrawContext;

import java.util.List;

public interface Element {
    void mouseMoved(double mouseX, double mouseY);
    void mouseDragged(double mouseX, double mouseY, int button);
    void mouseClicked(double mouseX, double mouseY, int button);
    void mouseReleased(double mouseX, double mouseY, int button);
    void mouseScrolled(double mouseX, double mouseY, double amount);

    void keyPressed(int keyCode, int scanCode, int modifiers);
    void keyReleased(int keyCode, int scanCode, int modifiers);

    boolean isInside(double mouseX, double mouseY);

    void render(DrawContext context, double mouseX, double mouseY);
    void charTyped(int character);
}
