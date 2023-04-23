package PixelWar.Aurelia.Util;

import java.util.List;

public class StringArray {

    private String[] array;

    public StringArray(List<String> list) {
        this.array = list.toArray(new String[0]);
    }

    public StringArray(String... input) {
        this.array = input;
    }

    public String[] getArray() {
        return array;
    }

    public StringArray replace(String toReplace, String toReplaceWith) {
        for (String s : array) {
            s = s.replace(toReplace, toReplaceWith);
        }
        return this;
    }
    
}
