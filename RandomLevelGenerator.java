import com.google.gson.*;

import java.io.*;
import java.util.*;

public class RandomLevelGenerator {
    private JsonObject data;
    private int lastThemeIndex = -1;
    private Random rand = new Random();

    public RandomLevelGenerator(String jsonFile) throws IOException {
        try (Reader reader = new FileReader(jsonFile)) {
            data = JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public Main.LevelData generateLevel(int level) {
        JsonArray themes = data.getAsJsonArray("themes");
        int themeIdx;
        if (themes.size() <= 1) {
            themeIdx = 0;
        } else {
            do {
                themeIdx = rand.nextInt(themes.size());
            } while (themeIdx == lastThemeIndex);
        }
        lastThemeIndex = themeIdx;
        JsonObject theme = themes.get(themeIdx).getAsJsonObject();

        JsonArray roomsArr = theme.getAsJsonArray("rooms");
        int numRooms = Math.min(roomsArr.size(), 4 + rand.nextInt(5)); 
        List<Integer> chosenIndices = new ArrayList<>();
        while (chosenIndices.size() < numRooms) {
            int idx = rand.nextInt(roomsArr.size());
            if (!chosenIndices.contains(idx)) chosenIndices.add(idx);
        }

        List<Room> rooms = new ArrayList<>();
        for (int idx : chosenIndices) {
            JsonObject roomObj = roomsArr.get(idx).getAsJsonObject();
            String name = roomObj.get("name").getAsString();
            String desc = roomObj.get("description").getAsString();

            List<Entity> monsters = new ArrayList<>();
            for (JsonElement mName : roomObj.getAsJsonArray("monsters")) {
                Entity monster = createMonster(mName.getAsString(), level);
                if (monster != null) monsters.add(monster);
            }

            List<Item> items = new ArrayList<>();
            for (JsonElement iName : roomObj.getAsJsonArray("items")) {
                Item item = createItem(iName.getAsString(), level);
                if (item != null) items.add(item);
            }

            rooms.add(new Room(name, desc, items.toArray(new Item[0]), monsters.toArray(new Entity[0]), new Room[0]));
        }

        int maxExits = 4;
        Map<Room, Set<Room>> adjacency = new HashMap<>();
        for (Room r : rooms) adjacency.put(r, new HashSet<>());

        List<Room> unconnected = new ArrayList<>(rooms);
        List<Room> connected = new ArrayList<>();
        connected.add(unconnected.remove(0));
        while (!unconnected.isEmpty()) {
            Room from = connected.get(rand.nextInt(connected.size()));
            Room to = unconnected.remove(rand.nextInt(unconnected.size()));
            adjacency.get(from).add(to);
            adjacency.get(to).add(from);
            connected.add(to);
        }

        int extraConnections = numRooms + rand.nextInt(numRooms); 
        for (int i = 0; i < extraConnections; i++) {
            Room a = rooms.get(rand.nextInt(rooms.size()));
            Room b = rooms.get(rand.nextInt(rooms.size()));
            if (a != b && adjacency.get(a).size() < maxExits && adjacency.get(b).size() < maxExits) {
                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }
        }

        for (Room r : rooms) {
            r.setExits(adjacency.get(r).toArray(new Room[0]));
        }

        Main.LevelData data = new Main.LevelData();
        data.startingRoom = rooms.get(0);
        data.lockedDoors = new HashMap<>();
        data.requirement = new Main.EnterPortalRequirement();
        
        Room portalRoom = null;
        for (int i = 0; i < roomsArr.size(); i++) {
            JsonObject roomObj = roomsArr.get(i).getAsJsonObject();
            if (roomObj.get("name").getAsString().equalsIgnoreCase("Portal")) {
                List<Entity> monsters = new ArrayList<>();
                for (JsonElement mName : roomObj.getAsJsonArray("monsters")) {
                    Entity monster = createMonster(mName.getAsString(), level);
                    if (monster != null) monsters.add(monster);
                }
                List<Item> items = new ArrayList<>();
                for (JsonElement iName : roomObj.getAsJsonArray("items")) {
                    Item item = createItem(iName.getAsString(), level);
                    if (item != null) items.add(item);
                }
                portalRoom = new Room("Portal", roomObj.get("description").getAsString(),
                                      items.toArray(new Item[0]), monsters.toArray(new Entity[0]), new Room[0]);
                break;
            }
        }

        if (portalRoom != null) {
            Room connectFrom = rooms.get(rand.nextInt(rooms.size()));
            Room[] fromExits = connectFrom.getExits();
            Room[] newFromExits = Arrays.copyOf(fromExits, fromExits.length + 1);
            newFromExits[fromExits.length] = portalRoom;
            connectFrom.setExits(newFromExits);

            Room[] portalExits = portalRoom.getExits();
            Room[] newPortalExits = Arrays.copyOf(portalExits, portalExits.length + 1);
            newPortalExits[portalExits.length] = connectFrom;
            portalRoom.setExits(newPortalExits);
            data.lockedDoors.put(portalRoom, new keyDoor(false, true, "Portal Key", true));
        }

        data.portalRoom = portalRoom;

        Room secretMapRoom = rooms.get(rand.nextInt(rooms.size()));
        Room portalKeyRoom = rooms.get(rand.nextInt(rooms.size()));
        while (portalKeyRoom == secretMapRoom && rooms.size() > 1) {
            portalKeyRoom = rooms.get(rand.nextInt(rooms.size()));
        }

        Item secretMap = createItem("Secret Map", level);
        if (secretMap != null) {
            List<Item> items = new ArrayList<>(Arrays.asList(secretMapRoom.getItems()));
            items.add(secretMap);
            secretMapRoom.setItems(items.toArray(new Item[0]));
            System.out.println("[DEBUG] Secret Map spawned in: " + secretMapRoom.getName());
        }

        Item portalKey = createItem("Portal Key", level);
        if (portalKey != null) {
            List<Item> items = new ArrayList<>(Arrays.asList(portalKeyRoom.getItems()));
            items.add(portalKey);
            portalKeyRoom.setItems(items.toArray(new Item[0]));
            System.out.println("[DEBUG] Portal Key spawned in: " + portalKeyRoom.getName());
        }

        return data;
    }

    private Entity createMonster(String name, int level) {
        JsonArray monsters = data.getAsJsonArray("monsters");
        for (JsonElement elem : monsters) {
            JsonObject m = elem.getAsJsonObject();
            if (m.get("name").getAsString().equalsIgnoreCase(name)) {
                int health = m.get("baseHealth").getAsInt() + m.getAsJsonObject("scaling").get("health").getAsInt() * level;
                int attack = m.get("baseAttack").getAsInt() + m.getAsJsonObject("scaling").get("attack").getAsInt() * level;
                boolean canMelee = m.has("canMelee") && m.get("canMelee").getAsBoolean();
                boolean canRanged = m.has("canRanged") && m.get("canRanged").getAsBoolean();
                boolean canMagic = m.has("canMagic") && m.get("canMagic").getAsBoolean();
                int rangeAttack = m.has("rangeAttack") ? m.get("rangeAttack").getAsInt() : 0;
                int magicAttack = m.has("magicAttack") ? m.get("magicAttack").getAsInt() : 0;
                int initiative = m.has("initiative") ? m.get("initiative").getAsInt() : 0;
                boolean autoAttack = m.has("autoAttack") && m.get("autoAttack").getAsBoolean();
                List<Drop> drops = m.has("drops") ? createDrops(m.getAsJsonArray("drops")) : new ArrayList<>();
                System.out.println(drops);

                return new Entity(name, m.get("desc").getAsString(), true, false, health, attack, canMelee, canRanged, canMagic, rangeAttack, magicAttack, initiative, autoAttack, drops);
            }
        }
        return null;
    }

    public List<Drop> createDrops(JsonArray dropsJson) {
        List<Drop> drops = new ArrayList<>();
        for (JsonElement dropElement : dropsJson) {
            if (dropElement.isJsonPrimitive() && dropElement.getAsJsonPrimitive().isString()) {
                String itemName = dropElement.getAsString();
                double chance = 1.0; 
                Item item = createItem(itemName, chance); 
                drops.add(new Drop(item, chance));
            }
        }
        return drops;
    }


    private Item createItem(String name, double chance) {
        JsonArray items = data.getAsJsonArray("items");
        for (JsonElement elem : items) {
            JsonObject i = elem.getAsJsonObject();
            if (!i.has("name")) continue;

            if (i.get("name").getAsString().equalsIgnoreCase(name)) {
                int healthFX = i.has("healthFX") ? i.get("healthFX").getAsInt() : 0;
                int attackFX = i.has("attackFX") ? i.get("attackFX").getAsInt() : 0;
                int defenseFX = i.has("defenseFX") ? i.get("defenseFX").getAsInt() : 0;
                String type = i.has("type") ? i.get("type").getAsString() : "misc";
                return new Item(
                    name,
                    i.has("description") ? i.get("description").getAsString() : "",
                    true,
                    false,
                    healthFX,
                    attackFX,
                    0,
                    type,
                    defenseFX
                );
            }
        }
        return null;
    }
}
