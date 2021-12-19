package net.runelite.client.plugins.oneclickminnows;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.Objects;

@Extension
@PluginDescriptor(
        name = "One Click Minnows",
        description = "...",
        tags = {"one", "click", "oneclick", "minnows"},
        enabledByDefault = false
)
@Slf4j
public class OneClickMinnowsPlugin extends Plugin {
    private int FLYING_FISH_GRAPHIC_ID = 1387;
    private int FISHING_SPOT_1_ID = 7732;
    private int FISHING_SPOT_2_ID = 7733;
    private int TARGET_SPOT_ID = FISHING_SPOT_1_ID;
    private int timeout;
    NPC InteractingFishingSpot;

    @Inject
    private Client client;

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (timeout>0)
        {
            timeout--;
        }
        if (client.getLocalPlayer().getInteracting()!=null)
        {
            timeout=3;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Minnows"))
            handleClick(event);
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        String text= "<col=00ff00>One Click Minnows";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
    }

    private void handleClick(MenuOptionClicked event) {
        if(((client.getLocalPlayer()).isMoving()
                ||client.getLocalPlayer().getPoseAnimation()
                !=client.getLocalPlayer().getIdlePoseAnimation()))
        {
            System.out.println("Consume event because not idle?");
            event.consume();
            return;
        }

        if (client.getLocalPlayer().getInteracting()!=null)
        {
            System.out.println("PLAYER IS INTERACTING WITH SOMETHING");
            if ((client.getLocalPlayer()).getInteracting().getGraphic() == FLYING_FISH_GRAPHIC_ID && InteractingFishingSpot.getId() == TARGET_SPOT_ID)
            {
                if (TARGET_SPOT_ID == FISHING_SPOT_1_ID)
                {
                    TARGET_SPOT_ID = FISHING_SPOT_2_ID;
                }
                else if (TARGET_SPOT_ID == FISHING_SPOT_2_ID)
                {
                    TARGET_SPOT_ID = FISHING_SPOT_1_ID;
                }
                event.setMenuEntry(fishingSpotMES(TARGET_SPOT_ID));
                return;
            }
        }
        if (timeout!=0)
        {
            event.consume();
            return;
        }
        event.setMenuEntry(fishingSpotMES(TARGET_SPOT_ID));
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        if (event.getSource()!=client.getLocalPlayer()) { return; }
        if(!(event.getTarget() instanceof NPC)) { return; }

        InteractingFishingSpot = (NPC) event.getTarget();
    }

    private MenuEntry fishingSpotMES(int id) {
        return createMenuEntry(
                getFishingSpot(id).getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getFishingSpotLocation(getFishingSpot(id)).getX(),
                getFishingSpotLocation(getFishingSpot(id)).getY(),
                false);
    }

    private Point getFishingSpotLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private NPC getFishingSpot(int id) {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}
