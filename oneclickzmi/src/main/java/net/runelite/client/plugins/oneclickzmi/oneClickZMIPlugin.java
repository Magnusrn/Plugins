/*
Originally made by TP.
 */

package net.runelite.client.plugins.oneclickzmi;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
		name = "One Click ZMI Runecrafting",
		description = "Computer aided gaming. Set bank withdraw Quantity to 1 and make sure to have 1 dose stams, food in the bank, runes for npc contact and ourania teleport and bank setup correctly with fillers. Must also be in the main bank tab. Doesn't support small pouch or large pouch on it's own. credit TP",
		tags = {"zmi,one click,ourania,rc"},
		enabledByDefault = false
)

@Slf4j
public class oneClickZMIPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private oneClickZMIConfig config;

	private int timeout = 0;
	private String state = "BANK";
	private int cachedXP = 0;
	private boolean craftedRunes = false;

	@Provides
	oneClickZMIConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(oneClickZMIConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		reset();
	}

	private void reset() {
		timeout = 0;
		state = "BANK";
		craftedRunes = false;
		cachedXP = 0;
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		if (timeout > 0)
		{
			timeout--;
		}
		if (cachedXP == 0)
		{
			cachedXP = client.getSkillExperience(Skill.RUNECRAFT);
		}
		System.out.println(state);
	}

	@Subscribe
	protected void onStatChanged(StatChanged event) {
		//on login this method triggers going from 0 to players current XP. all xp drops(even on leagues etc) should be below 50k and this method requires 77 rc.
		if (event.getSkill() == Skill.RUNECRAFT && event.getXp()- cachedXP <1000)
		{
			craftedRunes = true;
			cachedXP = client.getSkillExperience(Skill.RUNECRAFT);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().contains("There are no essences in this pouch."))
		{
			//not perfect but it works, prevents spam crafting if pouch is empty due to broken pouches previously
			craftedRunes = true ;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
		if (event.getMenuOption().equals("<col=00ff00>One Click ZMI"))
			handleClick(event);
	}

	@Subscribe
	private void onClientTick(ClientTick event) {
		if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) return;
		String text = "<col=00ff00>One Click ZMI";
		client.insertMenuItem(text, "", MenuAction.UNKNOWN.getId(), 0, 0, 0, true);
		//Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
		client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
	}

	private void handleClick(MenuOptionClicked event) {
		if (config.debug())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE,"",state,"");
		}
		if (config.consumeClicks() && (client.getLocalPlayer().isMoving()
				|| client.getLocalPlayer().getPoseAnimation()
				!= client.getLocalPlayer().getIdlePoseAnimation()
				|| client.getLocalPlayer().getAnimation() == 791)
				& !bankOpen()) //for some reason it consumes the first click at the bank?
		{
			log.debug("Consume event because not idle?");
			event.consume();
			return;
		}
		if (AboveLadder())
		{
			event.setMenuEntry(clickLadder());
			return;
		}
		if (timeout != 0)
		{
			log.debug("Consuming event because timeout is not 0");
			event.consume();
			return;
		}

		if (handlePouchRepair()!=null && !bankOpen() && isNearAltar())
		{
			event.setMenuEntry(handlePouchRepair());
			return;
		}

		if ((client.getEnergy() < 80 || client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) //if run energy below threshold or stamina not active
				&& config.drinkStamina() && bankOpen()
				&& (getEmptySlots() > 0 || getInventoryItem(ItemID.STAMINA_POTION1) != null))
		{
			event.setMenuEntry(withdrawStamina());
			timeout += 1; //needs to wait a tick for the potion to withdraw from bank.
			if (getInventoryItem(ItemID.STAMINA_POTION1) != null)
			{
				event.setMenuEntry(drinkStamina());
			}
			return;
		}

		if (client.getRealSkillLevel(Skill.HITPOINTS) - client.getBoostedSkillLevel(Skill.HITPOINTS) > 25  && bankOpen())
		{
			event.setMenuEntry(withdrawFood());
			timeout += 1; //needs to wait a tick for the food to withdraw from bank.
			if (getInventoryItem(config.foodID()) != null)
			{
				event.setMenuEntry(eatFood());
			}
			return;
		}

		if (getInventoryItem(ItemID.COLOSSAL_POUCH)!=null || getInventoryItem(ItemID.COLOSSAL_POUCH_26786) !=null)
		{
			colossalPouchHandler(event);
			return;
		}
		smallerPouchHandler(event);
	}

	private boolean isNearAltar() {
		return client.getLocalPlayer().getWorldLocation().distanceTo(getAltar().getWorldLocation())<5;
	}

	private void colossalPouchHandler(MenuOptionClicked event) {
		switch (state)
		{
			case "BANK":
				if (getBanker() != null)
				{
					event.setMenuEntry(getBankMES());
					timeout += 1; //adds 1t timeout when clicking bank before movement detection kicks in
					state = "DEPOSIT_ALL";
				}
				return;
			case "DEPOSIT_ALL":
				if (!bankOpen())
				{
					return;
				}
				event.setMenuEntry(depositAll());
				state = "WITHDRAW_ESSENCE";
				return;
			case "WITHDRAW_ESSENCE":
				event.setMenuEntry(withdrawEssence());
				state = "FILL_POUCH";
				return;
			case "FILL_POUCH":
				event.setMenuEntry(fillColossalPouch());
				state = "WITHDRAW_ESSENCE_2";
				return;
			case "WITHDRAW_ESSENCE_2":
				event.setMenuEntry(withdrawEssence());
				state = "FILL_POUCH_2";
				return;
			case "FILL_POUCH_2":
				event.setMenuEntry(fillColossalPouch());
				state = "WITHDRAW_ESSENCE_3";
				return;
			case "WITHDRAW_ESSENCE_3":
				event.setMenuEntry(withdrawEssence());
				state = "CLICK_ALTAR";
				return;
			case "CLICK_ALTAR":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				state = "EMPTY_COLOSSAL";
			case "EMPTY_COLOSSAL":
				event.setMenuEntry(emptyColossalPouch());
				state = "CLICK_ALTAR_2";
				return;
			case "CLICK_ALTAR_2":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				state = "EMPTY_COLOSSAL_2";
			case "EMPTY_COLOSSAL_2":
				event.setMenuEntry(emptyColossalPouch());
				state = "CLICK_ALTAR_3";
				return;
			case "CLICK_ALTAR_3":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				state = "EMPTY_COLOSSAL_3";
			case "EMPTY_COLOSSAL_3":
				event.setMenuEntry(emptyColossalPouch());
				state = "CLICK_ALTAR_4";
				return;
			case "CLICK_ALTAR_4":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				state = "TELEPORT";
			case "TELEPORT":
				event.setMenuEntry(castTeleport());
				timeout += 5;
				state = "BANK";
		}
	}

	private void smallerPouchHandler(MenuOptionClicked event) {
		switch (state)
		{
			case "BANK":
				if (getBanker() != null)
				{
					event.setMenuEntry(getBankMES());
					timeout += 1; //adds 1t timeout when clicking bank before movement detection kicks in
					state = "DEPOSIT_ALL";
				}
				return;
			case "DEPOSIT_ALL":
				if (!bankOpen())
				{
					return;
				}
				event.setMenuEntry(depositAll());
				state = "WITHDRAW_ESSENCE";
				return;
			case "WITHDRAW_ESSENCE":
				event.setMenuEntry(withdrawEssence());
				if (getInventoryItem(ItemID.MEDIUM_POUCH) == null) //if no Medium pouch assumes no large, skips straight to clicking altar
				{
					state = "CLICK_ALTAR";
					return;
				}
				state = "FILL_MEDIUM";
				return;
			case "FILL_MEDIUM":
				event.setMenuEntry(fillMediumPouch());
				if (getInventoryItem(ItemID.LARGE_POUCH) == null)
				{
					state = "WITHDRAW_ESSENCE_2";
					return;
				}
				state = "FILL_LARGE";
				return;
			case "FILL_LARGE":
				event.setMenuEntry(fillLargePouch());
				state = "WITHDRAW_ESSENCE_2";
				return;
			case "WITHDRAW_ESSENCE_2":
				event.setMenuEntry(withdrawEssence());
				if (getInventoryItem(ItemID.GIANT_POUCH) == null)
				{
					state = "CLICK_ALTAR";
					return;
				}
				state = "FILL_GIANT";
				return;
			case "FILL_GIANT":
				event.setMenuEntry(fillGiantPouch());
				state = "WITHDRAW_ESSENCE_3";
				return;
			case "WITHDRAW_ESSENCE_3":
				event.setMenuEntry(withdrawEssence());
				state = "CLICK_ALTAR";
				return;
			case "CLICK_ALTAR":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				if (getInventoryItem(ItemID.MEDIUM_POUCH) == null) //if no Medium pouch assumes no large, skips straight to teleport after first runecraft
				{
					state = "TELEPORT";
					break;
				}
				state = "EMPTY_MEDIUM";
			case "EMPTY_MEDIUM":
				event.setMenuEntry(emptyMedPouch());
				craftedRunes = false;
				if (getInventoryItem(ItemID.LARGE_POUCH) == null)
				{
					state = "CLICK_ALTAR_2";
					return;
				}
				state = "EMPTY_LARGE";
				return;
			case "EMPTY_LARGE":
				event.setMenuEntry(emptyLargePouch());
				state = "CLICK_ALTAR_2";
				return;
			case "CLICK_ALTAR_2":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				if (getInventoryItem(ItemID.GIANT_POUCH) == null)
				{
					state = "TELEPORT";
					break;
				}
				state = "EMPTY_GIANT";
			case "EMPTY_GIANT":
				event.setMenuEntry(emptyGiantPouch());
				state = "CLICK_ALTAR_3";
				return;
			case "CLICK_ALTAR_3":
				if (!craftedRunes)
				{
					event.setMenuEntry(getAltarMES());
					timeout +=1;
					return;
				}
				craftedRunes = false;
				state = "TELEPORT";
			case "TELEPORT":
				event.setMenuEntry(castTeleport());
				timeout += 5;
				state = "BANK";
		}
	}

	private boolean bankOpen() {
		return client.getItemContainer(InventoryID.BANK) != null;
	}

	private MenuEntry depositAll() {
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				786474,
				true);
	}

	private MenuEntry getBankMES() {
		return createMenuEntry(
				getBanker().getIndex(),
				MenuAction.NPC_FIRST_OPTION,
				0,
				0,
				false);
	}

	private MenuEntry withdrawEssence() {
		int ESSENCE = config.essenceType().getID();
		return createMenuEntry(
				7,
				MenuAction.CC_OP_LOW_PRIORITY,
				getBankIndex(ESSENCE),
				786445,
				false);
	}

	private MenuEntry drinkStamina() {
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(ItemID.STAMINA_POTION1).getIndex(),
				983043,
				false);
	}

	private MenuEntry withdrawStamina() {
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				getBankIndex(ItemID.STAMINA_POTION1),
				786445,
				false);
	}

	private MenuEntry withdrawFood() {
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				getBankIndex(config.foodID()),
				786445,
				false);
	}

	private MenuEntry eatFood() {
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(config.foodID()).getIndex(),
				983043,
				false);
	}


	private int getBankIndex(int id) {
		WidgetItem bankItem = new BankItemQuery()
				.idEquals(id)
				.result(client)
				.first();
		return bankItem.getWidget().getIndex();
	}

	private MenuEntry fillMediumPouch() {
		return createMenuEntry(
				9,
				MenuAction.CC_OP,
				getInventoryItem(ItemID.MEDIUM_POUCH).getIndex(),
				983043,
				true);
	}

	private MenuEntry fillLargePouch() {
		return createMenuEntry(
				9,
				MenuAction.CC_OP,
				getInventoryItem(ItemID.LARGE_POUCH).getIndex(),
				983043,
				true);
	}

	private MenuEntry fillGiantPouch() {
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(ItemID.GIANT_POUCH).getIndex(),
				983043,
				false);
	}

	private MenuEntry fillColossalPouch() {
		Widget pouch = getInventoryItem(ItemID.COLOSSAL_POUCH);
		if (getInventoryItem(ItemID.COLOSSAL_POUCH_26786)!=null)
		{
			pouch = getInventoryItem(ItemID.COLOSSAL_POUCH_26786);
		}
		return createMenuEntry(
				9,
				MenuAction.CC_OP,
				pouch.getIndex(),
				983043,
				true);
	}

	private MenuEntry emptyPouchMES(Widget pouch) {
		return createMenuEntry(3, MenuAction.CC_OP, pouch.getIndex(), WidgetInfo.INVENTORY.getId(), false);
	}
	private MenuEntry emptyMedPouch() {
		Widget pouch = getInventoryItem(ItemID.MEDIUM_POUCH);
		return emptyPouchMES(pouch);
	}

	private MenuEntry emptyLargePouch() {
		Widget pouch = getInventoryItem(ItemID.LARGE_POUCH);
		return emptyPouchMES(pouch);
	}

	private MenuEntry emptyGiantPouch() {
		Widget pouch = getInventoryItem(ItemID.GIANT_POUCH);
		return emptyPouchMES(pouch);
	}

	private MenuEntry emptyColossalPouch() {
		Widget pouch = getInventoryItem(ItemID.COLOSSAL_POUCH);
		if (getInventoryItem(ItemID.COLOSSAL_POUCH_26786)!=null)
		{
			pouch = getInventoryItem(ItemID.COLOSSAL_POUCH_26786);
		}
		return emptyPouchMES(pouch);
	}

	private GameObject getAltar() {
		return new GameObjectQuery()
				.idEquals(29631)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	private MenuEntry getAltarMES() {
		GameObject alter = getAltar();
		return createMenuEntry(
				29631,
				MenuAction.GAME_OBJECT_FIRST_OPTION,
				getLocation(alter).getX(),
				getLocation(alter).getY(),
				true);
	}

	private MenuEntry castTeleport() {
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				WidgetInfo.SPELL_OURANIA_TELEPORT.getId(),
				false);
	}

	private boolean AboveLadder() {
		return new GameObjectQuery()
				.idEquals(411)
				.result(client)
				.nearestTo(client.getLocalPlayer()) != null;
	}

	private GameObject findLadder() {
		int LADDER = 29635;
		return new GameObjectQuery()
				.idEquals(LADDER)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	private MenuEntry clickLadder() {
		GameObject ladder = findLadder();
		return createMenuEntry(
				findLadder().getId(),
				MenuAction.GAME_OBJECT_FIRST_OPTION,
				getLocation(ladder).getX(),
				getLocation(ladder).getY(),
				true);
	}

	private NPC getBanker() {
		return new NPCQuery()
				.idEquals(8132)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	private MenuEntry castNpcContact() {
		return createMenuEntry(
				2,
				MenuAction.CC_OP,
				-1,
				WidgetInfo.SPELL_NPC_CONTACT.getId(),
				false);
	}

	private MenuEntry handlePouchRepair() {
		if (client.getWidget(231,6)!=null && client.getWidget(231, 6).getText().equals("What do you want? Can't you see I'm busy?"))
		{
			return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, -1, 15138821, false);
		}
		//if player doesn't have abyssal pouch in bank
		if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(2)!=null && client.getWidget(219,1).getChild(2).getText().equals("Can you repair my pouches?"))
		{
			return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, 2, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
		}
		//if player has abyssal pouch in bank
		if (client.getWidget(219,1)!=null && client.getWidget(219,1).getChild(1)!=null && client.getWidget(219,1).getChild(1).getText().equals("Can you repair my pouches?"))
		{
			return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, 1, WidgetInfo.DIALOG_OPTION_OPTION1.getId(), false);
		}

		if (client.getWidget(217,6)!=null && client.getWidget(217,6).getText().equals("Can you repair my pouches?"))
		{
			return createMenuEntry(0, MenuAction.WIDGET_CONTINUE, -1, 14221317, false);
		}

		List<Integer> brokenPouches = Arrays.asList(ItemID.MEDIUM_POUCH_5511,ItemID.LARGE_POUCH_5513,ItemID.GIANT_POUCH_5515,ItemID.COLOSSAL_POUCH_26786);
		if (brokenPouches.stream().anyMatch(pouch -> client.getItemContainer(InventoryID.INVENTORY).contains(pouch)))
		{
			return castNpcContact();
		}
		return null;
	}


	private Point getLocation(TileObject tileObject) {
		if (tileObject == null)
		{
			return new Point(0, 0);
		}
		if (tileObject instanceof GameObject)
			return ((GameObject) tileObject).getSceneMinLocation();
		return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
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

	public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
		return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
				.setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
	}
}