package net.villagerzock.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public interface Text {
    static Text fromJson(JsonElement name) {
        if(name.isJsonPrimitive()){
            if (name.getAsJsonPrimitive().isString()){
                return Text.literal(name.getAsString());
            }
        }else if(name.isJsonObject()){
            JsonObject obj = name.getAsJsonObject();
            if(obj.has("text")){
                return Text.literal(obj.get("text").getAsString());
            }
        }else if(name.isJsonArray()){
            Text text = Text.empty();
            JsonArray array = name.getAsJsonArray();
            for(JsonElement jsonElement : array){
                Text entry = Text.fromJson(jsonElement);
                text.append(entry);
            }
            return text;
        }
        return Text.empty();
    }

    static Text empty() {
        return new MutableText();
    }

    List<ITextPart> getParts();
    void withStyle(Style style);
    default boolean isEmpty(){
        if(getParts().isEmpty())
            return true;
        for (ITextPart part : getParts()){
            if (!part.getText().isEmpty()){
                return false;
            }
        }
        return true;
    }
    Text append(String text);
    Text append(Text text);
    interface ITextPart {
        String getText();
        Style getStyle();
        void setStyle(Style style);
    }

    class TextPart implements ITextPart {
        @Getter
        private final String text;
        @Setter
        @Getter
        private Style style = Style.builder().build();

        public TextPart(String text) {
            this.text = text;
        }
    }


    static Text literal(String text) {
        return new MutableText().append(text);
    }
}
