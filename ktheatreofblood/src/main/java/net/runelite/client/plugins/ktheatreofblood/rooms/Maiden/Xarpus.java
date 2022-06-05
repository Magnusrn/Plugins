package net.runelite.client.plugins.ktheatreofblood.rooms.Maiden;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodConfig;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodPlugin;
import net.runelite.client.plugins.ktheatreofblood.Room;
import net.runelite.rs.api.RSClient;

import javax.inject.Inject;
import java.util.HashMap;

public class Xarpus extends Room {
    private int weaponCooldown;

    @Inject
    Client client;

    @Inject
    KTheatreOfBloodConfig config;

    @Provides
    KTheatreOfBloodConfig provideConfig(ConfigManager configManager) {
        return (KTheatreOfBloodConfig) configManager.getConfig(KTheatreOfBloodConfig.class);
    }

    @Inject
    protected Xarpus(KTheatreOfBloodPlugin plugin, KTheatreOfBloodConfig config) {
        super(plugin, config);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor()==client.getLocalPlayer())
        {
            if (client.getLocalPlayer().getAnimation()==8056)
            {
                weaponCooldown = 5;
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (weaponCooldown>0) weaponCooldown --;
        System.out.println(weaponCooldown);
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (config.xarpusWheelchair())
        {
            if (config.xarpusWheelchairWeaponCooldown() && weaponCooldown>0) return;
            if (isInDanger() && event.getMenuTarget().contains("Xarpus"))
            {
                event.consume();
                if (config.xarpusWheelchairClickFloor())
                {
                    walkTile(client.getLocalPlayer().getWorldLocation());
                }
                client.addChatMessage(ChatMessageType.BROADCAST,"DANGER","Rip HC","DANGER");
            }
        }
    }

    private boolean isInDanger() {
        NPC npc = new NPCQuery()
                .nameEquals("Xarpus")
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (npc == null) return false;
        int x = npc.getWorldLocation().getX();
        int y = npc.getWorldLocation().getY();
        //this requires a little overlap so people don't fuck themselves up in the middle. uses xarpus as a reference point to get worldpoints due to it constantly changing with new instances
        WorldArea swArea = new WorldArea(new WorldPoint(x-5,y-5,1),new WorldPoint(x+3,y+3,1));
        WorldArea seArea = new WorldArea(new WorldPoint(x+2,y-5,1),new WorldPoint(x+9,y+3,1));
        WorldArea nwArea = new WorldArea(new WorldPoint(x-5,y+2,1),new WorldPoint(x+3,y+9,1));
        WorldArea neArea = new WorldArea(new WorldPoint(x+2,y+2,1),new WorldPoint(x+9,y+9,1));
        HashMap<Integer, WorldArea> areas = new HashMap<>();
        areas.put(255,swArea);
        areas.put(735,nwArea);
        areas.put(1281,neArea);
        areas.put(1825,seArea);

        /**
         * Represents an in-game orientation that uses fixed point arithmetic.
         * <p>
         * Angles are represented as an int value ranging from 0-2047, where the
         * following is true:
         * <ul>
         *     <li>255  is South West</li>
         *     <li>735 is North West</li>
         *     <li>1281 is North East</li>
         *     <li>1825 is South East</li>
         * </ul>
         */

        return client.getLocalPlayer().getWorldLocation().isInArea(areas.get(npc.getOrientation()));
    }

    private void walkTile(WorldPoint worldpoint) {
        int x = worldpoint.getX() - client.getBaseX();
        int y = worldpoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }
}
