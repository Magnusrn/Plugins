package net.runelite.client.plugins.oneclickbloodsmorytania;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.messages.SkillUpdate;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "One Click Bloods Morytania",
        description = "Check Discord for setup information. Discord.link/kitsch",
        enabledByDefault = false
)
@Slf4j
public class OneClickBloodsMorytaniaPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OneClickBloodsMorytaniaConfig config;

    @Provides
    OneClickBloodsMorytaniaConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickBloodsMorytaniaConfig.class);
    }

    private int runecraftingState = 0;
    private int bankingState = 0;
    private int currentxp = 0;
    private boolean craftedRunes = false;

    @Override
    protected void startUp() throws Exception {
        reset();
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        if (currentxp == 0)
        {
            currentxp = client.getSkillExperience(Skill.RUNECRAFT);
        }
    }

    private void reset() {
        runecraftingState = 0;
        bankingState = 0;
        craftedRunes = false;
    }

    @Subscribe
    protected void onStatChanged(StatChanged event) {
        //on login this triggers going from 0 to players current XP. all xp drops(even on leagues etc) should be below 50k and this method requires 77 rc.
        if (event.getSkill() == Skill.RUNECRAFT && event.getXp()-currentxp<50000)
        {
            craftedRunes = true;
        }
    }


    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) return;
        String text = "<col=00ff00>One Click Bloods Morytania";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
    {
        if (event.getMenuOption().equals("<col=00ff00>One Click Bloods Morytania"))
            handleClick(event);
    }
    private void handleClick(MenuOptionClicked event) //billion if statements but unsure of alternative method, can't assign menuentries until visible due to queries
    {
        WidgetItem smallPouch = getInventoryItem(ItemID.SMALL_POUCH);
        WidgetItem mediumPouch = getInventoryItem(ItemID.MEDIUM_POUCH);
        WidgetItem largePouch = getInventoryItem(ItemID.LARGE_POUCH);
        WidgetItem giantPouch = getInventoryItem(ItemID.GIANT_POUCH);
        WidgetItem colossalPouch = getInventoryItem(ItemID.COLOSSAL_POUCH);

        if (handlePouchRepair()!=null)
        {
            event.setMenuEntry(handlePouchRepair());
            return;
        }

        List<Integer> brokenPouches = Arrays.asList(ItemID.MEDIUM_POUCH_5511,ItemID.LARGE_POUCH_5513,ItemID.GIANT_POUCH_5515,ItemID.COLOSSAL_POUCH_26786);
        if (brokenPouches.stream().anyMatch(pouch -> client.getItemContainer(InventoryID.INVENTORY).contains(pouch)))
        {
            event.setMenuEntry(repairPouchesSpellMES());
            return;
        }

        if (isInBloodAltar())
        {
            switch (runecraftingState)
            {
                case 0:
                    event.setMenuEntry(craftRunesMES());
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 1;
                case 1:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(colossalPouch));
                        runecraftingState = 3;
                        return;
                    }
                    if (giantPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(giantPouch));
                        runecraftingState = 2;
                        return;
                    }
                case 2:
                    if (largePouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(largePouch));
                        runecraftingState = 3;
                        return;
                    }
                case 3:
                    event.setMenuEntry(craftRunesMES());
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 4;
                case 4:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(colossalPouch));
                        runecraftingState = 6;
                        return;
                    }
                    if (mediumPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(mediumPouch));
                        runecraftingState = 5;
                        return;
                    }
                case 5:
                    if (smallPouch!=null)
                    {
                        event.setMenuEntry(emptyPouchMES(smallPouch));
                        runecraftingState = 6;
                        return;
                    }
                case 6:
                    event.setMenuEntry(craftRunesMES());
                    if (!craftedRunes)
                    {
                        return;
                    }
                    craftedRunes = false;
                    runecraftingState = 7;
                case 7:
                    event.setMenuEntry(teleToBankMES());
                    return;
            }
        }

        if (bankOpen())
        {
            switch (bankingState)
            {
                case 0:
                    event.setMenuEntry(withdrawEssence());
                    bankingState = 1;
                    return;
                case 1:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(colossalPouch));
                        bankingState = 3;
                        return;
                    }
                    if (giantPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(giantPouch));
                        bankingState = 2;
                        return;
                    }
                case 2:
                    if (largePouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(largePouch));
                        bankingState = 3;
                        return;
                    }
                case 3:
                    event.setMenuEntry(withdrawEssence());
                    bankingState = 4;
                    return;
                case 4:
                    if (colossalPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(colossalPouch));
                        bankingState = 6;
                        return;
                    }
                    if (mediumPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(mediumPouch));
                        bankingState = 5;
                        return;
                    }
                case 5:
                    if (smallPouch!=null)
                    {
                        event.setMenuEntry(fillPouchMES(smallPouch));
                        bankingState = 6;
                        return;
                    }
                case 6:
                    event.setMenuEntry(withdrawEssence());
                    bankingState = 7;
                    return;
                case 7:
                    event.setMenuEntry(closebankMES());
                    return;
            }
        }

        if (isInBloodAltarArea())
        {
            event.setMenuEntry(enterAltarMES());
            return;
        }

        if (isInPOH())
        {
            if (client.getEnergy()<config.runEnergy())
            {
                event.setMenuEntry(drinkFromPoolMES());
                return;
            }
            event.setMenuEntry(useFairyRingMES());
            return;
        }
        if (isInMorytaniaHideout1())
        {
            event.setMenuEntry(leaveMorytaniaHideout1MES());
            return;
        }
        if (isInMorytaniaHideout2())
        {
            event.setMenuEntry(leaveMorytaniaHideout2MES());
            return;
        }
        if (isInMorytaniaHideout3())
        {
            event.setMenuEntry(leaveMorytaniaHideout3MES());
            return;
        }
        if (isinMorytaniaHideout4LowAgility())
        {
            event.setMenuEntry(leaveMorytaniaHideout4LowAgilityMES());
            return;
        }
        if (isinMorytaniaHideout5LowAgility())
        {
            event.setMenuEntry(useLowAgilityShortcut1MES());
            return;
        }
        if (isinMorytaniaHideout5LowAgilityShortcut())
        {
            event.setMenuEntry(useLowAgilityShortcut2MES());
            return;
        }

        if (isinMorytaniaHideout5HighAgilityShortcut())
        {
            event.setMenuEntry(useHighAgilityShortcut2MES());
            return;
        }
        if (getEmptySlots()>0 && bankMES()!=null)
        {
            event.setMenuEntry(bankMES());
            return;
        }
        event.setMenuEntry(teleToPOHMES());
    }

    private MenuEntry handlePouchRepair() {
        if (client.getWidget(231,6)!=null && client.getWidget(231, 6).getText().equals("What do you want? Can't you see I'm busy?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, -1, 15138821, false);
        }
        //if player doesn't have abyssal pouch in bank
        if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(2)!=null && client.getWidget(219,1).getChild(2).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, 2, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
        }
        //if player has abyssal pouch in bank
        if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(1)!=null && client.getWidget(219,1).getChild(1).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, 1, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
        }

        if (client.getWidget(217,6)!=null && client.getWidget(217,6).getText().equals("Can you repair my pouches?"))
        {
            return createMenuEntry(0, MenuAction.WIDGET_TYPE_6, -1, 14221317, false);
        }
        return null;
    }

    private MenuEntry drinkFromPoolMES() {
        GameObject pool = getGameObject(29241);
        return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(),getLocation(pool).getY(), false);
    }

    private MenuEntry useFairyRingMES() {
        GameObject fairyRing = getGameObject(29228);
        if (getGameObject(29229)!=null) //if tree fairy ring combo is present
        {
            fairyRing = getGameObject(29229);
            return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
        }
        return createMenuEntry(fairyRing.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(fairyRing).getX(),getLocation(fairyRing).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout1MES() {
        GameObject tunnel = getGameObject(16308);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry leaveMorytaniaHideout2MES() {
        GameObject tunnel = getGameObject(5046);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout3MES() {
        //if 93 agility & 78 mining use good shortcut else use shit one
        GameObject tunnel = getGameObject(43759); //new tunnel ID
        if ((client.getBoostedSkillLevel(Skill.AGILITY)<93 || client.getBoostedSkillLevel(Skill.MINING)<78) && !config.overrideAgility())
        {
            tunnel = getGameObject(12770);
        }
        return createMenuEntry( tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry leaveMorytaniaHideout4LowAgilityMES() {
        //multiple objects with same ID so need to ensure it's the south tunnel
        WorldArea worldarea = new WorldArea(new WorldPoint(3488,9858,0),new WorldPoint(3495,9865,0));
        GameObject tunnel = new GameObjectQuery()
                .idEquals(12771)
                .result(client)
                .stream()
                .filter(t -> t.getWorldLocation().isInArea(worldarea))
                .findFirst()
                .orElse(null);
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry useLowAgilityShortcut1MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43755)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }
    private MenuEntry useLowAgilityShortcut2MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43758)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry useHighAgilityShortcut2MES() {
        WallObject tunnel = new WallObjectQuery()
                .idEquals(43762)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        return createMenuEntry(tunnel.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(tunnel).getX(), getLocation(tunnel).getY(), false);
    }

    private MenuEntry enterAltarMES() {
        GameObject altar = getGameObject(25380);

        //check to see if wearing any item that allow left click altar entrance
        List<Integer> items = Arrays.asList(ItemID.BLOOD_TIARA,ItemID.MAX_CAPE,ItemID.RUNECRAFT_CAPE,ItemID.RUNECRAFT_CAPET,ItemID.MAX_CAPE_13342,ItemID.CATALYTIC_TIARA);
         if (items.stream().anyMatch(item -> client.getItemContainer(InventoryID.EQUIPMENT).contains(item)))
         {
             return createMenuEntry(altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(altar).getX(), getLocation(altar).getY(), false);
         }

         client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
         if (getInventoryItem(ItemID.CATALYTIC_TALISMAN)!=null)
         {
             client.setSelectedItemSlot(getInventoryItem(ItemID.CATALYTIC_TALISMAN).getIndex());
             client.setSelectedItemID(ItemID.CATALYTIC_TALISMAN);
         }
         else
         {
             client.setSelectedItemSlot(getInventoryItem(ItemID.BLOOD_TALISMAN).getIndex());
             client.setSelectedItemID(ItemID.BLOOD_TALISMAN);
         }

        return createMenuEntry(altar.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT, getLocation(altar).getX(), getLocation(altar).getY(), false);
    }

    private MenuEntry craftRunesMES() {
        GameObject altar = getGameObject(43479);
        return createMenuEntry(altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(altar).getX(), getLocation(altar).getY(), true);
    }

    private MenuEntry emptyPouchMES(WidgetItem pouch) {
        return createMenuEntry(pouch.getId(), MenuAction.ITEM_SECOND_OPTION, pouch.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private MenuEntry teleToBankMES() {
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.MAX_CAPE_13342))
        {
            return createMenuEntry(4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPE) || client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.CRAFTING_CAPET))
        {
            return createMenuEntry(3, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_CAPE.getId(), false);
        }

        WidgetItem craftingCape = getInventoryItem(ItemID.CRAFTING_CAPE);
        WidgetItem craftingCapeT = getInventoryItem(ItemID.CRAFTING_CAPET);
        if (craftingCape!=null)
        {
            return createMenuEntry(craftingCape.getId(), MenuAction.ITEM_THIRD_OPTION, craftingCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (craftingCapeT!=null)
        {
            return createMenuEntry(craftingCapeT.getId(), MenuAction.ITEM_THIRD_OPTION, craftingCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_MOONCLAN_TELEPORT.getId(), false);
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
        return null;
    }

    private MenuEntry withdrawEssence() {
        int essence = ItemID.PURE_ESSENCE;
        return createMenuEntry(7, MenuAction.CC_OP_LOW_PRIORITY, getBankIndex(essence), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry fillPouchMES(WidgetItem pouch) {
        return createMenuEntry(9, MenuAction.CC_OP_LOW_PRIORITY, pouch.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
    }

    private MenuEntry closebankMES() {
        return createMenuEntry(1, MenuAction.CC_OP, 11, 786434, false);
    }

    private MenuEntry teleToPOHMES() {
        WidgetItem tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        WidgetItem conCape = getInventoryItem(ItemID.CONSTRUCT_CAPE);
        WidgetItem conCapeT = getInventoryItem(ItemID.CONSTRUCT_CAPET);

        if (conCape!=null)
        {
            return createMenuEntry(conCape.getId(), MenuAction.ITEM_FOURTH_OPTION, conCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (conCapeT!=null)
        {
            return createMenuEntry(conCapeT.getId(), MenuAction.ITEM_FOURTH_OPTION, conCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
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
        return createMenuEntry(tab.getId(), MenuAction.ITEM_FIRST_OPTION, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private MenuEntry repairPouchesSpellMES() {
        return createMenuEntry(2, MenuAction.CC_OP, -1, WidgetInfo.SPELL_NPC_CONTACT.getId(), false);
    }

    private boolean isInPOH() {
        reset();
        return getGameObject(4525)!=null; //checks for portal, p sure this is same for everyone if not need to do alternative check.
    }
    private boolean isInMorytaniaHideout1() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3437,9819,0),new WorldPoint(3454,9830,0)));
    }

    private boolean isInMorytaniaHideout2() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3457,9807,0),new WorldPoint(3475,9825,0)));
    }

    private boolean isInMorytaniaHideout3() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3476,9799,0),new WorldPoint(3507,9840,0)));
    }

    private boolean isinMorytaniaHideout4LowAgility() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3485,9859,0),new WorldPoint(3498,9879,0)));
    }

    private boolean isinMorytaniaHideout5LowAgility() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3511,9807,0),new WorldPoint(3538,9832,0)));
    }

    private boolean isinMorytaniaHideout5LowAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3546,9785,0),new WorldPoint(3572,9812,0)));
    }

    private boolean isinMorytaniaHideout5HighAgilityShortcut() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3532,9764,0),new WorldPoint(3542,9781,0)));
    }

    private boolean isInBloodAltarArea() {
        return client.getLocalPlayer().getWorldLocation().isInArea(new WorldArea(new WorldPoint(3543,9764,0),new WorldPoint(3570,9784,0)));
    }

    private boolean isInBloodAltar() {
        int BLOOD_ALTAR_ID = 43479;
        return getGameObject(BLOOD_ALTAR_ID)!=null;
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private int getBankIndex(int ID){
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(ID)
                .result(client)
                .first();
        return bankItem.getWidget().getIndex();
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

    private WidgetItem getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    private MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}