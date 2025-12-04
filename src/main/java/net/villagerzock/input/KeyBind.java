package net.villagerzock.input;

import lombok.Getter;
import net.villagerzock.text.Text;

public class KeyBind {
    public enum InputType{
        KEY,
        MOUSE_BUTTON,
        MOUSE_WHEEL
    }
    @Getter
    private final Text title;
    @Getter
    private final InputType type;
    @Getter
    private final int defaultKey;
    private int pressed;
    private int typed;
    private boolean down;

    public KeyBind(Text title, InputType type, int defaultKey) {
        this.title = title;
        this.type = type;
        this.defaultKey = defaultKey;
    }

    public boolean isPressed(){
        return down;
    }
    public boolean wasPressed(){
        boolean result = pressed > 0;
        if(result){
            pressed--;
        }
        return result;
    }
    public boolean wasTyped(){
        if (typed > 0){
            typed--;
            return true;
        }
        return false;
    }

    public void down(){
        down = true;
        pressed++;
    }
    public void up(){
        down = false;
    }
    public void addPressed(int val){
        pressed += val;
    }
    public void addTyped(int val) {
        typed += val;
    }
}
