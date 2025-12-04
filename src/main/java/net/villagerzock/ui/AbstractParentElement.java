package net.villagerzock.ui;

import net.villagerzock.DrawContext;

import java.util.List;

public abstract class AbstractParentElement implements Element {
    public abstract List<Element> getChildren();

    public void addChild(Element child) {
        getChildren().add(child);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Element element : getChildren()) {
            element.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        for (Element element : getChildren()) {
            element.mouseDragged(mouseX, mouseY,button);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (Element element : getChildren()) {
            element.mouseClicked(mouseX, mouseY,button);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Element element : getChildren()) {
            element.mouseReleased(mouseX, mouseY,button);
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Element element : getChildren()) {
            element.mouseScrolled(mouseX, mouseY,amount);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element element : getChildren()) {
            element.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Element element : getChildren()) {
            element.keyReleased(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean isInside(double mouseX, double mouseY) {
        for (Element element : getChildren()) {
            if (element.isInside(mouseX,mouseY)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        for (Element element : getChildren()) {
            element.render(context, mouseX, mouseY);
        }
    }

    @Override
    public void charTyped(int character) {
        for (Element element : getChildren()) {
            element.charTyped(character);
        }
    }
}
