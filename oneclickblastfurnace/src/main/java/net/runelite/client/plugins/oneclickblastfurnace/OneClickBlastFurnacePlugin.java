package net.runelite.client.plugins.oneclickblastfurnace;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.*;

@Extension
@PluginDescriptor(
        name = "One Click Blast Furnace(BETA)",
        description = "Not heavily tested. Refer to github.com/magnusrn/plugins readme for info. ",
        enabledByDefault = false,
        tags = {"one","click","oneclick","smithing","blast","furnace"})
@Slf4j
public class OneClickBlastFurnacePlugin extends Plugin{

    //TODO
    //speed up bar collection
    //Add some way to not waste run energy, maybe just use counter on stam drink and drink early if about to run out.
    //Add super energies if people complain and they're feasible at bf

    private boolean shouldWithdrawBars = false;
    private boolean coalBagFull = false; // this is in theory gettable from client.getVar(VarPlayer.POUCH_STATUS) but for some reason seems inconsistent.
    private int withdrawStaminaCooldown = 0 ; //don't look at these, ghetto way of being able to click fast
    private int withdrawCoalCooldown = 0 ;
    private int withdrawOreCooldown = 0;
    private int fillCoalBagCooldown = 0;
    private int depositAllCooldown = 0;
    private int equipIceGlovesCooldown = 0;
    private int equipGoldGlovesCooldown = 0;
    private int takeBarCooldown = 0;
    private int timeout = 0;
    private BeltState beltState = BeltState.DEPOSIT_ORE_OR_COAL;

    @Inject
    private Client client;

    @Inject
    private OneClickBlastFurnaceConfig config;

