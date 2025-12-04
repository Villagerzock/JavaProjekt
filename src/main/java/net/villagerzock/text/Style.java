package net.villagerzock.text;

import lombok.Builder;
import lombok.Getter;
import net.villagerzock.Color;

@Builder(toBuilder = true)
public class Style {
    @Getter
    private final boolean bold;
    @Getter
    private final boolean italic;
    @Getter
    private final boolean underline;
    @Getter
    private final Color color;

    public Color getColor(Color defaultColor) {
        if (color == null) {
            return defaultColor;
        }
        return color;
    }

    private Style(boolean bold, boolean italic, boolean underline, Color color){
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.color = color;
    }

}
