package kyu.cities.Util;

public enum CityOption {

    PVP(trueOrFalse()), 
    JOIN_REQUIREMENT(EntryRequirement.stringValues()),
    MIN_CLAIM_RANK(CityRank.stringValues()),
    CAN_NEWCOMMERS_BREAK_BLOCKS(trueOrFalse()),
    CAN_CITY_COUNCIL_EDIT_OPTIONS(trueOrFalse());

    private String[] options;

    CityOption(String[] options) {
        this.options = options;
    }

    public boolean isValidValue(String value) {
        for (String s : options) {
            if (s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    public String[] getOptions() {
        return options;
    }

    private static String[] trueOrFalse() {
        return new String[]{"true", "false"};
    }
    
}
