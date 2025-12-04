package net.villagerzock.input;

import net.villagerzock.text.Text;

import static org.lwjgl.glfw.GLFW.*;

public class KeyBinds {
    public static final KeyBind JUMP = register(
            Text.literal("Jump"),
            KeyBind.InputType.KEY,
            GLFW_KEY_SPACE
    );
    public static final KeyBind LEFT = register(
            Text.literal("Left"),
            KeyBind.InputType.KEY,
            GLFW_KEY_A
    );
    public static final KeyBind RIGHT = register(
            Text.literal("Right"),
            KeyBind.InputType.KEY,
            GLFW_KEY_D
    );
    public static final KeyBind DOWN = register(
            Text.literal("Down"),
            KeyBind.InputType.KEY,
            GLFW_KEY_S
    );
    public static final KeyBind UP = register(
            Text.literal("Up"),
            KeyBind.InputType.KEY,
            GLFW_KEY_W
    );

    private static KeyBind register(Text text, KeyBind.InputType inputType, int key) {
        KeyBind keyBind = new KeyBind(
                text,
                inputType,
                key
        );
        KeyBindingManager.register(keyBind);
        return keyBind;
    }

    public static void init(){}
}
