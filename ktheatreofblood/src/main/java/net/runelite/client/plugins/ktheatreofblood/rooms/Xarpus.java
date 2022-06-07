package net.runelite.client.plugins.ktheatreofblood.rooms;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodConfig;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodPlugin;
import net.runelite.client.plugins.ktheatreofblood.Room;
import net.runelite.rs.api.RSClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Xarpus extends Room {
    private int weaponCooldown;
    private int ticksSinceTurn = 0;
    private int lastDirection = 0;

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

    @Override
    protected void startUp() throws Exception {
        System.out.println("starting plugin xarpus");
        reset();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor()==client.getLocalPlayer())
        {
            switch (client.getLocalPlayer().getAnimation())
            {
                case 8056: //scythe animation
                case 435: //scythe defensive (wyd)
                    weaponCooldown = 5;
                    break;
                case 1658: //abyssal whip/Tentacle slash
                case 1659: //abyssal whip/Tentacle defensive
                case 4503: //Inquisitor's mace crush
                case 390: //Dragon Scimitar /Blade of Saeldor
                case 393: // claw scratch
                case 8145: // rapier stab
                case 1062: // dds spec
                case 422: // punch
                case 423: // kick
                case 386: // lunge
                    weaponCooldown = 4;
                    break;
                case 428: // chally swipe
                case 440: // chally jab
                case 1203: // chally spec
                    weaponCooldown = 7;
                    break;
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc()==null || event.getNpc().getName() == null) return;
        if (event.getNpc().getName().contains("Xarpus"))
        {
            reset();
        }
    }

    private void reset() {
        ticksSinceTurn = 0;
        lastDirection = 0;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (weaponCooldown>0) weaponCooldown --;
        NPC xarpus = getXarpus();
        List<Integer> directions = Arrays.asList(255,735,1281,1825);
        if (xarpus!=null)
        {
            if (lastDirection!= xarpus.getOrientation())
            {
                ticksSinceTurn = 0;
            }
            if (directions.contains(xarpus.getOrientation()))
            {
                ticksSinceTurn++;
                lastDirection = xarpus.getOrientation();
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (config.xarpusWheelchair())
        {
            if (ticksSinceTurn+weaponCooldown>7) return;
            if (ticksSinceTurn>7) return;
            if (isInDanger() && event.getMenuTarget().contains("Xarpus"))
            {
                event.consume();
                walkTile(client.getLocalPlayer().getWorldLocation());
            }
        }
    }

    private boolean isInDanger() {
        NPC npc = getXarpus();
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
        //fix npe on entering, is orientation nulling sometimes?
        return client.getLocalPlayer().getWorldLocation().isInArea(areas.get(npc.getOrientation()));
    }

    private NPC getXarpus() {
        return new NPCQuery()
                .nameEquals("Xarpus")
                .result(client)
                .nearestTo(client.getLocalPlayer());
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
