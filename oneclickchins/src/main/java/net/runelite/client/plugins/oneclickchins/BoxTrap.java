package net.runelite.client.plugins.oneclickchins;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemSpawned;

import java.time.Duration;
import java.time.Instant;

public class BoxTrap {
    @Getter
    private final WorldPoint worldLocation;
    @Getter
    private TileItem tileItem;
    @Getter
    @Setter
    private GameObject gameObject;
    @Getter
    private Instant placedOn;
    @Getter
    @Setter
    private net.runelite.client.plugins.oneclickchins.BoxTrap.State state;

    BoxTrap(GameObject gameObject)
    {
        this.state = net.runelite.client.plugins.oneclickchins.BoxTrap.State.OPEN;
        this.placedOn = Instant.now();
        this.worldLocation = gameObject.getWorldLocation();
        this.gameObject = gameObject;
    }

    BoxTrap(ItemSpawned itemSpawned)
    {
        this.state = State.ITEM;
        this.placedOn = Instant.now();
        this.worldLocation = itemSpawned.getTile().getWorldLocation();
        this.tileItem = itemSpawned.getItem();
    }

    enum State
    {
        /**
         * A laid out trap.
         */
        OPEN,
        /**
         * A trap that is empty.
         */
        EMPTY,
        /**
         * A trap that caught something.
         */
        FULL,


        ITEM
    }

    public int getTrapTimeRemaining()
    {
        Duration duration = Duration.between(placedOn, Instant.now());
        return 60 - duration.toSecondsPart();
    }

    /**
     * Resets the time value when the trap was placed.
     */
    public void resetTimer()
    {
        placedOn = Instant.now();
    }
}