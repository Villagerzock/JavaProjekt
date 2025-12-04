package net.villagerzock.ui.screens;

import net.villagerzock.DrawContext;
import net.villagerzock.Main;
import net.villagerzock.text.Text;
import net.villagerzock.tileWorld.TileWorldLoadable;
import net.villagerzock.ui.MenuScreen;
import net.villagerzock.ui.Screen;
import net.villagerzock.ui.elements.ButtonWidget;

public class LevelSelectScreen extends MenuScreen {
    public LevelSelectScreen(Screen parent) {
        super(Text.empty(), parent);
    }

    @Override
    protected void init(int width, int height) {
        super.init(width, height);
        int y = 0;
        for (TileWorldLoadable loadable : Main.getLoadables()) {
            this.addChild(new ButtonWidget(Main.getSize().x/2 - 400,y,800,80,(button)->{
                Main.loadLevel(loadable);
            },loadable.title()));
            y += 85;
        }
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        renderBackgroundTiles(context);
        super.render(context, mouseX, mouseY);
    }
}
