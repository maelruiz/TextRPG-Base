import java.util.*;

public class Player {
    private int health = 30;
    private int maxHealth = 30; 
    private int attack = 5; 
    private int rangedBase = 0; 
    private int magicBase = 0;  
    private int stealth = 0;
    private int defense = 0;
    private String pClass = "";
    private static final int INVENTORY_CAPACITY = 20;
    private ArrayList<Item> inventory = new ArrayList<>();
    int endurance = 20;
    private int perception = 0;

    private static final Map<String, Integer> TYPE_LIMITS = Map.of(
        "cloak", 1,
        "boots", 1,
        "book", 2,
        "weapon", 1,
        "map", 1,
        "amulet", 1,
        "ring", 2
    );

    private Map<String, List<Item>> equipped = new HashMap<>();

    public Player() {}

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; } 
    public int getAttack() { return attack; }
    public int getStealth() { return stealth; }
    public int getDefense() { return defense; }
    public ArrayList<Item> getInventory() { return inventory; }
    public Map<String, List<Item>> getEquipped() { return equipped; }
    public int getInventoryCapacity() { return INVENTORY_CAPACITY; }
    public String getpClass() { return pClass; }
    public void setpClass(String pClass) { this.pClass = pClass; }
    public void applypClassFX(String pClass) {
        switch (pClass) {
            case "Warrior":
                attack = (int)(attack + 5);
                break;
            case "Archer":
                rangedBase = (int)(rangedBase + 5);
                break;
            case "Mage":
                magicBase = (int)(magicBase + 5);
                break;
    }}
    public void setEndurance(int endurance) { this.endurance = endurance; }
    public int getEndurance() { return endurance; }
    public void increaseIndurance(int amount) { this.endurance += amount; }
    public void setPerception(int perception) { this.perception = perception; }
    public int getPerception() { return perception; }
    public void increasePerception(int amount) { this.perception += amount; }   
    public int rollPerception() {
        return (int)(Math.random() * 20) + 1 + perception; 
    }

    public void takeDamage(int dmg) { 
        int reduced = Math.max(1, dmg - defense);
        health -= reduced;
        System.out.println("Your defense absorbs " + (dmg - reduced) + " damage (" + reduced + " taken)");
    }
    public boolean isAlive() { return health > 0; }
    public void setHealth(int health) { 
        this.health = Math.min(health, maxHealth);
    }
    public void setAttack(int attack) { this.attack = attack; }

    public int getMeleeAttack() {
        int total = attack;
        for (List<Item> items : equipped.values()) {
            for (Item i : items) {
                if (i.isMelee()) total += i.getAttackFX();
            }
        }
        return total;
    }

    public int getRangedAttack() {
        int total = rangedBase;
        for (List<Item> items : equipped.values()) {
            for (Item i : items) {
                if (i.isRanged()) total += i.getAttackFX();
            }
        }
        return total;
    }

    public int getMagicAttack() {
        int total = magicBase;
        for (List<Item> items : equipped.values()) {
            for (Item i : items) {
                if (i.isMagic()) total += i.getAttackFX() + i.getMagicFX();
            }
        }
        return total;
    }

    public void addItem(Item item) {
        if (inventory.size() >= INVENTORY_CAPACITY) {
            System.out.println("Your inventory is full!");
            return;
        }
        inventory.add(item);
    }

    public boolean useItem(String name) {
        Iterator<Item> it = inventory.iterator();
        while (it.hasNext()) {
            Item i = it.next();
            if (i.getName().equalsIgnoreCase(name)) {
                if (i.getType().equals("potion") || i.getType().equals("food") || i.getType().equals("scroll")) {
                    if (i.getHealthFX() > 0) {
                        int heal = i.getHealthFX();
                        if (heal > 0) {
                            setHealth(this.health + heal);
                            System.out.println("You used " + i.getName() + " and healed " + heal + " HP!");
                        } else {
                            System.out.println("You used " + i.getName() + ".");
                        }
                    } 
                    if (i.getEnduranceFX() > 0) {
                        increaseIndurance(i.getEnduranceFX());
                        System.out.println("You used " + i.getName() + " and recovered " + i.getEnduranceFX() + " endurance!");
                    } else {
                        System.out.println("Not health");
                    }
                }
                    it.remove();
                    return true;
                } else {
                    System.out.println("You can't use that item directly.");
                    return false;
                }
            }
        System.out.println("You don't have that item.");
        return false;
    }

    public boolean equipItem(Item item) {
        String type = item.getType();
        if (type.equals("potion") || type.equals("food")) {
            System.out.println("You can't equip consumables!");
            return false;
        }
        int limit = TYPE_LIMITS.getOrDefault(type, 99);
        equipped.putIfAbsent(type, new ArrayList<>());
        List<Item> equippedOfType = equipped.get(type);
        if (equippedOfType.contains(item)) return false;  
        if (equippedOfType.size() >= limit) return false; 
        equippedOfType.add(item);
        applyItemEffects(item, true);
        return true;
    }

    public boolean unequipItem(String name) {
        for (List<Item> items : equipped.values()) {
            Iterator<Item> it = items.iterator();
            while (it.hasNext()) {
                Item i = it.next();
                if (i.getName().equalsIgnoreCase(name)) {
                    applyItemEffects(i, false);
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeItemByName(String name) {
        Iterator<Item> it = inventory.iterator();
        while (it.hasNext()) {
            Item i = it.next();
            if (i.getName().equalsIgnoreCase(name)) {
                unequipItem(name);
                it.remove();
                return true;
            }
        }
        return false;
    }

    private void applyItemEffects(Item item, boolean apply) {
        int mult = apply ? 1 : -1;
        this.stealth += mult * item.getStealthFX();
        this.attack += mult * item.getAttackFX();
        this.maxHealth += mult * item.getHealthFX(); 
        this.defense += mult * item.getDefenseFX();
        this.magicBase += mult * item.getMagicFX();
        this.rangedBase += mult * item.getRangedFX();
        this.endurance += mult * item.getEnduranceFX();
        if (this.health > this.maxHealth) this.health = this.maxHealth;
    }

    public Item getLastItem() {
        if (inventory.isEmpty()) return null;
        return inventory.get(inventory.size() - 1);
    }

    public Item getBestGearOfType(String type) {
        Item best = null;
        for (Item i : inventory) {
            if (i.getType().equals(type)) {
                if (best == null || i.getAttackFX() + i.getStealthFX() + i.getHealthFX() >
                    best.getAttackFX() + best.getStealthFX() + best.getHealthFX()) {
                    best = i;
                }
            }
        }
        return best;
    }

    public boolean isRare(Item item) {
        String n = item.getName().toLowerCase();
        return n.contains("ring") || n.contains("amulet") || n.contains("crown") || n.contains("philosopher");
    }

    public List<Item> getEquippedFlat() {
        List<Item> flat = new ArrayList<>();
        for (List<Item> list : equipped.values()) flat.addAll(list);
        return flat;
    }

    public List<Item> getInventoryByType(String type) {
        List<Item> filtered = new ArrayList<>();
        for (Item i : inventory) {
            if (i.getType().equalsIgnoreCase(type)) filtered.add(i);
        }
        return filtered;
    }

    public void choosepClass(Scanner scanner) {
        boolean valid = false;
        System.out.println("Choose your class: ");
        System.out.println("1. Warrior");
        System.out.println("2. Mage");
        System.out.println("3. Archer");
        System.out.print("Enter your choice: ");
        while (!valid) {
            int choice = scanner.nextInt();
            if (choice == 1) {pClass = "Warrior"; setpClass(pClass); valid = true; applypClassFX(pClass);}
            else if (choice == 2) {pClass = "Mage"; setpClass(pClass); valid = true; applypClassFX(pClass);}
            else if (choice == 3) {pClass = "Archer"; setpClass(pClass); valid = true; applypClassFX(pClass);}
            else {
                System.out.println("Invalid choice. Please enter a valid number: ");
            }
        }
    }
}
