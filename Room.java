public class Room {
    private String name;
    private String description;
    private Item[] items;
    private Entity[] characters;
    private Room[] exits;
    private boolean discovered = false;

    public Room(String name, String description, Item[] items, Entity[] characters, Room[] exits) {
        this.name = name;
        this.description = description;
        this.items = items;
        this.characters = characters;
        this.exits = exits;
    }

    public void markDiscovered() {
        this.discovered = true;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Item[] getItems() {
        return items;
    }

    public Entity[] getCharacters() {
        return characters;
    }

    public Room[] getExits() {
        return exits;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }

    public void setCharacters(Entity[] characters) {
        this.characters = characters;
    }

    public void setExits(Room[] exits) {
        this.exits = exits;
    }

    public void addItem(Item item) {
        Item[] newItems = new Item[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, items.length);
        newItems[items.length] = item;
        items = newItems;
    }

    public void removeItem(Item item) {
        Item[] newItems = new Item[items.length - 1];
        int index = 0;
        for (Item i : items) {
            if (!i.equals(item)) {
                newItems[index++] = i;
            }
        }
        items = newItems;
    }

    public void addCharacter(Entity character) {
        Entity[] newCharacters = new Entity[characters.length + 1];
        System.arraycopy(characters, 0, newCharacters, 0, characters.length);
        newCharacters[characters.length] = character;
        characters = newCharacters;
    }

    public void removeCharacter(Entity character) {
        Entity[] newCharacters = new Entity[characters.length - 1];
        int index = 0;
        for (Entity c : characters) {
            if (!c.equals(character)) {
                newCharacters[index++] = c;
            }
        }
        characters = newCharacters;
    }

    public void addExit(Room exit) {
        Room[] newExits = new Room[exits.length + 1];
        System.arraycopy(exits, 0, newExits, 0, exits.length);
        newExits[exits.length] = exit;
        exits = newExits;
    }

    public void removeExit(Room exit) {
        Room[] newExits = new Room[exits.length - 1];
        int index = 0;
        for (Room r : exits) {
            if (!r.equals(exit)) {
                newExits[index++] = r;
            }
        }
        exits = newExits;
    }

    public void printDetails() {
        System.out.println("Room Name: " + name);
        System.out.println("Description: " + description);
        System.out.println("Items: ");
        for (Item item : items) {
            System.out.println("- " + item.getName() + ": " + item.getDescription());
        }
        System.out.println("Characters: ");
        for (Entity character : characters) {
            System.out.println("- " + character.getName() + ": " + character.getDesc());
        }
        System.out.println("Exits: ");
        for (Room exit : exits) {
            System.out.println("- " + exit.getName());
        }
    }
}
