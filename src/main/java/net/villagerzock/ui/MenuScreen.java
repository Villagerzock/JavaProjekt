package net.villagerzock.ui;

import net.villagerzock.DrawContext;
import net.villagerzock.Main;
import net.villagerzock.TextureLoader;
import net.villagerzock.text.Text;

public class MenuScreen extends Screen {
    private final Screen parent;
    public MenuScreen(Text text, Screen parent) {
        super(text);
        this.parent = parent;
    }
    public void renderBackgroundTiles(DrawContext dc) {
        TextureLoader.TexturePart part = Main.getTextures().getPart("textures/tiles_spritesheet.png->stone");
        dc.texture("textures/tiles_spritesheet.png->stone",0,0,Main.getSize().x,Main.getSize().y,part.u(),part.v(),part.width(),part.height(),2048,2048);
    }
    @Override
    public void onClose() {
        super.onClose();
        Main.setScreen(parent);
    }
}
