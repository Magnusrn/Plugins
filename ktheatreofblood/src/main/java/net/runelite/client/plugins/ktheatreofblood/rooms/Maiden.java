package net.runelite.client.plugins.ktheatreofblood.rooms;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodConfig;
import net.runelite.client.plugins.ktheatreofblood.Room;
import net.runelite.client.plugins.ktheatreofblood.KTheatreOfBloodPlugin;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Maiden extends Room {
    HashMap<NPC,String> crabs = new HashMap<>();
    private boolean hotkeyHeld = false;

    @Inject
    private Client client;

    @Inject
    private KTheatreOfBloodConfig config;

    @Inject
    private KeyManager keyManager;

    @Provides
    KTheatreOfBloodConfig provideConfig(ConfigManager configManager) {
        return (KTheatreOfBloodConfig) configManager.getConfig(KTheatreOfBloodConfig.class);
    }

    @Inject
    protected Maiden(KTheatreOfBloodPlugin plugin, KTheatreOfBloodConfig config) {
        super(plugin, config);
    }

    private final HotkeyListener maidenKeyListener = new HotkeyListener(() -> config.maidenKeybind()) {
        @Override
        public void keyPressed(KeyEvent e) {
            if (config.maidenKeybind().matches(e)) {
                hotkeyHeld = true;
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            if (config.maidenKeybind().matches(e)) {
                hotkeyHeld = false;
            }
        }
    };

    @Override
    protected void startUp() throws Exception {
        System.out.println("starting plugin");
        keyManager.registerKeyListener(maidenKeyListener);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        String position = "??";
        NPC npc = npcSpawned.getNpc();
        int x = npc.getWorldLocation().getRegionX();
        int y = npc.getWorldLocation().getRegionY();

        //TY SPOONLITE FOR POSITION CHECKS
        if (x == 21 && y == 40) {
            position = "N1";
        }else if (x == 22 && y == 41) {
            position = "N1";
        }else if (x == 25 && y == 40) {
            position = "N2";
        }else if (x == 26 && y == 41) {
            position = "N2";
        }else if (x == 29 && y == 40) {
            position = "N3";
        }else if (x == 30 && y == 41) {
            position = "N3";
        }else if (x == 33 && y == 40) {
            position = "N4";
        }else if (x == 34 && y == 41) {
            position = "N4";
        }else if (x == 33 && y == 38) {
            position = "N4";
        }else if (x == 34 && y == 39) {
            position = "N4";
        }else if (x == 21 && y == 20) {
            position = "S1";
        }else if (x == 22 && y == 19) {
            position = "S1";
        }else if (x == 25 && y == 20) {
            position = "S2";
        }else if (x == 26 && y == 19) {
            position = "S2";
        }else if (x == 29 && y == 20) {
            position = "S3";
        }else if (x == 30 && y == 19) {
            position = "S3";
        }else if (x == 33 && y == 20) {
            position = "S4";
        }else if (x == 34 && y == 19) {
            position = "S4";
        }else if (x == 33 && y == 22) {
            position = "S4";
        }else if (x == 34 && y == 20) {
            position = "S4";
        }
        if (npcSpawned.getNpc()==null || npcSpawned.getNpc().getName() == null) return;
        if (npcSpawned.getNpc().getName().contains("Nylocas Matomenos"))
        {
            crabs.put(npc,position);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        if (npcDespawned.getNpc()==null || npcDespawned.getNpc().getName() == null) return;
        if (npcDespawned.getNpc().getName().contains("Nylocas Matomenos"))
        {
            crabs.remove(npcDespawned.getNpc());
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuTarget().contains("Maiden") && hotkeyHeld) {
            if (crabs.size() != 0 && getOptimalCrab()!=null) {
                NPC npc = getOptimalCrab();
                Widget widget = client.getWidget(WidgetInfo.SPELL_ICE_BARRAGE);
                client.setSelectedSpellName("<col=00ff00>" + "Ice Barrage" + "</col>");
                client.setSelectedSpellWidget(widget.getId());
                client.setSelectedSpellChildIndex(-1);
                event.setMenuAction(MenuAction.WIDGET_TARGET_ON_NPC);
                event.setParam0(getLocation(npc).getX()); //do i even need location? idfk
                event.setParam1(getLocation(npc).getY());
                event.setId(npc.getIndex());
                crabs.remove(npc); //prevent double freezing same crab, though this should really only be used once!
                return;
            }
            event.consume();
        }
    }

    private NPC getOptimalCrab() {
        String npcLocation = null;
        if (crabs.containsValue("S2")) npcLocation = "S2";
        if (crabs.containsValue("N2")) npcLocation = "N2";
        if (crabs.containsValue("N1")) npcLocation = "N1";
        if (crabs.containsValue("S1")) npcLocation = "S1";

        String finalNpcLocation = npcLocation;
        return  crabs.keySet().stream().filter(crab-> crabs.get(crab).equals(finalNpcLocation)).findAny().orElse(null);
    }

    private Point getLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }
}