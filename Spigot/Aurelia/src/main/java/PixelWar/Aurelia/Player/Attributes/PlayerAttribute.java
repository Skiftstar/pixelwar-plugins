package PixelWar.Aurelia.Player.Attributes;

public enum PlayerAttribute {
    VIT,
    ATK,
    DEF,
    AGI,
    MAN,
    CRR,
    CRD,
    INT;

    private int startValue;

    public static void fetchStartValues() {
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            //TODO: Fetch from DB
            attribute.setStartValue(1);
        }
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
    }

    public int getStartValue() {
        return startValue;
    }
}
