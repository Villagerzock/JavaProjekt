package net.villagerzock.ui;

import lombok.Getter;
import net.villagerzock.Main;
import net.villagerzock.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Screen extends AbstractParentElement {
    private List<Element> elements = new ArrayList<>();
    @Getter
    private final Text text;

    public Screen(Text text) {
        this.text = text;
    }
    @Override
    public List<Element> getChildren() {
        return elements;
    }
    public void clearAndInit(int width, int height) {
        elements.clear();
        init(width, height);
    }
    protected void init(int width, int height){
    }
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && shouldCloseWithEscape()) {
            close();
        }
    }
    public void tick(){}
    public boolean shouldCloseWithEscape() {
        return true;
    }
    public boolean canMove(){
        return false;
    }
    public void onClose() {
    }
    public final void close(){
        Main.setScreen(null);
        onClose();
    }

    public boolean shouldPause() {
        return true;
    }
}
