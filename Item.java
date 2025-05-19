public class Item {
    private String name;
    private String description;
    private boolean isPickedUp;
    private boolean isUsable;
    private boolean isRanged;
    private boolean isMelee;    
    private boolean isMagic;    
    private int healthFX;
    private int attackFX;

    private int stealthFX;
    private String type;
    private int defenseFX;
    private int magicFX; 
    private int rangeFX;   
    private int enduranceFX;  
    
    public Item(String name, String description, boolean isUsable, boolean isRanged, boolean isMelee, boolean isMagic,
                int healthFX, int attackFX, int stealthFX, String type, int defenseFX, int magicFX, int rangeFX, int enduranceFX) {
        this.name = name;
        this.description = description;
        this.isUsable = isUsable;
        this.isRanged = isRanged;
        this.isMelee = isMelee;
        this.isMagic = isMagic;
        this.healthFX = healthFX;
        this.attackFX = attackFX;
        this.stealthFX = stealthFX;
        this.type = type;
        this.defenseFX = defenseFX;
        this.magicFX = magicFX;
        this.enduranceFX = enduranceFX;
    }

    public Item(String name, String description, boolean isUsable, boolean isRanged, boolean isMelee, boolean isMagic,
                int healthFX, int attackFX, int stealthFX, String type, int defenseFX, int magicFX, int enduranceFX) {
        this.name = name;
        this.description = description;
        this.isUsable = isUsable;
        this.isRanged = isRanged;
        this.isMelee = isMelee;
        this.isMagic = isMagic;
        this.healthFX = healthFX;
        this.attackFX = attackFX;
        this.stealthFX = stealthFX;
        this.type = type;
        this.defenseFX = defenseFX;
        this.magicFX = magicFX;
        this.enduranceFX = enduranceFX;
    }

    public Item(String name, String description, boolean isUsable, boolean isRanged, boolean isMelee, boolean isMagic,
                int healthFX, int attackFX, int stealthFX, String type, int defenseFX, int magicFX) {
        this.name = name;
        this.description = description;
        this.isUsable = isUsable;
        this.isRanged = isRanged;
        this.isMelee = isMelee;
        this.isMagic = isMagic;
        this.healthFX = healthFX;
        this.attackFX = attackFX;
        this.stealthFX = stealthFX;
        this.type = type;
        this.defenseFX = defenseFX;
        this.magicFX = magicFX;
    }

    public Item(String name, String description, boolean isUsable, boolean isRanged, int healthFX, int attackFX) {
        this(name, description, isUsable, isRanged, !isRanged, false, healthFX, attackFX, 0, "misc", 0, 0);
    }

    public Item(String name, String description, boolean isUsable, boolean isRanged, int healthFX, int attackFX, int stealthFX, String type) {
        this(name, description, isUsable, isRanged, !isRanged, false, healthFX, attackFX, stealthFX, type, 0, 0);
    }

    public Item(String name, String description, boolean isUsable, boolean isRanged, int healthFX, int attackFX, int stealthFX, String type, int defenseFX) {
        this(name, description, isUsable, isRanged, !isRanged, false, healthFX, attackFX, stealthFX, type, defenseFX, 0);
    }

    public Item(String name, String description, boolean isUsable, int healthFX, int attackFX) {
        this(name, description, isUsable, false, true, false, healthFX, attackFX, 0, "misc", 0, 0);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isUsable() { return isUsable; }
    public boolean isPickedUp() { return isPickedUp; }
    public void setPickedUp(boolean pickedUp) { isPickedUp = pickedUp; }
    public int getHealthFX() { return healthFX; }
    public int getAttackFX() { return attackFX; }
    public int getRangedFX() { return rangeFX; }
    public void setRangedFX(int rangeFX) { this.rangeFX = rangeFX; }
    public void setHealthFX(int healthFX) { this.healthFX = healthFX; }
    public void setAttackFX(int attackFX) { this.attackFX = attackFX; }
    public boolean isRanged() { return isRanged; }
    public void setRanged(boolean ranged) { isRanged = ranged; }
    public boolean isMelee() { return isMelee; }
    public void setMelee(boolean melee) { isMelee = melee; }
    public boolean isMagic() { return isMagic; }
    public void setMagic(boolean magic) { isMagic = magic; }
    public int getStealthFX() { return stealthFX; }
    public String getType() { return type; }
    public int getDefenseFX() { return defenseFX; }
    public void setDefenseFX(int defenseFX) { this.defenseFX = defenseFX; }
    public int getMagicFX() { return magicFX; }
    public void setMagicFX(int magicFX) { this.magicFX = magicFX; }
    public int getEnduranceFX() { return enduranceFX; }
    public void setEnduranceFX(int enduranceFX) { this.enduranceFX = enduranceFX; }
    
}
