package net.runelite.client.plugins.oneclickkarambwans;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import java.util.*;

@Extension
@PluginDescriptor(
        name = "One Click Karambwans",
        description = "Set recent fairy ring to DKP. Supports Fish Barrel",
        tags = {"karambwans,one click,zanaris,fishing,oneclick"},
        enabledByDefault = false
)

@Slf4j
public class oneClickKarambwansPlugin extends Plugin {

    private final int FAIRY_RING_KARAMJA_ID = 29495;
    private String state = "BARREL";

    @Inject
    private Client client;

    @Inject
    private oneClickKarambwansConfig config;

    @Provides
    oneClickKarambwansConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(oneClickKarambwansConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        state = "BARREL";
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Karambwans")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) return;
        String text = "<col=00ff00>One Click Karambwans";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    private void handleClick(MenuOptionClicked event) {
        int FAIRY_RING_ANIMATION1 = 3265;
        int FAIRY_RING_ANIMATION2 = 3266;
        if ((client.getLocalPlayer().isMoving()
                || client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() == AnimationID.FISHING_KARAMBWAN
                || client.getLocalPlayer().getAnimation() == FAIRY_RING_ANIMATION1
                || client.getLocalPlayer().getAnimation() == FAIRY_RING_ANIMATION2)
                & !bankOpen()) {
            System.out.println("Consume event because not idle?");
            event.consume();
            return;
        }
        System.out.println("1");

        if (getInventoryItem(ItemID.RAW_KARAMBWANJI)== null || getInventoryItem(ItemID.KARAMBWAN_VESSEL_3159) == null) {
            System.out.println("Consume event because no karambwanji or vessel");
            event.consume();
            return;
        }
        System.out.println("2");

        if (getEmptySlots() == 0) {
            if (getGameObject(FAIRY_RING_KARAMJA_ID) != null) {
                event.setMenuEntry(teleToBankMES());
                return;
            }
            if (!bankOpen())
            {
                event.setMenuEntry(bankMES());
                return;
            }
        }

        if (getInventoryItem(ItemID.RAW_KARAMBWAN) == null && getGameObject(FAIRY_RING_KARAMJA_ID) == null)
        {
            if (useFairyRingMES()!=null)
            {
                event.setMenuEntry(useFairyRingMES());
                return;
            }
            event.setMenuEntry(teleToFairyRingMES());
            return;
        }

        if (bankOpen()) {
            Set<Integer> CLUE_BOTTLE_SET = Set.of(ItemID.CLUE_BOTTLE_BEGINNER,ItemID.CLUE_BOTTLE_EASY,ItemID.CLUE_BOTTLE_MEDIUM,ItemID.CLUE_BOTTLE_HARD,ItemID.CLUE_BOTTLE_ELITE);
            for (int ClueBottle : CLUE_BOTTLE_SET)
            {
                if (getInventoryItem(ClueBottle)!=null)
                {
                    event.setMenuEntry(depositClueBottleMES(ClueBottle));
                    return;
                }
            }

            if (getInventoryItem(ItemID.OPEN_FISH_BARREL) != null) {
                switch (state) {
                    case "BARREL":
                        event.setMenuEntry(emptyBarrelMES());
                        state = "INVENTORY";
                        break;

                    case "INVENTORY":
                        event.setMenuEntry(depositKarambwansMES());
                        state = "BARREL";
                        break;
                }
            } else {
                event.setMenuEntry(depositKarambwansMES());
            }
            return;
        }

        if (getGameObject(FAIRY_RING_KARAMJA_ID) != null) {
            event.setMenuEntry(fishingSpotMES());
        }
    }

    private MenuEntry fishingSpotMES() {
        return createMenuEntry(
                getFishingSpot().getIndex(),
                MenuAction.NPC_FIRST_OPTION,
                getNPCLocation(getFishingSpot()).getX(),
                getNPCLocation(getFishingSpot()).getY(),
                false);
    }

    private MenuEntry teleToBankMES() {
        state = "BARREL"; //reset state before banking, workaround in case of spam clicking in bank messing up state before game has registered the container change

        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
        {
            return createMenuEntry(4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPET))
        {
            return createMenuEntry(3, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }

        Widget craftingCape = getInventoryItem(ItemID.CRAFTING_CAPE);
        Widget craftingCapeT = getInventoryItem(ItemID.CRAFTING_CAPET);
        if (craftingCape!=null)
        {
            return createMenuEntry(craftingCape.getId(), MenuAction.ITEM_THIRD_OPTION, craftingCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (craftingCapeT!=null)
        {
            return createMenuEntry(craftingCapeT.getId(), MenuAction.ITEM_THIRD_OPTION, craftingCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (config.bankAtSeers()) // not possible to check if seers tele is castable without spellbook being loaded
        {
            return createMenuEntry(2, MenuAction.CC_OP, -1, WidgetInfo.SPELL_CAMELOT_TELEPORT.getId(), false);
        }
        return createMenuEntry(FAIRY_RING_KARAMJA_ID, MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(getGameObject(FAIRY_RING_KARAMJA_ID)).getX(), getLocation(getGameObject(FAIRY_RING_KARAMJA_ID)).getY(), false);
    }

    private MenuEntry bankMES() {
        GameObject craftingBank = getGameObject(14886);
        if (craftingBank!=null)
        {
            return createMenuEntry(craftingBank.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(craftingBank).getX(), getLocation(craftingBank).getY(), false);
        }
        GameObject lunarBank = getGameObject(16700);
        if (lunarBank!=null)
        {
            return createMenuEntry(lunarBank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(lunarBank).getX(), getLocation(lunarBank).getY(), false);
        }
        GameObject seersBank = getGameObject(25808);
        if (seersBank!=null)
        {
            return createMenuEntry(seersBank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, getLocation(seersBank).getX(), getLocation(seersBank).getY(), false);
        }

        GameObject zanarisBank = getGameObject(26711);
        if (zanarisBank!=null)
        {
            return createMenuEntry(
                    zanarisBank.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION,
                    getLocation(zanarisBank).getX(),
                    getLocation(zanarisBank).getY(),
                    false);
        }
        return null;
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = null;
        GameObject zanarisToKaramjaFR = getGameObject(29560);
        GameObject legendsToKaramjaFr = getGameObject(29495);
        GameObject pohFairyRing = getGameObject(29228);
        GameObject pohFairyRingTreeCombo = getGameObject(29229);

        if (pohFairyRingTreeCombo!=null) //if tree fairy ring combo is present, different opcode to other fairy rings.
        {
            return createMenuEntry(pohFairyRingTreeCombo.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION, getLocation(pohFairyRingTreeCombo).getX(),getLocation(pohFairyRingTreeCombo).getY(), false);
        }

        if (zanarisToKaramjaFR!=null)
        {
            fairyRing = zanarisToKaramjaFR;
        }

        if (legendsToKaramjaFr!=null)
        {
            fairyRing = legendsToKaramjaFr;
        }

        if (pohFairyRing!=null)
        {
            fairyRing = pohFairyRing;
        }

        if (fairyRing!=null)
        {
            return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(), getLocation(fairyRing).getY(), false);
        }
        return null;
    }

    private MenuEntry teleToFairyRingMES() {
        if (config.pohFairyRing())
        {
            return teleToPOHMES();
        }
        if (useQuestCapeTeleMES()!=null)
        {
            return useQuestCapeTeleMES();
        }
        return null;
    }



    private MenuEntry useQuestCapeTeleMES() {
        Widget questCape = getInventoryItem(ItemID.QUEST_POINT_CAPE);
        Widget questCapeT = getInventoryItem(ItemID.QUEST_POINT_CAPE_T);

        if (questCapeT!=null)
        {
            return createMenuEntry(questCapeT.getId(), MenuAction.ITEM_THIRD_OPTION, questCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (questCape!=null)
        {
            return createMenuEntry(questCape.getId(), MenuAction.ITEM_THIRD_OPTION, questCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }

        if (client.getItemContainer(InventoryID.EQUIPMENT)!=null
                && (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.QUEST_POINT_CAPE)
                || (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.QUEST_POINT_CAPE_T))))
        {
            return createMenuEntry(3, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }
        return null;
    }


    private MenuEntry teleToPOHMES() {
        Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        Widget conCape = getInventoryItem(ItemID.CONSTRUCT_CAPE);
        Widget conCapeT = getInventoryItem(ItemID.CONSTRUCT_CAPET);

        if (conCape!=null)
        {
            return createMenuEntry(conCape.getId(), MenuAction.ITEM_FOURTH_OPTION, conCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (conCapeT!=null)
        {
            return createMenuEntry(conCapeT.getId(), MenuAction.ITEM_FOURTH_OPTION, conCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (tab!=null)
        {
            return createMenuEntry(tab.getId(), MenuAction.ITEM_FIRST_OPTION, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT)!=null)
        {
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
            {
                return createMenuEntry(5, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
            if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CONSTRUCT_CAPET))
            {
                return createMenuEntry( 4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
            }
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry depositKarambwansMES() {
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                getInventoryItem(ItemID.RAW_KARAMBWAN).getIndex(),
                983043,
                false);
    }

    private MenuEntry depositClueBottleMES(int ID) {
        return createMenuEntry(
                2,
                MenuAction.CC_OP,
                getInventoryItem(ID).getIndex(),
                983043,
                false);
    }

    private MenuEntry emptyBarrelMES() {
        return createMenuEntry(
                9,
                MenuAction.CC_OP_LOW_PRIORITY,
                getInventoryItem(ItemID.OPEN_FISH_BARREL).getIndex(),
                983043, false);
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private Widget getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget!=null && !inventoryWidget.isHidden())
        {
            return getWidgetItem(inventoryWidget,id);
        }
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        return null;
    }

    private Widget getWidgetItem(Widget widget,int id) {
        for (Widget item : widget.getDynamicChildren())
        {
            if (item.getItemId() == id)
            {
                return item;
            }
        }
        return null;
    }

    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }
        return -1;
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject == null) {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject) {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private Point getNPCLocation(NPC npc)
    {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private NPC getFishingSpot()
    {
        return new NPCQuery()
                .idEquals(NpcID.FISHING_SPOT_4712)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}