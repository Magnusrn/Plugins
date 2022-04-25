package net.runelite.client.plugins.oneclickzmi;

public enum EssenceType {
    Daeyalt(24704),
    Pure_Essence(7936);

    private final int ID;

    EssenceType(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return this.ID;
    }
}
