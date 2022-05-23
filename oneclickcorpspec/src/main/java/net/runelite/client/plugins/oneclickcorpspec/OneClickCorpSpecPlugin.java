package net.runelite.client.plugins.oneclickcorpspec;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.vars.AccountType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import java.util.*;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
        name = "One Click Corp Spec",
        enabledByDefault = false,
        description = "read disc for more info"
)
@Slf4j
public class OneClickCorpSpecPlugin extends Plugin {
    private boolean gamesNeckEquipped = false;
    private boolean tortureEquipped = false;
    private boolean hasTeledToPool = false;
    private boolean hasTeledToCorp = false;
    private boolean pluginPaused = true;
    //prevent double entering the lair
    private boolean enteredCorpArea = false;
    private int hammerHits = 0;
    private int arclightHits = 0;
    private int godswordDamage = 0;
    //trigger after corp is ready and only resume after pool is reached(nardah/poh) to allow for banking


    @Inject
    private Client client;

    @Inject
    private OneClickCorpSpecConfig config;

    @Inject
    private Notifier notifier;

    @Provides
    OneClickCorpSpecConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickCorpSpecConfig.class);
    }

    protected void startUp() throws Exception {
        reset();
    }

    private void reset() {
        hasTeledToPool = false;
        hasTeledToCorp = false;
        enteredCorpArea = false;
        hammerHits = 0;
        arclightHits = 0;
        godswordDamage = 0;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        checkEquippedNecklace();
        //dwh check as often you'll tele to pool after speccing is finished and it would reset plugin otherwise.
        if (isAtPoolAltar() && client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.DRAGON_WARHAMMER)) pluginPaused = false;
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        if (pluginPaused) return;
        String text = "<col=00ff00>One Click Corp Spec";
        client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("<col=00ff00>One Click Corp Spec"))
            handleClick(event);
    }

    private void handleClick(MenuOptionClicked event) {
        if (!specEnabled() && (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >=500 || hasTeledToPool))
        {
            setMenuEntry(event,enableSpec());
            return;
        }
        if (equipmentHandler()!=null && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >=500) //swap weapons anywhere to prevent overspeccing corp
        {
            setMenuEntry(event, equipmentHandler());
            return;
        }

        if (hasTeledToCorp)
        {
            if (!isInCorpEntrance()) return;
            hasTeledToCorp = false;
        }

        if (hasTeledToPool)
        {
            if (!isAtPoolAltar()) return;
            hasTeledToPool = false;
        }

        if (isInCorpEntrance())
        {
            if (equipTorture()!=null)
            {
                setMenuEntry(event,equipTorture());
                gamesNeckEquipped = false;
                tortureEquipped = true;
                return;
            }
            if (enteredCorpArea)
            {
                event.consume();
                return;
            }
            setMenuEntry(event,enterCorp());
            return;
        }

        if (isAtPoolAltar())
        {
            if (usePool()!=null)
            {
                setMenuEntry(event,usePool());
                return;
            }
            if (equipGamesNeck()!=null)
            {
                setMenuEntry(event,equipGamesNeck());
                gamesNeckEquipped = true;
                tortureEquipped = false;
                return;
            }
            if (teleToCorp()!=null)
            {
                setMenuEntry(event,teleToCorp());
                hasTeledToCorp = true;
                return;
            }
        }

        if (isInCorpsLair())
        {
            if (client.getBoostedSkillLevel(Skill.HITPOINTS)<config.emergencyTele())
            {//add option to automate this
                setMenuEntry(event, teleToPoolAltar());
                return;
            }
            if (attackCorp()!=null && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) != 0)
            {
                setMenuEntry(event,attackCorp());
                return;
            }
            if (attackCorp() == null)
            {
                event.consume();
                walkFurtherInsideCave();
                return;
            }
            if (equipGamesNeck()!=null)
            {
                setMenuEntry(event,equipGamesNeck());
                gamesNeckEquipped = true;
                tortureEquipped = false;
                return;
            }
        }
        setMenuEntry(event, teleToPoolAltar());
    }
    //returns true if at nardah or poh pool
    private boolean isAtPoolAltar() {
        return getGameObject(10389)!=null || getGameObject(ObjectID.ORNATE_POOL_OF_REJUVENATION)!=null;
    }

    private boolean isInCorpEntrance() {
        WorldArea worldArea = new WorldArea(new WorldPoint(2960,4248,2),new WorldPoint(2972,4263,2));
        if (client.getAccountType()== AccountType.NORMAL)
        {
            worldArea = new WorldArea(new WorldPoint(2960, 4377, 2), new WorldPoint(2972, 4391, 2));
        }
        return client.getLocalPlayer().getWorldLocation().isInArea(worldArea);
    }

    private boolean isInCorpsLair() {
        WorldArea worldArea = new WorldArea(new WorldPoint(2972,4240,2),new WorldPoint(3001,4272,2));
        if (client.getAccountType()== AccountType.NORMAL)
        {
            worldArea = new WorldArea(new WorldPoint(2972,4359,2),new WorldPoint(3001,4399,2));
        }
        return client.getLocalPlayer().getWorldLocation().isInArea(worldArea);
    }

    private MenuEntry usePool() {
        GameObject pool;
        if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) return null;
        pool = getGameObject(ObjectID.ORNATE_POOL_OF_REJUVENATION);
        if (pool==null)
        {
            pool = getGameObject(10389);
        }
        return createMenuEntry(pool.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(pool).getX(), getLocation(pool).getY(), false);
    }

    private MenuEntry useGamesNecklace() {
        if (!gamesNeckEquipped) return null;
        return createMenuEntry(4, MenuAction.CC_OP, -1, WidgetInfo.EQUIPMENT_AMULET.getId(), false);
    }

    private void checkEquippedNecklace() {
        List<Integer> necklaces = Arrays.asList(ItemID.GAMES_NECKLACE1,
                                                ItemID.GAMES_NECKLACE2,
                                                ItemID.GAMES_NECKLACE3,
                                                ItemID.GAMES_NECKLACE4,
                                                ItemID.GAMES_NECKLACE5,
                                                ItemID.GAMES_NECKLACE6,
                                                ItemID.GAMES_NECKLACE7,
                                                ItemID.GAMES_NECKLACE8);
        if (necklaces.stream().anyMatch(necklace -> client.getItemContainer(InventoryID.EQUIPMENT).contains(necklace)))
        {
            gamesNeckEquipped = true;
            tortureEquipped = false;
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.AMULET_OF_TORTURE))
        {
            gamesNeckEquipped = false;
            tortureEquipped = true;
        }
    }

    private MenuEntry useJewelleryBox() {
        GameObject box = getGameObject(29156);
        return createMenuEntry(box.getId(), MenuAction.GAME_OBJECT_THIRD_OPTION, getLocation(box).getX(), getLocation(box).getY(), false);
    }

    private MenuEntry teleToCorp() {
        System.out.println("teleing to corp");
        enteredCorpArea = false;
        return useGamesNecklace()!=null? useGamesNecklace() : useJewelleryBox();
    }

    private MenuEntry teleToPoolAltar() {
        hasTeledToPool = true;
        Widget amulet = getInventoryItem(ItemID.DESERT_AMULET_4);
        Widget tab = getInventoryItem(ItemID.TELEPORT_TO_HOUSE);
        Widget conCape = getInventoryItem(ItemID.CONSTRUCT_CAPE);
        Widget conCapeT = getInventoryItem(ItemID.CONSTRUCT_CAPET);
        if (conCape!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCape.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (conCapeT!=null)
        {
            return createMenuEntry(6, MenuAction.CC_OP_LOW_PRIORITY, conCapeT.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (tab!=null)
        {
            return createMenuEntry(2, MenuAction.CC_OP, tab.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (amulet!=null)
        {
            return createMenuEntry(4, MenuAction.CC_OP, amulet.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.SPELL_TELEPORT_TO_HOUSE.getId(), false);
    }

    private MenuEntry equipTorture() {
        Widget amulet = getInventoryItem(ItemID.AMULET_OF_TORTURE);
        if (amulet == null || tortureEquipped) return null;
        return createMenuEntry(3, MenuAction.CC_OP, amulet.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }

    private MenuEntry equipmentHandler() {
        Widget arclight = getInventoryItem(ItemID.ARCLIGHT);
        Widget godsword = getInventoryItem(ItemID.BANDOS_GODSWORD);
        if (hammerHits>=config.hammerHits() && client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.DRAGON_WARHAMMER))
        {
            return createMenuEntry(3, MenuAction.CC_OP, arclight.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        if (arclightHits>=config.arclightHits() && client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.ARCLIGHT))
        {
            return createMenuEntry(3, MenuAction.CC_OP, godsword.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        }
        return null;
    }

    private MenuEntry enableSpec() {
        return createMenuEntry(1, MenuAction.CC_OP, -1, WidgetInfo.MINIMAP_SPEC_CLICKBOX.getId(), false);
    }

    private MenuEntry enterCorp() {
        if (enteredCorpArea) return null;
        GameObject passage = getGameObject(677);
        enteredCorpArea = true;
        return createMenuEntry(passage.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, getLocation(passage).getX(), getLocation(passage).getY(), false);
    }

    private void walkFurtherInsideCave() {
        WorldArea worldArea = new WorldArea(new WorldPoint(2980,4253,2),new WorldPoint(2987,4259,2));
        if (client.getAccountType()== AccountType.NORMAL)
        {
            worldArea = new WorldArea(new WorldPoint(2979,4380,2),new WorldPoint(2984,4385,2));
        }
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(worldArea.toWorldPointList().size());
        WorldPoint randomPoint = worldArea.toWorldPointList().get(index);
        int x = randomPoint.getX() - client.getBaseX();
        int y = randomPoint.getY() - client.getBaseY();
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    private MenuEntry attackCorp() {
        NPC corp = new NPCQuery()
                .idEquals(NpcID.CORPOREAL_BEAST)
                .result(client)
                .nearestTo(client.getLocalPlayer());
        if (corp==null) return null;
        return createMenuEntry(corp.getIndex(), MenuAction.NPC_SECOND_OPTION, getLocation(corp).getX(), getLocation(corp).getY(), false);
    }
    private MenuEntry equipGamesNeck() {
        if (gamesNeckEquipped) return null;
        //not sure of a better way to do this as games neck ID's don't seem to correlate with charges)
        HashMap<Integer,Integer> gamesNecks = new HashMap<>();
        gamesNecks.put(ItemID.GAMES_NECKLACE1,1);
        gamesNecks.put(ItemID.GAMES_NECKLACE2,2);
        gamesNecks.put(ItemID.GAMES_NECKLACE3,3);
        gamesNecks.put(ItemID.GAMES_NECKLACE4,4);
        gamesNecks.put(ItemID.GAMES_NECKLACE5,5);
        gamesNecks.put(ItemID.GAMES_NECKLACE6,6);
        gamesNecks.put(ItemID.GAMES_NECKLACE7,7);
        gamesNecks.put(ItemID.GAMES_NECKLACE8,8);
        List<Integer> gamesNeckList = gamesNecks.keySet().stream()
                .filter(necklace -> getInventoryItem(necklace) != null)
                .collect(Collectors.toList());

        //method to get the lowest charge necklace if it exists in inventory
        int gamesNeck = 0;
        if (gamesNeckList.size()>0)
        {
            gamesNeck = gamesNeckList
                    .stream()
                    .min(Comparator.comparing(gamesNecks::get))
                    .orElse(null);
        }
        if (gamesNeck == 0 ) return null;

        Widget necklace = getInventoryItem(gamesNeck);
        if (necklace== null) return null;
        return createMenuEntry(3, MenuAction.CC_OP, necklace.getIndex(), WidgetInfo.INVENTORY.getId(), false);
    }


    private boolean specEnabled() {
        return client.getVarpValue(301)==1;
    }

    private Widget getInventoryItem(int id) {
        client.runScript(6009, 9764864, 28, 1, -1);
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        Widget bankInventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (bankInventoryWidget!=null && !bankInventoryWidget.isHidden())
        {
            return getWidgetItem(bankInventoryWidget,id);
        }
        if (inventoryWidget!=null) //if hidden check exists then you can't access inventory from any tab except inventory
        {
            return getWidgetItem(inventoryWidget,id);
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

    private Point getLocation(TileObject tileObject) {
        if (tileObject == null) {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject) {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    private Point getLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private void setMenuEntry(MenuOptionClicked event, MenuEntry menuEntry){
        event.setId(menuEntry.getIdentifier());
        event.setMenuAction(menuEntry.getType());
        event.setParam0(menuEntry.getParam0());
        event.setParam1(menuEntry.getParam1());
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        Actor target = hitsplatApplied.getActor();
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        // Ignore all hitsplats other than mine
        if (!hitsplat.isMine() || target == client.getLocalPlayer() || hitsplat.getAmount()==0) return;
        if (!target.getName().contains("Corporeal Beast")) return;

        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.BANDOS_GODSWORD))
        {
            godswordDamage += hitsplat.getAmount();
            if (godswordDamage>=config.godswordDamage())
            {
                pluginPaused = true;
                notifier.notify("Corporeal Beast is specced down.");
            }
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.ARCLIGHT))
        {
            arclightHits++;
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT).contains(ItemID.DRAGON_WARHAMMER))
        {
            hammerHits++;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains("Your Corporeal Beast kill count is: "))
        {
            reset();
        }
    }
}