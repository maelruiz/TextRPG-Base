import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String name;
    private String desc;
    private boolean hostile;
    private boolean defeated;
    private int health;
    private int attack;
    private int rangeAttack;
    private int magicAttack;
    private boolean canMelee;
    private boolean canRanged;
    private boolean canMagic;
    private int initiative;
    private boolean autoAttack;
    private List<Drop> drops;

    public Entity(String name, String desc, boolean hostile, boolean defeated, int health, int attack, boolean canMelee, boolean canRanged, boolean canMagic, int rangeAttack, int magicAttack, int initiative, boolean autoattack, List<Drop> drops) {
        this.name = name;
        this.desc = desc;
        this.hostile = hostile;
        this.defeated = defeated;
        this.health = health;
        this.attack = attack;
        this.canMelee = canMelee;
        this.canRanged = canRanged;
        this.canMagic = canMagic;
        this.rangeAttack = rangeAttack;
        this.magicAttack = magicAttack;
        this.initiative = initiative;
        this.autoAttack = autoattack;
        this.drops = drops;


    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isHostile() {
        return hostile;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public int getHealth() {
        return health;
    }

    public int getAttack() {
        return attack;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public void takeDamage(int dmg) {
        this.health -= dmg;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean canRanged() {
        return canRanged;
    }

    public boolean canMagic() {
        return canMagic;
    }

    public boolean canMelee() {
        return canMelee;
    }

    public int getRangeAttack() {
        return rangeAttack;
    }

    public int getMagicAttack() {
        return magicAttack;
    }
    
    public int getInitiative() {
        return initiative;
    }

    public boolean isAutoAttack() {
        return autoAttack;
    }

    public List<Drop> getDrops() {
        return this.drops;
    }

    public void addDrop(Drop drop) {
        this.drops.add(drop);
    }
}


