package net.villagerzock.text;

import java.util.ArrayList;
import java.util.List;

public class MutableText implements Text {
    private List<ITextPart> parts = new ArrayList<ITextPart>();
    @Override
    public List<ITextPart> getParts() {
        return parts;
    }

    @Override
    public void withStyle(Style style) {
        for(ITextPart part : parts) {
            part.setStyle(style);
        }
    }

    @Override
    public Text append(String text) {
        parts.add(new TextPart(text));
        return this;
    }

    @Override
    public Text append(Text text) {
        parts.addAll(text.getParts());
        return this;
    }
}
