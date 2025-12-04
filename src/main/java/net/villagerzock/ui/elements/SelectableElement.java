package net.villagerzock.ui.elements;

import lombok.Getter;
import net.villagerzock.DrawContext;
import net.villagerzock.ui.Element;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public abstract class SelectableElement implements Element {
    private boolean isHovered = false;
    @Getter
    private Vector2i pos;
    @Getter
    private Vector2i size;
    private final Consumer<SelectableElement> action;
    public SelectableElement(int x, int y, int width, int height, Consumer<SelectableElement> action) {
        this.pos = new Vector2i(x,y);
        this.size = new Vector2i(width,height);
        this.action = action;
    }
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        isHovered = isInside(mouseX, mouseY);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isInside(mouseX,mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            action.accept(this);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double amount) {

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public boolean isInside(double mouseX, double mouseY) {
        return mouseX > pos.x && mouseX < (pos.x + size.x) && mouseY > pos.y && mouseY < (pos.y + size.y);
    }

    @Override
    public void charTyped(int character) {

    }
}
