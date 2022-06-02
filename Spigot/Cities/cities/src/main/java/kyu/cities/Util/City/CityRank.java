package kyu.cities.Util.City;

public enum CityRank {
    MAYOR(3), CITY_COUNCIL(2), FULL_MEMBER(1), NEW_MEMBER(0);

    private int val;

    CityRank(int val) {
        this.val = val;
    }
    
    public int getVal() {
        return val;
    }

    public static String[] stringValues() {
        String[] arr = new String[values().length];

        for (int i = 0; i < values().length; i++) {
            arr[i] = values()[i].toString();
        }
        return arr;
    }
}
