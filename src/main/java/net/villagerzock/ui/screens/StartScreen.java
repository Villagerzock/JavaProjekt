package net.villagerzock.ui.screens;

import net.villagerzock.Main;
import net.villagerzock.text.Text;
import net.villagerzock.ui.MenuScreen;
import net.villagerzock.ui.Screen;
import net.villagerzock.ui.elements.ButtonWidget;

import java.awt.*;

public class StartScreen extends MenuScreen {
    public StartScreen(Text text) {
        super(text,null);
    }

    @Override
    protected void init(int width, int height) {
        super.init(width,height);
        addChild(new ButtonWidget(width/2 - 400,height/2-40,800,80,(b)->{
            System.out.println("Button pressed");
            Main.setScreen(new LevelSelectScreen(this));
        }, Text.literal("Select Level")));
    }

    @Override
    public boolean shouldCloseWithEscape() {
        return false;
    }
}
