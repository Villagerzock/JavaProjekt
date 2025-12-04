package net.villagerzock.input;

import lombok.Setter;
import net.villagerzock.MapList;

import java.util.HashMap;
import java.util.Map;

public class KeyBindingManager {

    private record KeyBindingData(KeyBind.InputType type, int keyCode){
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof KeyBindingData keyBindingData){
                return type == keyBindingData.type && keyCode==keyBindingData.keyCode;
            }
            return false;
        }
    }

    private static MapList<KeyBindingData,KeyBind> keyBindings = new MapList<>();
    @Setter
    private static int modifiers = 0;


    public static boolean isModifier(int modifier){
        return (modifiers & modifier) != 0;
    }

    public static void register(KeyBind keyBinding){
        keyBindings.putItem(new KeyBindingData(keyBinding.getType(),keyBinding.getDefaultKey()), keyBinding);
    }
    public static void KeyPressed(int keycode){
        for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.KEY,keycode))){
            bind.down();
            bind.addTyped(1);
        }
    }
    public static void KeyReleased(int keycode){
        for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.KEY,keycode))){
            bind.up();
        }
    }
    public static void KeyRepeated(int keycode) {
        for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.KEY,keycode))){
            bind.addTyped(1);
        }
    }
    public static void MouseClicked(int button){
        for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.MOUSE_BUTTON,button))){
            bind.down();
        }
    }
    public static void MouseReleased(int button){
        for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.MOUSE_BUTTON,button))){
            bind.up();
        }
    }
    public static void MouseScrolled(int value){
        int actualValue = Math.abs(value);
        if (value > 0){
            for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.MOUSE_WHEEL,0))){
                bind.addPressed(actualValue);
            }
        }else if (value < 0){
            for (KeyBind bind : keyBindings.get(new KeyBindingData(KeyBind.InputType.MOUSE_WHEEL,1))){
                bind.addPressed(actualValue);
            }
        }
    }
}
