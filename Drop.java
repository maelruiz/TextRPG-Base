public class Drop {
    public Item item;
    public double chance;

    public Drop(Item item, double chance) {
        this.item = item;
        this.chance = chance;
    }

        public Item getItem() {
            return this.item;
        }

        public double getChance() {
            return this.chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }
    }