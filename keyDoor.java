public class keyDoor {
    private boolean isKey;
    private boolean isDoor;
    private String keyType;
    private boolean locked;

    public keyDoor(boolean isKey, boolean isDoor, String keyType) {
        this(isKey, isDoor, keyType, isDoor);
    }

    public keyDoor(boolean isKey, boolean isDoor, String keyType, boolean locked) {
        this.isKey = isKey;
        this.isDoor = isDoor;
        this.keyType = keyType;
        this.locked = locked;
    }

    public boolean isKey() {
        return isKey;
    }

    public boolean isDoor() {
        return isDoor;
    }

    public String getKeyType() {
        return keyType;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        this.locked = false;
    }
}
