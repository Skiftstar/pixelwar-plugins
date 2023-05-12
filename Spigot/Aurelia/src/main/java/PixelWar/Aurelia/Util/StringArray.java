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
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].replace(toReplace, toReplaceWith);
        }
        return this;
    }
    
}
