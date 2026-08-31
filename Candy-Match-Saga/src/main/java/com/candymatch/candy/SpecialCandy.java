package com.candymatch.candy;

/**
 * Utility model for special candy bonus calculation and metadata.
 */
public class SpecialCandy {
    private final SpecialCandyType type;
    private final int bonusPoints;

    public SpecialCandy(SpecialCandyType type) {
        this.type = type;
        switch (type) {
            case STRIPED_HORIZONTAL:
            case STRIPED_VERTICAL:
                this.bonusPoints = 60;
                break;
            case WRAPPED:
                this.bonusPoints = 100;
                break;
            case COLOR_BOMB:
                this.bonusPoints = 200;
                break;
            default:
                this.bonusPoints = 0;
                break;
        }
    }

    public SpecialCandyType getType() {
        return type;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }
}
