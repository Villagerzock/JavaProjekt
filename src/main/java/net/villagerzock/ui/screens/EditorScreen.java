package net.villagerzock.ui.screens;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.villagerzock.Color;
import net.villagerzock.DrawContext;
import net.villagerzock.Main;
import net.villagerzock.input.KeyBinds;
import net.villagerzock.text.Text;
import net.villagerzock.tileWorld.Tile;
import net.villagerzock.ui.Screen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class EditorScreen extends Screen {
    private int selectedTile = -1;
    @Getter
    private int yOff = 0;

    public Tile getSelectedTile() {
        return Tile.getTileType(selectedTile);
    }

    public EditorScreen(Text text) {
        super(text);
    }

    public static final int TILES_PER_LINE = 4; // 512 / 128

    private int savedPopupTick = 0;

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        super.render(context, mouseX, mouseY);
        context.enableScissor(Main.getSize().x-512, 0, Main.getSize().x, Main.getSize().y);
        context.fill(Main.getSize().x - 512,0,Main.getSize().x,Main.getSize().y, new Color(0x000000FF));
        for (int i = 0; i<Tile.tiles.size(); ++i) {
            int x = (i % TILES_PER_LINE) * 128 + (Main.getSize().x - 512);
            int y = (i / TILES_PER_LINE) * 128;
            Tile tile = Tile.tiles.get(i);
            context.drawSprite(tile.getTexture(), x, y, 128,128);
            if (i == selectedTile) {
                drawOutLine(context,x,y,0x0094FFFF);
            }
        }
        double actualMx = mouseX - (Main.getSize().x - 512);
        if (actualMx > 0){
            //System.out.println(String.format("MX: %.2f Size: [%d, %d]", actualMx, Main.getSize().x, Main.getSize().y));
            int tx = (int)(actualMx - (actualMx % 128));
            int ty = (int)(mouseY - (mouseY % 128));

            drawOutLine(context,tx+(Main.getSize().x - 512),ty,0xFFFFFFFF);
        }

        context.disableScissor();

        context.fill(Main.getSize().x / 2 - 300,Main.getSize().y - Math.min(savedPopupTick > 600 ? Math.max(savedPopupTick - 600, 0) : savedPopupTick,savedPopupTick > 600 ? 0 : 60),Main.getSize().x / 2 + 300, Main.getSize().y, Color.CYAN);
        context.text(Main.getTextRenderer(),"Saved Successfully",Main.getSize().x / 2 - 300,Main.getSize().y - Math.min(savedPopupTick > 600 ? Math.max(savedPopupTick - 600, 0) : savedPopupTick,savedPopupTick > 600 ? 0 : 60) + 48, Color.BLACK);

        if (savedPopupTick > 0) {
            savedPopupTick++;
        }
        if (savedPopupTick > 800) {
            savedPopupTick = 0;
        }
    }

    public void drawOutLine(DrawContext context, int tx, int ty, int color) {
        context.fill(tx,ty,tx+TILES_PER_LINE,ty+128,new Color(color));
        context.fill(tx+123,ty,tx+128,ty+128,new Color(color));
        context.fill(tx,ty,tx+128,ty+TILES_PER_LINE,new Color(color));
        context.fill(tx,ty+123,tx+128,ty+128,new Color(color));
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW_KEY_0){
            close();
        }
        if (keyCode == GLFW_KEY_S && (modifiers & GLFW_MOD_CONTROL) != 0){
            try {
                File file = new File("./level_1.json");
                if (!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                Main.getCurrentLevel().save(new GsonBuilder().setPrettyPrinting().create(),new FileWriter(file));
                savedPopupTick = 1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        double actualMx = mouseX - (Main.getSize().x - 512);
        int tx = (int)(actualMx - (actualMx % 128)) / 128;
        int ty = (int)(mouseY - (mouseY % 128)) / 128;
        int i = (ty * TILES_PER_LINE) + tx;
        System.out.println(i);
        if (i < Tile.tiles.size() && actualMx >= 0){
            selectedTile = i;
        }
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public boolean isInside(double mouseX, double mouseY) {
        return super.isInside(mouseX, mouseY) || mouseX >= Main.getSize().x - 512;
    }

    @Override
    public void tick() {
        super.tick();
        while (KeyBinds.UP.wasTyped()) {
            yOff--;
        }
        while (KeyBinds.DOWN.wasTyped()) {
            yOff++;
        }
    }
}