    @Provides
    OneClickBlastFurnaceConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickBlastFurnaceConfig.class);
    }
    @Override
    protected void startUp() throws Exception
    {
        shouldWithdrawBars = false;
        coalBagFull = false;
        withdrawStaminaCooldown = 0 ;
        withdrawCoalCooldown = 0 ;
        withdrawOreCooldown = 0;
        fillCoalBagCooldown = 0;
        depositAllCooldown = 0;
        equipIceGlovesCooldown = 0;
        equipGoldGlovesCooldown = 0;
        takeBarCooldown = 0;
        timeout = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (withdrawStaminaCooldown>0) withdrawStaminaCooldown--;
        if (withdrawCoalCooldown>0) withdrawCoalCooldown--;
        if (withdrawOreCooldown>0) withdrawOreCooldown--;
        if (fillCoalBagCooldown>0) fillCoalBagCooldown--;
        if (depositAllCooldown>0) depositAllCooldown--;
        if (equipIceGlovesCooldown>0) equipIceGlovesCooldown--;
        if (equipGoldGlovesCooldown>0) equipGoldGlovesCooldown--;
        if (takeBarCooldown>0) takeBarCooldown--;
        if (timeout>0) timeout--;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Blast Furnace"))
            handleClick(event);
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        String text = "<col=00ff00>One Click Blast Furnace";
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
        //Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    enum BeltState {
        EMPTY_COAL_BAG,
        DEPOSIT_ORE_OR_COAL,
        DEPOSIT_COAL,
        TAKE_BARS,
        BANK
    }

    private void handleClick(MenuOptionClicked event) {
        if (timeout>0) { return;} //returns if waiting on stamina to withdraw
        System.out.println("shouldWithdrawBars = " + shouldWithdrawBars);
        if (config.barType() == OneClickBlastFurnaceTypes.GOLD) {
            if (getEmptySlots() != 0 && getInventoryItem(OneClickBlastFurnaceTypes.GOLD.getOreID()) == null && equipIceMES() != null && isBesideBelt() && equipIceGlovesCooldown == 0) { //edge case - if stam is drunk there'll be empty slots
                event.setMenuEntry(equipIceMES());
                equipIceGlovesCooldown += 5;
                return;
            }
        }

        if (oreInInvent() || coalInInvent()) { //if ore or coal in invent should be depositing.
            event.setMenuEntry(depositOreMES());
            beltState = BeltState.EMPTY_COAL_BAG;
            return;
        }
        if (bankOpen()) {
            ImmutableList<Integer> StaminaIds = ImmutableList.of(12625, 12627, 12629, 12631);
            for (Integer staminaID : StaminaIds) {
                if (getInventoryItem(staminaID) != null) {
                    event.setMenuEntry(drinkStamMES(staminaID));
                    return;
                }
            }
            if (client.getEnergy() < 20 ) {
                if (getEmptySlots() < 1)
                {
                    event.setMenuEntry(depositAllMES());
                    return;
                }
                event.setMenuEntry(withdrawFullStaminaMES());
                System.out.println("1");
                timeout += 1;
                return;
            }
            else if (((client.getEnergy() < 80 && !iswearingRingOfEndurance()) || client.getEnergy() < 60) || client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) {
                if (getEmptySlots() < 1)
                {
                    event.setMenuEntry(depositAllMES());
                    return;
                }
                event.setMenuEntry(withdrawStaminaMES());
                System.out.println("2");
                timeout+=1;
                return;
            }

            beltState = BeltState.DEPOSIT_ORE_OR_COAL; //reset belt state
            if (getInventoryItem(config.barType().getBarID()) != null && depositAllCooldown == 0) {
                event.setMenuEntry(depositAllMES());
                System.out.println("3");
                depositAllCooldown += 5;
                return;
            }
            if (!coalBagFull && fillCoalBagMES() != null && fillCoalBagCooldown == 0) {
                event.setMenuEntry(fillCoalBagMES());
                System.out.println("4");
                fillCoalBagCooldown += 5;
                return;
            }
            int coalDeposited = client.getVarbitValue(Varbits.BLAST_FURNACE_COAL.getId());
            OneClickBlastFurnaceTypes Bar = config.barType();
            if (Bar.getCoal() * 28 > coalDeposited && withdrawCoalCooldown == 0) { //this is overkill but can afford to be without any negatives afaik. Need surplus always to prevent iron bars accidentally. Covers with and without coal bag.
                event.setMenuEntry(withdrawCoalMES());
                withdrawCoalCooldown += 5;
                System.out.println("5");
                return;
            } else if (withdrawOreCooldown == 0 && withdrawCoalCooldown == 0) { //withdraw coal cooldown somehow needed, think it's due to leaving bank before shits loaded? idfk
                event.setMenuEntry(withdrawOreMES());
                withdrawOreCooldown += 5;
                shouldWithdrawBars = true;
                System.out.println("6");
                return;
            }
            event.setMenuEntry(depositOreMES());
            beltState = BeltState.EMPTY_COAL_BAG;
            return;
        }

        if (isBesideBelt()) {
            System.out.println(beltState);
            switch (beltState) {
                case DEPOSIT_ORE_OR_COAL:
                    event.setMenuEntry(depositOreMES());
                    beltState = BeltState.EMPTY_COAL_BAG;
                    return;
                case EMPTY_COAL_BAG:
                    if (coalBagFull && emptyCoalBagMES() != null) {
                        event.setMenuEntry(emptyCoalBagMES());
                        coalBagFull = false;
                        beltState = BeltState.DEPOSIT_COAL;
                    } else if (shouldWithdrawBars) {
                        event.setMenuEntry(withdrawBarsMES());
                        beltState = BeltState.TAKE_BARS;
                    } else {
                        event.setMenuEntry(bankMES());
                        beltState = BeltState.BANK;
                    }
                    return;
                case DEPOSIT_COAL:
                    event.setMenuEntry(depositOreMES());
                    if (shouldWithdrawBars) {
                        beltState = BeltState.TAKE_BARS;
                    } else {
                        beltState = BeltState.BANK;
                    }
                    return;
                case TAKE_BARS:
                    event.setMenuEntry(withdrawBarsMES());
                    return;
                case BANK:
                    event.setMenuEntry(bankMES());
                    return;
            }
        }

        if (client.getWidget(270,1)!=null ) {
            event.setMenuEntry(takeBarsMES());
            return;
        }

        if (getEmptySlots()>5 && shouldWithdrawBars) { //can be any value between ~2 and 26,
            event.setMenuEntry(withdrawBarsMES());
            return;
        }

        if (client.getWidget(229,1)!=null && equipGoldGlovesMES()!=null && equipGoldGlovesCooldown==0) {
            event.setMenuEntry(equipGoldGlovesMES());
            equipGoldGlovesCooldown+=5;
            return;
        }
        event.setMenuEntry(bankMES());
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getItemContainer()==client.getItemContainer(InventoryID.INVENTORY))
        {
            if (getInventoryItem(config.barType().getBarID())!=null) {
                shouldWithdrawBars =false;
            }
        }
    }

    private MenuEntry bankMES() {
        GameObject bank = getGameObject(26707);
        return createMenuEntry(bank.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(bank).getX(), getLocation(bank).getY(), false);
    }

    private MenuEntry equipGoldGlovesMES() {
        int gloveID = 776;
        WidgetItem goldGloves = getInventoryItem(gloveID);
        if (goldGloves != null) {
            return createMenuEntry(goldGloves.getId(), MenuAction.ITEM_SECOND_OPTION, goldGloves.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return null;
    }

    private MenuEntry equipIceMES() {
        int gloveID = 1580;
        WidgetItem iceGloves = getInventoryItem(gloveID);
        if (iceGloves != null) {
            return createMenuEntry(iceGloves.getId(), MenuAction.ITEM_SECOND_OPTION, iceGloves.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return null;
    }

    private MenuEntry depositOreMES() {
        GameObject belt = getGameObject(9100);
        return createMenuEntry(belt.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(belt).getX(), getLocation(belt).getY(), false);
    }

    private MenuEntry withdrawBarsMES() {
        GameObject barDispenser = getGameObject(9092);
        return createMenuEntry(barDispenser.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(barDispenser).getX(), getLocation(barDispenser).getY(), false);
    }

    private MenuEntry depositAllMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.BANK_DEPOSIT_INVENTORY.getId(), false);
    }

    private MenuEntry takeBarsMES() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, 17694734, false);
    }

    private MenuEntry fillCoalBagMES() {
        WidgetItem closedCoalBag = getInventoryItem(12019);
        WidgetItem openCoalBag = getInventoryItem(24480);
        coalBagFull = true;
        if (closedCoalBag!=null) {
            return createMenuEntry(9, MenuAction.CC_OP, closedCoalBag.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
        }
        if (openCoalBag != null) {
            return createMenuEntry(9, MenuAction.CC_OP, openCoalBag.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
        }
        return null;
    }

    private MenuEntry emptyCoalBagMES() {
        WidgetItem closedCoalBag = getInventoryItem(12019);
        WidgetItem openCoalBag = getInventoryItem(24480);

        coalBagFull = false;
        if (closedCoalBag!=null) {
            return createMenuEntry(closedCoalBag.getId(), MenuAction.ITEM_FOURTH_OPTION, closedCoalBag.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (openCoalBag != null) {
            return createMenuEntry(openCoalBag.getId(), MenuAction.ITEM_FOURTH_OPTION, openCoalBag.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return null;
    }

    private MenuEntry withdrawOreMES() {
        OneClickBlastFurnaceTypes Ore = config.barType();
        return createMenuEntry(7, MenuAction.CC_OP_LOW_PRIORITY, getBankIndex(Ore.getOreID()), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry withdrawCoalMES() {
        int coal = 453;
        return createMenuEntry(7, MenuAction.CC_OP_LOW_PRIORITY, getBankIndex(coal), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry withdrawStaminaMES() {
        int staminaDose = 12631;
        return createMenuEntry(1, MenuAction.CC_OP, getBankIndex(staminaDose), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry withdrawFullStaminaMES() {
        int staminaPotion = 12625;
        return createMenuEntry(1, MenuAction.CC_OP, getBankIndex(staminaPotion), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
    }

    private MenuEntry drinkStamMES(int id) {
        WidgetItem staminaDose = getInventoryItem(id);
        return createMenuEntry(9, MenuAction.CC_OP_LOW_PRIORITY, staminaDose.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
    }

    private boolean iswearingRingOfEndurance() {
        return client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.RING_OF_ENDURANCE);
    }

    private boolean oreInInvent() {
        int ore = config.barType().getOreID();
        return (getInventoryItem(ore)!= null);
    }

    private boolean coalInInvent() {
        return getInventoryItem(453) != null;
    }

    private boolean isBesideBelt() {
        return (getGameObject(9100).getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())<2);
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject instanceof GameObject)
            return ((GameObject) tileObject).getSceneMinLocation();
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private GameObject getGameObject(int id)
    {
        return new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
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

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private int getBankIndex(int ID){
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(ID)
                .result(client)
                .first();
        if (bankItem != null) {
            return bankItem.getWidget().getIndex();
        }
        return -1;
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}