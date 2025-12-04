package net.villagerzock;

public class InGameHud {
    public void render(DrawContext dc) {
        if (Main.getCurrentLevel() != null){
            dc.text(Main.getTextRenderer(),"Time: " + Main.getCurrentLevel().getTime(),0,48,Color.CYAN);
        }

    }
}
