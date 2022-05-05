package kyu.cities.Util;

public enum EntryRequirement {
    INVITE_ONLY, NONE, REQUEST;

    public static String[] stringValues() {
        String[] arr = new String[values().length];

        for (int i = 0; i < values().length; i++) {
            arr[i] = values()[i].toString();
        }
        return arr;
    }
}
