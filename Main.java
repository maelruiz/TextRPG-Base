import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.IOException;
import java.util.Random;

public class Main {

    static RandomLevelGenerator randomLevelGenerator;

    static class LevelData {
        Room startingRoom;
        Room portalRoom;
        java.util.HashMap<Room, keyDoor> lockedDoors;
        LevelRequirement requirement;
        boolean advanced;
    }

    // randomly generate levels
    private static LevelData loadLevel(int level) {
        if (randomLevelGenerator == null) {
            try {
                randomLevelGenerator = new RandomLevelGenerator("C:\\Users\\ruizg\\Documents\\IB CS Projects\\Java\\RPGRoom\\src\\room_blueprints.json");
            } catch (IOException e) {
                System.out.println("Failed to load random level generator: " + e.getMessage());
                return null;
            }
        }
        LevelData data = randomLevelGenerator.generateLevel(level);
        data.requirement = randomWinCondition(data);
        return data;
    }

    // generate a random win condition for the level
    private static LevelRequirement randomWinCondition(LevelData data) {
        Random rand = new Random();
        int pick = rand.nextInt(4);
        switch (pick) {
            case 0:
                // defeat all hostiles
                return new DefeatAllHostilesRequirement();
            case 1:
                // Enter portal
                return new EnterPortalRequirement();
            case 2:
                // defeat a random boss (random enemy from a random room) (make boss double as hard as the rest of the enemies)
                java.util.List<Entity> bosses = new java.util.ArrayList<>();
                for (Room r : getAllRooms(data.startingRoom, new java.util.HashSet<>())) {
                    for (Entity e : r.getCharacters()) {
                        if (e.isHostile()){
                            e.setAttack(e.getAttack() * 2);
                            e.setHealth(e.getHealth() * 2);
                            bosses.add(e);
                        }
                    }
                }
                if (!bosses.isEmpty()) {
                    Entity boss = bosses.get(rand.nextInt(bosses.size()));
                    return new DefeatBossRequirement(boss.getName());
                }
                return new DefeatAllHostilesRequirement();
            default:
                return new CompositeLevelRequirement(
                    new DefeatAllHostilesRequirement(),
                    new EnterPortalRequirement()
                );
        }
    }

    // a level requirement interface for specific win conditions to inherit
    interface LevelRequirement {
        boolean isMet(Player player, Room currentRoom, LevelData levelData);
        String getDescription();
    }
    // Win Condition (Enter Portal)
    static class EnterPortalRequirement implements LevelRequirement {
    public boolean isMet(Player player, Room currentRoom, LevelData levelData) {
        return currentRoom == levelData.portalRoom && levelData.advanced;
    }
    public String getDescription() {
        return "Enter the portal room and use 'advance' to proceed.";
    }
}
    // Win Condition (Defeat Boss)
    static class DefeatBossRequirement implements LevelRequirement {
        private final String bossName;
        DefeatBossRequirement(String bossName) {
            this.bossName = bossName;
        }
        public boolean isMet(Player player, Room currentRoom, LevelData levelData) {
            for (Room r : getAllRooms(levelData.startingRoom, new java.util.HashSet<>())) {
                for (Entity e : r.getCharacters()) {
                    if (e.getName().equalsIgnoreCase(bossName) && !e.isDefeated()) return false;
                }
            }
            return true;
        }
        public String getDescription() {
            return "Defeat the boss: " + bossName;
        }
    }
    // Win Condition (Defeat All Hostiles)
    static class DefeatAllHostilesRequirement implements LevelRequirement {
        public boolean isMet(Player player, Room currentRoom, LevelData levelData) {
            for (Room r : getAllRooms(levelData.startingRoom, new java.util.HashSet<>())) {
                for (Entity e : r.getCharacters()) {
                    if (e.isHostile() && !e.isDefeated()) return false;
                }
            }
            return true;
        }
        public String getDescription() {
            return "Defeat all hostile entities.";
        }
    }
    // allows the combination of two win conditions
    static class CompositeLevelRequirement implements LevelRequirement {
        private final LevelRequirement[] requirements;
        CompositeLevelRequirement(LevelRequirement... requirements) {
            this.requirements = requirements;
        }

