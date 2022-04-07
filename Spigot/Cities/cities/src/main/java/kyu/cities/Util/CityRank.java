package kyu.cities.Util;

public enum CityRank {
    MAYOR(3), CITY_COUNCIL(2), FULL_MEMBER(1), NEW_MEMBER(0);

    private int val;

    CityRank(int val) {
        this.val = val;
    }
    
    public int getVal() {
        return val;
    }
}
