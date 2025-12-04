package net.villagerzock.ui.elements;

import net.villagerzock.Color;
import net.villagerzock.DrawContext;
import net.villagerzock.Main;
import net.villagerzock.text.Text;

import java.awt.*;
import java.util.function.Consumer;

public class ButtonWidget extends SelectableElement {
    private final Text text;
    public ButtonWidget(int x, int y, int width, int height, Consumer<SelectableElement> action, Text text) {
        super(x, y, width, height, action);
        this.text = text;
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        context.drawSprite(this.isInside(mouseX,mouseY) ? "textures/ui_test.png->button_hovered" : "textures/ui_test.png->button",getPos().x, getPos().y, getSize().x, getSize().y);
        context.text(Main.getTextRenderer(),text, (int) (getPos().x + ((float) getSize().x / 2) - Main.getTextRenderer().getWidth(text) / 2), getPos().y + (getSize().y / 2) + 16, new Color(0xFFFFFFFF));
    }
}