        public boolean isMet(Player player, Room currentRoom, LevelData levelData) {
            for (LevelRequirement req : requirements) {
                if (!req.isMet(player, currentRoom, levelData)) return false;
            }
            return true;
        }
        public String getDescription() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < requirements.length; i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(requirements[i].getDescription());
            }
            return sb.toString();
        }
    }

    // lists all rooms by traversing through connections and adding them to a set (similar to a python tuple)
    private static java.util.Set<Room> getAllRooms(Room start, java.util.Set<Room> visited) {
        if (start == null || visited.contains(start)) return visited;
        visited.add(start);
        for (Room r : start.getExits()) getAllRooms(r, visited);
        return visited;
    }

    // prints a breadcrumb style "minimap" to make traversing rooms more accessible 
    private static void printMiniMap(Room currentRoom, java.util.Set<Room> visitedRooms) {
        System.out.println("\n\u001B[36mMinimap:\u001B[0m");
        for (Room r : visitedRooms) {
            String marker = (r == currentRoom) ? "\u001B[32m[You]\u001B[0m " : "      ";
            String exits = "";
            for (Room ex : r.getExits()) {
                exits += " -> " + ex.getName();
            }
            System.out.println(marker + r.getName() + exits);
        }
    }

    // Main game loop
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Player player = new Player();
        boolean playing = true;
        int level = 1;

        // Initialize the random level generator
        try {
            randomLevelGenerator = new RandomLevelGenerator("C:\\Users\\ruizg\\Documents\\IB CS Projects\\Java\\RPGRoom\\src\\room_blueprints.json");
        } catch (IOException e) {
            System.out.println("Warning: Could not load random level generator. No levels will be available.");
            randomLevelGenerator = null;
        }

        LevelData levelData = loadLevel(level);
        if (levelData == null) {
            System.out.println("No levels available. Exiting.");
            return;
        }
        Room currentRoom = levelData.startingRoom;
        java.util.HashMap<Room, keyDoor> lockedDoors = levelData.lockedDoors;
        java.util.Set<Room> visitedRooms = new java.util.HashSet<>();

        // Start game (with colors to enhance the readability)
        System.out.println("\u001B[35m" + "\n=====================================" + "\u001B[0m");
        System.out.println("\u001B[36mWelcome\u001B[0m");
        System.out.println("Type 'help' to see available commands.");
        System.out.println("\u001B[35m=====================================\u001B[0m");

        // choose class
        System.out.println("\u001B[33mChoose your class:\u001B[0m");
        player.choosepClass(scanner);

        while (playing && player.isAlive()) {

            System.out.println("\u001B[33mWin Condition: " + levelData.requirement.getDescription() + "\u001B[0m");

            System.out.println("\u001B[33m\n=== " + currentRoom.getName() + " ===\u001B[0m");
            System.out.println(currentRoom.getDescription());
            System.out.println("\u001B[32mHealth: " + player.getHealth() + "\u001B[0m");

            // print all items in a room
            Item[] items = currentRoom.getItems();
            boolean hasItems = false;
            System.out.println("\n\u001B[34mItems in the room:\u001B[0m");
            int itemDisplayIdx = 1;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) continue;
                if (!items[i].isPickedUp()) {
                    System.out.println("  (" + itemDisplayIdx + ") " + items[i].getName() + " - " + items[i].getDescription());
                    itemDisplayIdx++;
                }
            }
            if (itemDisplayIdx == 1)
                System.out.println("  (none)");

            // print all entities in a room
            Entity[] entities = currentRoom.getCharacters();
            boolean hasEntities = false;
            System.out.println("\n\u001B[31mEntities here:\u001B[0m");
            int entityDisplayIdx = 1;
            for (int i = 0; i < entities.length; i++) {
                if (!entities[i].isDefeated()) {
                    System.out.println("  (" + entityDisplayIdx + ") " + entities[i].getName() + " - " + entities[i].getDesc() +
                            (entities[i].isHostile() ? " [Hostile]" : ""));
                    entityDisplayIdx++;
                }
            }
            if (entityDisplayIdx == 1)
                System.out.println("  (none)");

            // print all exits
            Room[] exits = currentRoom.getExits();
            int visibleExitIdx = 1;

            System.out.println("\n\u001B[36mExits:\u001B[0m");
            for (int i = 0; i < exits.length; i++) {
            Room exit = exits[i];
            System.out.println("  (" + visibleExitIdx + ") " + exit.getName());
            visibleExitIdx++;
            }
            if (visibleExitIdx == 1) {
                System.out.println("  (none)");
            }

            // show command help
            System.out.println("\n\u001B[35mCommands: move [#], pickup [#], battle [#], inventory, help, quit, equip [item], unequip [item], throw [item]\u001B[0m");

            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            // Split chained commands by ; or &&
            String[] commandChains = input.split("\\s*;\\s*|\\s*&&\\s*");
            for (String cmd : commandChains) {
                if (cmd.isBlank()) continue;
                String[] parts = cmd.trim().split("\\s+");
                String command = parts[0];

                // map single letter commands to full commands for ease and reducing clutter
                switch (command) {
                    case "i": command = "inventory"; break;
                    case "e": command = "equip"; break;
                    case "u": command = "unequip"; break;
                    case "t": command = "throw"; break;
                    case "p": command = "pickup"; break;
                    case "b": command = "battle"; break;
                    case "m": command = "move"; break;
                    case "x": command = "inspect"; break;
                    case "s": command = "sort"; break;
                    case "h": command = "help"; break;
                    case "q": command = "quit"; break;
                    case "a": command = "advance"; break;
                }
                List<Item> invList = new ArrayList<>(player.getInventory());
                List<Item> equippedListFlat = player.getEquippedFlat();

                // command processing with full command name
                switch (command) {
                    // movement
                    case "move":
                        player.increaseIndurance(-1);

                        if (parts.length < 2) {
                            System.out.println("Specify which exit to take.");
                            break;
                        }
                        try {
                            int exitNum = Integer.parseInt(parts[1]) - 1;
                            if (exitNum < 0 || exitNum >= currentRoom.getExits().length) {
                                System.out.println("Invalid exit number.");
                                break;
                            }
                            Room nextRoom = currentRoom.getExits()[exitNum];

                            // check if door is locked
                            if (lockedDoors.containsKey(nextRoom)) {
                                keyDoor door = lockedDoors.get(nextRoom);
                                if (door.isLocked()) {
                                    boolean hasKey = false;
                                    for (Item i : player.getInventory()) {
                                        if (i.getName().equalsIgnoreCase("Portal Key")) {
                                            hasKey = true;
                                            break;
                                        }
                                    }
                                    if (hasKey) {
                                        System.out.println("You use the Portal Key to unlock the door!");
                                        door.unlock();
                                    } else {
                                        System.out.println("The door is locked. You need a Portal Key.");
                                        break;
                                    }
                                }
                            }
                            // finalize move to next room and print minimap
                            currentRoom = nextRoom;
                            visitedRooms.add(currentRoom);

                            // Start battle immediately for each autoattack entity
                            for (Entity e : currentRoom.getCharacters()) {
                                if (e.isHostile() && e.isAutoAttack() && e.isAlive()) {
                                    System.out.println(e.getName() + " catches you off guard and attacks!");
                                    int distance = 1; 
                                    boolean inBattle = true;
                                    java.util.Random rand = new java.util.Random();
                                    boolean playerFirst = player.getStealth() >= e.getInitiative();
                                    if (!playerFirst) {
                                        System.out.println(e.getName() + " strikes first!");
                                    }
                                    while (player.isAlive() && e.isAlive() && inBattle) {
                                        System.out.println("\nDistance to " + e.getName() + ": " + (distance == 3 ? "Far" : distance == 2 ? "Mid" : "Close"));
                                        System.out.println("Your health: " + player.getHealth() + " | " + e.getName() + " health: " + e.getHealth());
                                        System.out.println("Choose action: melee, ranged, magic, use, show inventory, move closer, move away, run");                                        String action = scanner.nextLine().trim().toLowerCase();
                                        boolean playerTurn = true;
                                        switch (action) {
                                            case "show":
                                            case "show inventory":
                                                invList = new ArrayList<>(player.getInventory());
                                                equippedListFlat = player.getEquippedFlat();
                                                Main.showInventorySummary(player, invList, equippedListFlat);
                                                break;
                                            case "melee":
                                                if (distance == 1) {
                                                    boolean hasMelee = false;
                                                    for (Item i : player.getInventory()) {
                                                        if (i.isMelee()) { hasMelee = true; break; }
                                                    }
                                                    if (hasMelee) {
                                                        int dmg = player.getMeleeAttack();
                                                        System.out.println("You strike with a melee attack for " + dmg + " damage!");
                                                        e.takeDamage(dmg);
                                                        playerTurn = false;
                                                    } else {
                                                        System.out.println("You have no melee weapon equipped!");
                                                    }
                                                } else {
                                                    System.out.println("You are too far for a melee attack!");
                                                }
                                                break;
                                            case "ranged":
                                                if (distance >= 2) {
                                                    boolean hasRanged = false;
                                                    for (Item i : player.getInventory()) {
                                                        if (i.isRanged()) { hasRanged = true; break; }
                                                    }
                                                    if (hasRanged) {
                                                        int dmg = player.getRangedAttack();
                                                        System.out.println("You fire a ranged attack for " + dmg + " damage!");
                                                        e.takeDamage(dmg);
                                                        playerTurn = false;
                                                    } else {
                                                        System.out.println("You have no ranged weapon equipped!");
                                                    }
                                                } else {
                                                    System.out.println("You are too close for a ranged attack!");
                                                }
                                                break;
                                            case "magic":
                                                boolean hasMagic = false;
                                                for (Item i : player.getInventory()) {
                                                    if (i.isMagic()) { hasMagic = true; break; }
                                                }
                                                if (hasMagic) {
                                                    int dmg = player.getMagicAttack();
                                                    System.out.println("You cast a magic attack for " + dmg + " damage!");
                                                    e.takeDamage(dmg);
                                                    playerTurn = false;
                                                } else {
                                                    System.out.println("You have no magic item equipped!");
                                                }
                                                break;
                                            case "move closer":
                                            case "closer":
                                                if (distance > 1) {
                                                    distance--;
                                                    System.out.println("You move closer to " + e.getName() + ".");
                                                    playerTurn = false;
                                                } else {
                                                    System.out.println("You are already at close range!");
                                                }
                                                break;
                                            case "move away":
                                            case "away":
                                                if (distance < 3) {
                                                    distance++;
                                                    System.out.println("You move farther from " + e.getName() + ".");
                                                    playerTurn = false;
                                                } else {
                                                    System.out.println("You are already at the farthest distance!");
                                                }
                                                break;
                                            case "run":
                                            case "escape":
                                                int escapeChance = distance * 35 + 10;
                                                if (rand.nextInt(100) < escapeChance) {
                                                    System.out.println("You successfully escaped!");
                                                    inBattle = false;
                                                    break;
                                                } else {
                                                    System.out.println("You failed to escape!");
                                                    playerTurn = false;
                                                }
                                                break;
                                            case "use":
                                                List<Item> usableItems = new ArrayList<>();
                                                for (int i = 0; i < player.getInventory().size(); i++) {
                                                    Item item = player.getInventory().get(i);
                                                    if (item.isUsable()) {
                                                        usableItems.add(item);
                                                        System.out.println("  (" + (usableItems.size()) + ") " + item.getName() + ": " + item.getDescription());
                                                    }
                                                }
                                                if (usableItems.isEmpty()) {
                                                    System.out.println("You have no usable items!");
                                                    break;
                                                }
                                                System.out.print("Which item to use? (number or 'cancel'): ");  
                                                String useInput = scanner.nextLine().trim().toLowerCase();
                                                if (useInput.equals("cancel")) break;
                                                try {
                                                    int useIdx = Integer.parseInt(useInput) - 1;
                                                    if (useIdx >= 0 && useIdx < usableItems.size()) {
                                                        Item toUse = usableItems.get(useIdx);
                                                        if (player.useItem(toUse.getName())) {
                                                            System.out.println("You used " + toUse.getName() + "!");
                                                            playerTurn = false;
                                                        } else {
                                                            System.out.println("Failed to use " + toUse.getName() + ".");
                                                        }
                                                    } else {
                                                        System.out.println("Invalid item number.");
                                                    }
                                                } catch (NumberFormatException ex) {
                                                    System.out.println("Invalid input.");
                                                }
                                                break;
                                            default:
                                                System.out.println("Unknown action. Try: melee, ranged, magic, move closer, move away, run");
                                        }
                                        if (!playerTurn && e.isAlive() && inBattle) {
                                            if (distance == 1 && e.canMelee()) {
                                                System.out.println(e.getName() + " attacks you for " + e.getAttack() + " damage!");
                                                player.takeDamage(e.getAttack());
                                            } else if (distance >= 2 && e.canRanged()) {
                                                System.out.println(e.getName() + " fires a ranged attack for " + e.getRangeAttack() + " damage!");
                                                player.takeDamage(e.getRangeAttack());
                                            } else if (e.canMagic()) {
                                                System.out.println(e.getName() + " casts a spell for " + e.getMagicAttack() + " damage!");
                                                player.takeDamage(e.getMagicAttack());
                                            } else {
                                                System.out.println(e.getName() + " moves closer!");
                                                distance--;
                                            }
                                        }
                                        if (!e.isAlive()) {
                                            List<Drop> drops = e.getDrops();
                                            for (Drop drop : drops) {
                                                if (Math.random() < drop.getChance()) {
                                                    currentRoom.addItem(drop.getItem());
                                                    System.out.println("You found a " + drop.getItem() + "!");
                                                }
                                            }
                                            e.setDefeated(true);
                                            System.out.println("You defeated " + e.getName() + "!");
                                        }
                                        if (!player.isAlive()) {
                                            System.out.println("You have been defeated!");
                                            playing = false;
                                            break;
                                        }
                                    }
                                    if (!playing || !player.isAlive()) break;
                                    if (levelData.requirement.isMet(player, currentRoom, levelData)) {
                                        System.out.println("\n\u001B[33mLevel complete! Requirement met: " + levelData.requirement.getDescription() + "\u001B[0m");
                                        level++;
                                        LevelData nextLevel = loadLevel(level);
                                        if (nextLevel == null) {
                                            System.out.println("You have beaten all available levels! Thanks for playing!");
                                            break;
                                        }
                                        currentRoom = nextLevel.startingRoom;
                                        levelData = nextLevel;
                                        lockedDoors = nextLevel.lockedDoors;
                                        visitedRooms.clear();
                                        visitedRooms.add(currentRoom);
                                        System.out.println("\n\u001B[31mYou have entered Level " + level + "!\u001B[0m");
                                        continue;
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid exit number.");
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;
                    case "pickup":
                    case "p":
                        player.increaseIndurance(-1);

                        List<Item> availableItems = new java.util.ArrayList<>();
                        for (Item item : items) {
                            if (!item.isPickedUp()) {
                                availableItems.add(item);
                            }
                        }
                        if (availableItems.isEmpty()) {
                            System.out.println("No items to pick up.");
                            break;
                        }
                        if (parts.length < 2) {
                            System.out.println("Usage: pickup [item number]");
                            break;
                        }
                        try {
                            int itemIndex = Integer.parseInt(parts[1]) - 1;
                            if (itemIndex >= 0 && itemIndex < availableItems.size()) {
                                Item picked = availableItems.get(itemIndex);
                                player.addItem(picked);
                                picked.setPickedUp(true);
                                System.out.println("You picked up the " + picked.getName() + "!");
                            } else {
                                System.out.println("Invalid item.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid item number.");
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;
                    case "battle":
                    case "b":
                        player.increaseIndurance(-1);

                        List<Entity> availableEntities = new java.util.ArrayList<>();
                        for (Entity entity : entities) {
                            if (!entity.isDefeated() && entity.isHostile()) {
                                availableEntities.add(entity);
                            }
                        }
                        if (availableEntities.isEmpty()) {
                            System.out.println("No one to battle here.");
                            break;
                        }
                        if (parts.length < 2) {
                            System.out.println("Usage: battle [entity number]");
                            break;
                        }
                        try {
                            int entityIndex = Integer.parseInt(parts[1]) - 1;
                            if (entityIndex >= 0 && entityIndex < availableEntities.size()) {
                                Entity enemy = availableEntities.get(entityIndex);
                                System.out.println("You engage in battle with " + enemy.getName() + "!");
                                int distance = 3;
                                boolean inBattle = true;
                                java.util.Random rand = new java.util.Random();
                                boolean playerFirst = player.getStealth() >= enemy.getInitiative();
                                if (!playerFirst) {
                                    System.out.println(enemy.getName() + " notices you first and attacks!");
                                    player.takeDamage(enemy.getAttack());
                                }
                                while (player.isAlive() && enemy.isAlive() && inBattle) {
                                    System.out.println("\nDistance to " + enemy.getName() + ": " + (distance == 3 ? "Far" : distance == 2 ? "Mid" : "Close"));
                                    System.out.println("Your health: " + player.getHealth() + " | " + enemy.getName() + " health: " + enemy.getHealth());
                                    System.out.println("Choose action: melee, ranged, magic, move closer, move away, run");
                                    System.out.print("> ");
                                    String action = scanner.nextLine().trim().toLowerCase();
                                    boolean playerTurn = true;
                                    switch (action) {
                                        case "melee":
                                            if (distance == 1) {
                                                boolean hasMelee = false;
                                                for (Item i : player.getInventory()) {
                                                    if (i.isMelee()) { hasMelee = true; break; }
                                                }
                                                if (hasMelee) {
                                                    int dmg = player.getMeleeAttack();
                                                    System.out.println("You strike with a melee attack for " + dmg + " damage!");
                                                    enemy.takeDamage(dmg);
                                                    playerTurn = false;
                                                } else {
                                                    System.out.println("You have no melee weapon equipped!");
                                                }
                                            } else {
                                                System.out.println("You are too far for a melee attack!");
                                            }
                                            break;
                                        case "ranged":
                                            if (distance >= 2) {
                                                boolean hasRanged = false;
                                                for (Item i : player.getInventory()) {
                                                    if (i.isRanged()) { hasRanged = true; break; }
                                                }
                                                if (hasRanged) {
                                                    int dmg = player.getRangedAttack();
                                                    System.out.println("You fire a ranged attack for " + dmg + " damage!");
                                                    enemy.takeDamage(dmg);
                                                    playerTurn = false;
                                                } else {
                                                    System.out.println("You have no ranged weapon equipped!");
                                                }
                                            } else {
                                                System.out.println("You are too close for a ranged attack!");
                                            }
                                            break;
                                        case "magic":
                                            boolean hasMagic = false;
                                            for (Item i : player.getInventory()) {
                                                if (i.isMagic()) { hasMagic = true; break; }
                                            }
                                            if (hasMagic) {
                                                int dmg = player.getMagicAttack();
                                                System.out.println("You cast a magic attack for " + dmg + " damage!");
                                                enemy.takeDamage(dmg);
                                                playerTurn = false;
                                            } else {
                                                System.out.println("You have no magic item equipped!");
                                            }
                                            break;
                                        case "move closer":
                                        case "closer":
                                            if (distance > 1) {
                                                distance--;
                                                System.out.println("You move closer to " + enemy.getName() + ".");
                                                playerTurn = false;
                                            } else {
                                                System.out.println("You are already at close range!");
                                            }
                                            break;
                                        case "move away":
                                        case "away":
                                            if (distance < 3) {
                                                distance++;
                                                System.out.println("You move farther from " + enemy.getName() + ".");
                                                playerTurn = false;
                                            } else {
                                                System.out.println("You are already at the farthest distance!");
                                            }
                                            break;
                                        case "run":
                                        case "escape":
                                            int escapeChance = distance * 35 + 10;
                                            if (rand.nextInt(100) < escapeChance) {
                                                System.out.println("You successfully escaped!");
                                                inBattle = false;
                                                break;
                                            } else {
                                                System.out.println("You failed to escape!");
                                                playerTurn = false;
                                            }
                                            break;
                                        case "use":
                                            List<Item> usableItems = new ArrayList<>();
                                            for (int i = 0; i < player.getInventory().size(); i++) {
                                                Item item = player.getInventory().get(i);
                                                if (item.isUsable()) {
                                                    usableItems.add(item);
                                                    System.out.println("  (" + (usableItems.size()) + ") " + item.getName() + ": " + item.getDescription());
                                                }
                                            }
                                            if (usableItems.isEmpty()) {
                                                System.out.println("You have no usable items!");
                                                break;
                                            }
                                            System.out.print("Which item to use? (number or 'cancel'): ");
                                            String useInput = scanner.nextLine().trim().toLowerCase();
                                            if (useInput.equals("cancel")) break;
                                            try {
                                                int useIdx = Integer.parseInt(useInput) - 1;
                                                if (useIdx >= 0 && useIdx < usableItems.size()) {
                                                    Item toUse = usableItems.get(useIdx);
                                                    if (player.useItem(toUse.getName())) {
                                                        System.out.println("You used " + toUse.getName() + "!");
                                                        playerTurn = false;
                                                    } else {
                                                        System.out.println("Failed to use " + toUse.getName() + ".");
                                                    }
                                                } else {
                                                    System.out.println("Invalid item number.");
                                                }
                                            } catch (NumberFormatException ex) {
                                                System.out.println("Invalid input.");
                                            }
                                            break;
                                        default:
                                            System.out.println("Unknown action. Try: melee, ranged, magic, move closer, move away, run");
                                    }
                                    if (enemy.isAlive() && inBattle) {
                                        boolean acted = false;
                                        if (distance == 1 && enemy.canMelee()) {
                                            System.out.println(enemy.getName() + " attacks you for " + enemy.getAttack() + " damage!");
                                            player.takeDamage(enemy.getAttack());
                                            acted = true;
                                        }
                                        else if (distance >= 2 && enemy.canRanged()) {
                                            System.out.println(enemy.getName() + " fires a ranged attack for " + enemy.getRangeAttack() + " damage!");
                                            player.takeDamage(enemy.getRangeAttack());
                                            acted = true;
                                        }
                                        else if (enemy.canMagic()) {
                                            System.out.println(enemy.getName() + " casts a spell for " + enemy.getMagicAttack() + " damage!");
                                            player.takeDamage(enemy.getMagicAttack());
                                            acted = true;
                                        }
                                        if (!acted) {
                                            if (enemy.canMelee() && distance > 1) {
                                                distance--;
                                                System.out.println(enemy.getName() + " moves closer!");
                                            } else if (enemy.canRanged() && distance < 2) {
                                                distance++;
                                                System.out.println(enemy.getName() + " moves farther away!");
                                            } else {
                                                System.out.println(enemy.getName() + " waits for an opportunity...");
                                            }
                                        }
                                    }
                                    if (!enemy.isAlive()) {
                                            List<Drop> drops = enemy.getDrops();
                                            System.out.println("drops:"+drops);
                                            for (Drop drop : drops) {
                                                if (Math.random() < drop.getChance()) {
                                                    currentRoom.addItem(drop.getItem());
                                                    System.out.println("You found a " + drop.getItem() + "!");
                                                }
                                            }
                                            enemy.setDefeated(true);
                                            System.out.println("You defeated " + enemy.getName() + "!");
                                        }
                                    if (!player.isAlive()) {
                                        System.out.println("You have been defeated!");
                                        playing = false;
                                        break;
                                    }
                                }
                            } else {
                                System.out.println("Invalid entity or not hostile.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid entity number.");
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        if (levelData.requirement.isMet(player, currentRoom, levelData)) {
                            System.out.println("\n\u001B[33mLevel complete! Requirement met: " + levelData.requirement.getDescription() + "\u001B[0m");
                            level++;
                            LevelData nextLevel = loadLevel(level);
                            if (nextLevel == null) {
                                System.out.println("You have beaten all available levels! Thanks for playing!");
                                break;
                            }
                            currentRoom = nextLevel.startingRoom;
                            levelData = nextLevel;
                            lockedDoors = nextLevel.lockedDoors;
                            visitedRooms.clear();
                            visitedRooms.add(currentRoom);
                            System.out.println("\n\u001B[31mYou have entered Level " + level + "!\u001B[0m");
                            continue;
                        }
                        break;
                    case "inventory":
                    case "i":
                        // Category filtering by type
                        if (parts.length > 1) {
                            String filterType = parts[1];
                            System.out.println("\n\u001B[32mYour inventory (" + filterType + "):\u001B[0m");
                            int idx = 1;
                            for (Item item : invList) {
                                if (item.getType().equalsIgnoreCase(filterType)) {
                                    System.out.println("  (" + idx + ") " + item.getName() + ": " + item.getDescription());
                                }
                                idx++;
                            }
                            System.out.println("Type 'inventory' to see all items.");
                            break;
                        }
                        showInventorySummary(player, invList, equippedListFlat);

                        System.out.println("\n\u001B[32mEndurance: " + player.getEndurance() + "\u001B[0m");

                        break;

                    case "inspect":
                        if (parts.length < 2) {
                            System.out.println("Usage: inspect [item number]");
                            break;
                        }
                        try {
                            int inspectIdx = Integer.parseInt(parts[1]) - 1;
                            if (inspectIdx < 0 || inspectIdx >= invList.size()) {
                                System.out.println("Invalid item number.");
                                break;
                            }
                            Item toInspect = invList.get(inspectIdx);
                            System.out.println("\n\u001B[36m" + toInspect.getName() + "\u001B[0m");
                            System.out.println("Type: " + toInspect.getType());
                            System.out.println("Description: " + toInspect.getDescription());
                            if (toInspect.getAttackFX() != 0) System.out.println("Attack: " + (toInspect.getAttackFX() > 0 ? "+" : "") + toInspect.getAttackFX());
                            if (toInspect.getStealthFX() != 0) System.out.println("Stealth: " + (toInspect.getStealthFX() > 0 ? "+" : "") + toInspect.getStealthFX());
                            if (toInspect.getHealthFX() != 0) System.out.println("Health: " + (toInspect.getHealthFX() > 0 ? "+" : "") + toInspect.getHealthFX());
                            System.out.println("Usable: " + (toInspect.isUsable() ? "Yes" : "No"));
                            System.out.println("Ranged: " + (toInspect.isRanged() ? "Yes" : "No"));
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid item number.");
                        }
                        break;

                    case "equip":
                        if (parts.length < 2) {
                            System.out.println("Usage: equip [item number|last]");
                            break;
                        }
                        int equipIdx = -1;
                        if (parts[1].equals("last")) {
                            Item last = player.getLastItem();
                            if (last == null) {
                                System.out.println("No items to equip.");
                                break;
                            }
                            equipIdx = invList.indexOf(last);
                        } else {
                            try {
                                equipIdx = Integer.parseInt(parts[1]) - 1;
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid item number.");
                                break;
                            }
                        }
                        if (equipIdx < 0 || equipIdx >= invList.size()) {
                            System.out.println("Invalid item number.");
                            break;
                        }
                        Item itemToEquip = invList.get(equipIdx);
                        System.out.println("Equipping " + itemToEquip.getName() + ":");
                        if (!scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                            System.out.println("Cancelled.");
                            break;
                        }
                        if (player.equipItem(itemToEquip)) {
                            System.out.println("You equipped the " + itemToEquip.getName() + ".");
                            if (itemToEquip.getName().equalsIgnoreCase("Secret Map")) {
                                System.out.println("\u001B[36mThe Secret Map reveals a hidden passage to the portal room!\u001B[0m");
                            }
                        } else {
                            System.out.println("You can't equip more of this type or it's already equipped.");
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;

                    case "unequip":
                        if (parts.length < 2) {
                            System.out.println("Usage: unequip [item number]");
                            break;
                        }
                        try {
                            int unequipIdx = Integer.parseInt(parts[1]) - 1;
                            if (unequipIdx < 0 || unequipIdx >= equippedListFlat.size()) {
                                System.out.println("Invalid equipped item number.");
                                break;
                            }
                            Item toUnequip = equippedListFlat.get(unequipIdx);
                            System.out.print("Are you sure you want to unequip " + toUnequip.getName() + "? (y/n): ");
                            if (!scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                                System.out.println("Cancelled.");
                                break;
                            }
                            if (player.unequipItem(toUnequip.getName())) {
                                System.out.println("You unequipped the " + toUnequip.getName() + ".");
                            } else {
                                System.out.println("You don't have that item equipped.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid equipped item number.");
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;

                    case "throw":
                    case "drop":
                        if (parts.length < 2) {
                            System.out.println("Usage: throw [item number(s)]");
                            break;
                        }
                        for (int i = 1; i < parts.length; i++) {
                            try {
                                int throwIdx = Integer.parseInt(parts[i]) - 1;
                                if (throwIdx < 0 || throwIdx >= invList.size()) {
                                    System.out.println("Invalid item number: " + parts[i]);
                                    continue;
                                }
                                Item toThrow = invList.get(throwIdx);
                                if (player.isRare(toThrow) || equippedListFlat.contains(toThrow)) {
                                    System.out.print("Are you sure you want to throw away " + toThrow.getName() + "? (y/n): ");
                                    if (!scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                                        System.out.println("Cancelled.");
                                        continue;
                                    }
                                }
                                if (player.removeItemByName(toThrow.getName())) {
                                    System.out.println("You threw away the " + toThrow.getName() + ".");
                                } else {
                                    System.out.println("You don't have that item.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid item number.");
                            }
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;

                    case "use":
                        if (parts.length < 2) {
                            System.out.println("Usage: use [item number|last|all potions]");
                            break;
                        }
                        if (parts[1].equals("last")) {
                            Item last = player.getLastItem();
                            if (last == null) {
                                System.out.println("No items to use.");
                                break;
                            }
                            player.useItem(last.getName());
                            break;
                        }
                        if (parts[1].equals("all") && parts.length > 2) {
                            String type = parts[2];
                            for (Item item : new java.util.ArrayList<>(invList)) {
                                if (item.getType().equalsIgnoreCase(type)) {
                                    player.useItem(item.getName());
                                }
                            }
                            break;
                        }
                        for (int i = 1; i < parts.length; i++) {
                            try {
                                int useIdx = Integer.parseInt(parts[i]) - 1;
                                if (useIdx < 0 || useIdx >= invList.size()) {
                                    System.out.println("Invalid item number: " + parts[i]);
                                    continue;
                                }
                                Item toUse = invList.get(useIdx);
                                player.useItem(toUse.getName());
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid item number.");
                            }
                        }
                        showInventorySummary(player, invList, equippedListFlat);
                        break;

                    case "sort":
                        if (parts.length < 3 || !parts[1].equals("inventory")) {
                            System.out.println("Usage: sort inventory [name|type|effect]");
                            break;
                        }
                        String sortBy = parts[2];
                        if (sortBy.equals("name")) {
                            invList.sort(java.util.Comparator.comparing(Item::getName));
                        } else if (sortBy.equals("type")) {
                            invList.sort(java.util.Comparator.comparing(Item::getType));
                        } else if (sortBy.equals("effect")) {
                            invList.sort(java.util.Comparator.comparingInt(i -> i.getAttackFX() + i.getStealthFX() + i.getHealthFX()));
                        } else {
                            System.out.println("Unknown sort type.");
                            break;
                        }
                        System.out.println("Inventory sorted by " + sortBy + ".");
                        showInventorySummary(player, invList, equippedListFlat);
                        break;

                    case "help":
                    case "h":
                        System.out.println("\n\u001B[35m=== Help: RPGRoom Commands ===\u001B[0m");
                        System.out.println("General Commands:");
                        System.out.println("  move [#]           - Move to an exit by its number.");
                        System.out.println("  pickup [#]         - Pick up an item in the room by its number.");
                        System.out.println("  battle [#]         - Battle a hostile entity by its number.");
                        System.out.println("  inventory [type]   - Show your inventory. Add a type to filter (e.g., 'inventory potion').");
                        System.out.println("  inspect [#]        - Show details for an item in your inventory.");
                        System.out.println("  equip [#|last]     - Equip an item by its inventory number or the last picked up item.");
                        System.out.println("  unequip [#]        - Unequip an equipped item by its equipped number.");
                        System.out.println("  throw [# ...]      - Throw away one or more items by their inventory numbers.");
                        System.out.println("  use [#|last|all type] - Use a consumable item by its number, 'last', or all of a type (e.g., 'use all potion').");
                        System.out.println("  sort inventory [name|type|effect] - Sort your inventory alphabetically, by type, or by stat effect.");
                        System.out.println("  advance            - Enter the portal to the next level.");
                        System.out.println("  help               - Show this help message.");
                        System.out.println("  quit               - Quit the game.");
                        System.out.println();
                        System.out.println("Battle Commands (when in combat):");
                        System.out.println("  melee              - Attack with a melee weapon (close range only).");
                        System.out.println("  ranged             - Attack with a ranged weapon (mid/far range).");
                        System.out.println("  magic              - Attack with a magic item (any range, if available).");
                        System.out.println("  move closer        - Move one step closer to the enemy.");
                        System.out.println("  move away          - Move one step farther from the enemy.");
                        System.out.println("  run                - Attempt to escape from battle.");
                        System.out.println("  use                - Use a consumable item (e.g., potion) during battle.");
                        System.out.println("  show inventory     - View your inventory and equipped items during battle.");
                        System.out.println();
                        System.out.println("Tips:");
                        System.out.println("  - You can chain commands with ';' or '&&' (e.g., 'pickup 1; equip 1').");
                        System.out.println("  - Use 'inspect [#]' to see item stats and effects.");
                        System.out.println("  - Some rooms or doors require special items to access.");
                        System.out.println("  - Equipping items can boost your stats, but you can only equip a limited number of each type.");
                        System.out.println("  - Use consumables wisely during tough battles");
                        System.out.println("  - Type 'inventory' often to keep track of your items and stats.");
                        System.out.println("  - You can use the 'advance' command to enter the portal to the next level.");
                        System.out.println("  - After picking up items, type 'inventory' to see the updated inventory list.");
                        System.out.println("\u001B[35m==============================\u001B[0m");
                        break;
                    case "quit":
                    case "q":
                        playing = false;
                        System.out.println("Thank you for playing");
                        break;
                    case "advance":
                    case "portal":
                        player.increaseIndurance(-1);
                        if (currentRoom == levelData.portalRoom) {
                            if (!levelData.advanced) {
                                System.out.println("You step into the swirling portal...");
                                levelData.advanced = true;
                            } else {
                                System.out.println("You have already activated the portal.");
                            }
                        } else {
                            System.out.println("You need to be in the portal room to advance.");
                        }
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
                if (!playing || !player.isAlive()) break;
            }
            
            printMiniMap(currentRoom, visitedRooms);

            if (levelData.requirement.isMet(player, currentRoom, levelData)) {
                System.out.println("\n\u001B[33mLevel complete! Requirement met: " + levelData.requirement.getDescription() + "\u001B[0m");
                level++;
                player.setEndurance((int) (player.getEndurance()*1.5));
                LevelData nextLevel = loadLevel(level);
                if (nextLevel == null) {
                    System.out.println("You have beaten all available levels! Thanks for playing!");
                    break;
                }
                currentRoom = nextLevel.startingRoom;
                levelData = nextLevel;
                lockedDoors = nextLevel.lockedDoors;
                visitedRooms.clear();
                visitedRooms.add(currentRoom);
                System.out.println("\n\u001B[31mYou have entered Level " + level + "!\u001B[0m");
                continue;
            }
            if (player.getEndurance() <= 0) {
                System.out.println("\n\u001B[31mYou have run out of stamina, falling to your knees as you collapse. You are at the mercy of the enemies.\u001B[0m");
                break;
            }
        }
        if (!player.isAlive()) {
            System.out.println("Game Over");
        }
        scanner.close();
    }

    public Item getLastItem(Player player) {
        if (player.getInventory().isEmpty()) return null;
        return player.getInventory().get(player.getInventory().size() - 1);
    }

    public Item getBestGearOfType(String type, Player player) {
        Item best = null;
        for (Item i : player.getInventory()) {
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
        return item.getName().toLowerCase().contains("ring") ||
               item.getName().toLowerCase().contains("amulet") ||
               item.getName().toLowerCase().contains("crown") ||
               item.getName().toLowerCase().contains("philosopher");
    }

    static void showInventorySummary(Player player, List<Item> invList, List<Item> equippedListFlat) {
        System.out.println("\n\u001B[32mInventory (" + invList.size() + "/20):\u001B[0m");
        for (int i = 0; i < invList.size(); i++) {
            Item item = invList.get(i);
            String stat = "";
            String tags = "";
            if (item.isMelee()) tags += " [Melee]";
            if (item.isRanged()) tags += " [Ranged]";
            if (item.isMagic()) tags += " [Magic]";
            if (item.getAttackFX() != 0) stat += " ATK:" + item.getAttackFX();
            if (item.getStealthFX() != 0) stat += " STL:" + item.getStealthFX();
            if (item.getHealthFX() != 0) stat += " HP:" + item.getHealthFX();
            if (item.getDefenseFX() != 0) stat += " DEF:" + item.getDefenseFX();
            System.out.println("  (" + (i+1) + ") " + item.getName() + " [" + item.getType() + "]" + tags + stat);        }
        if (invList.isEmpty()) System.out.println("  (empty)");
        System.out.println("Equipped:");
        for (int i = 0; i < equippedListFlat.size(); i++) {
            Item item = equippedListFlat.get(i);
            String stat = "";
            if (item.getAttackFX() != 0) stat += " ATK:" + item.getAttackFX();
            if (item.getStealthFX() != 0) stat += " STL:" + item.getStealthFX();
            if (item.getHealthFX() != 0) stat += " HP:" + item.getHealthFX();
            if (item.getDefenseFX() != 0) stat += " DEF:" + item.getDefenseFX();
            System.out.println("  (" + (i+1) + ") " + item.getName() + " [" + item.getType() + "]" + stat);
        }
        if (equippedListFlat.isEmpty()) System.out.println("  (none)");
        System.out.println(
            "Health: " + player.getHealth() + "/" + player.getMaxHealth() +
            " | Melee: " + player.getMeleeAttack() +
            " | Ranged: " + player.getRangedAttack() +
            " | Magic: " + player.getMagicAttack() +
            " | Defense: " + player.getDefense() +
            " | Stealth: " + player.getStealth()
        );
        System.out.println("Type 'inspect [#]', 'equip [#]', 'use [#]', 'throw [#]', or 'help' for options.");

    }
}