/*
Originally made by TP.
 */

package net.runelite.client.plugins.oneclickzmi;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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

	private final int STAMINA_DOSE = 12631;
	private final int MEDIUM_POUCH = 5510;
	private final int MEDIUM_POUCH_DECAYED = 5511;
	private final int LARGE_POUCH = 5512;
	private final int LARGE_POUCH_DECAYED = 5513;
	private final int GIANT_POUCH = 5514;
	private final int GIANT_POUCH_DECAYED = 5515;
	private final int LADDER = 29635;
	@Inject
	private Client client;
	@Inject
	private oneClickZMIConfig config;
	private int timeout = 0;
	private int ESSENCE;
	private String state = "BANK";
	private String pouch_repair_state = "CAST_NPC_CONTACT";
	private String eat_food_state = "WITHDRAW";
	private String drink_stam_state = "WITHDRAW";

	@Provides
	oneClickZMIConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(oneClickZMIConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	private void reset()
	{
		timeout = 0;
		state = "BANK";
		pouch_repair_state = "CAST_NPC_CONTACT";
		eat_food_state = "WITHDRAW";
		drink_stam_state = "WITHDRAW";
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (timeout > 0)
		{
			timeout--;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException
	{
		if (event.getMenuOption().equals("<col=00ff00>One Click ZMI"))
			handleClick(event);
	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		String text;

		if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN)
			return;

		else if (client.getLocalPlayer().getAnimation() == 791)
		{
			text = "<col=00ff00>Runecrafting";
		}
		else
		{
			text = "<col=00ff00>One Click ZMI";
		}
		this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
				.getId(), 0, 0, 0, true);
		//Ethan Vann the goat. Allows for left clicking anywhere when bank open instead of withdraw/deposit taking priority
		client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
	}

	private void handleClick(MenuOptionClicked event)
	{

		if (timeout != 0)
		{
			log.debug("Consuming event because timeout is not 0");
			event.consume();
			return;

		}
		if ((client.getLocalPlayer().isMoving()
				|| client.getLocalPlayer().getPoseAnimation()
				!= client.getLocalPlayer().getIdlePoseAnimation()
				|| client.getLocalPlayer().getAnimation() == 791)
				& !isBankOpen()) //for some reason it consumes the first click at the bank?
		{
			log.debug("Consume event because not idle?");
			event.consume();
			return;
		}

		if ((getInventoryItem(MEDIUM_POUCH_DECAYED) != null || getInventoryItem(LARGE_POUCH_DECAYED) != null || getInventoryItem(GIANT_POUCH_DECAYED) != null) && !isBankOpen()) //only repairs pouch when the bank is closed naturally so not to mess with state if mid banking
		{
			log.debug("pouch_repair_state = " + pouch_repair_state);
			switch (pouch_repair_state)
			{
				case "CAST_NPC_CONTACT":
					event.setMenuEntry(castNpcContact());
					pouch_repair_state = "CONTINUE_1";
					timeout += 13; //adds timeout after casting NPC contact;
					break;
				case "CONTINUE_1":
					event.setMenuEntry(continueChat1());
					pouch_repair_state = "CONTINUE_2";
					timeout += 2; //adds timeout for next chat box to load
					break;
				case "CONTINUE_2":
					event.setMenuEntry(continueChat2());
					pouch_repair_state = "CAST_NPC_CONTACT";
					timeout += 2; //adds a timeout to allow for pouches to repair to prevent npc contact from being recasted
					break;
			}
			return;
		}

		if ((client.getEnergy() < 80 || client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) //if run energy below threshold or stamina not active
				&& config.drinkStamina() && isBankOpen()
				&& (getEmptySlots() > 0 || getInventoryItem(STAMINA_DOSE) != null))
		{
			log.debug("drink_stam_state = " + drink_stam_state);
			switch (drink_stam_state)
			{
				case "WITHDRAW":
					event.setMenuEntry(withdrawStamina());
					drink_stam_state = "DRINK";
					timeout += 1; //needs to wait a tick for the potion to withdraw from bank.
					break;

				case "DRINK":
					if (getInventoryItem(STAMINA_DOSE) != null)
					{
						event.setMenuEntry(drinkStamina());
					}

					drink_stam_state = "WITHDRAW";
					break;
			}
			return;
		}

		if (client.getBoostedSkillLevel(Skill.HITPOINTS) < 70 && isBankOpen())
		{
			log.debug("eat_food_state = " + eat_food_state);
			switch (eat_food_state)
			{
				case "WITHDRAW":
					event.setMenuEntry(withdrawFood());
					eat_food_state = "EAT";
					timeout += 1; //needs to wait a tick for the food to withdraw from bank.
					break;

				case "EAT":
					if (getInventoryItem(config.foodID()) != null)
					{
						event.setMenuEntry(eatFood());
					}

					eat_food_state = "WITHDRAW";
					break;
			}
			return;
		}

		log.debug("state = " + state);
		switch (state)
		{
			case "BANK":
				if (getBanker() != null)
				{
					event.setMenuEntry(getBankMES());
					timeout += 1; //adds 1t timeout when clicking bank before movement detection kicks in
					state = "DEPOSIT_ALL";
				}
				else
				{
					log.debug("Banker is null");
				}
				break;

			case "DEPOSIT_ALL":
				if (!isBankOpen())
				{
					return;
				}
				event.setMenuEntry(depositAll());
				state = "WITHDRAW_DAEYALT";
				break;

			case "WITHDRAW_DAEYALT":
				event.setMenuEntry(withdrawDaeyalt());
				if (getInventoryItem(MEDIUM_POUCH) == null) //if no Medium pouch assumes no large, skips straight to clicking altar
				{
					state = "CLICK_ALTAR";
					break;
				}
				state = "FILL_MEDIUM";
				break;

			case "FILL_MEDIUM":
				event.setMenuEntry(fillMediumPouch());
				if (getInventoryItem(LARGE_POUCH) == null)
				{
					state = "WITHDRAW_DAEYALT_2";
					break;
				}
				state = "FILL_LARGE";
				break;

			case "FILL_LARGE":
				event.setMenuEntry(fillLargePouch());
				state = "WITHDRAW_DAEYALT_2";
				break;

			case "WITHDRAW_DAEYALT_2":
				event.setMenuEntry(withdrawDaeyalt());
				if (getInventoryItem(GIANT_POUCH) == null)
				{
					state = "CLICK_ALTAR";
					break;
				}
				state = "FILL_GIANT";
				break;

			case "FILL_GIANT":
				event.setMenuEntry(fillGiantPouch());
				state = "WITHDRAW_DAEYALT_3";
				break;

			case "WITHDRAW_DAEYALT_3":
				event.setMenuEntry(withdrawDaeyalt());
				state = "CLICK_ALTAR";
				break;

			case "CLICK_ALTAR":
				event.setMenuEntry(getAltarMES());
				timeout += 4;
				if (getInventoryItem(MEDIUM_POUCH) == null) //if no Medium pouch assumes no large, skips straight to teleport after first runecraft
				{
					state = "TELEPORT";
					break;
				}
				state = "EMPTY_MEDIUM";
				break;

			case "EMPTY_MEDIUM":
				event.setMenuEntry(emptyMedPouch());
				if (getInventoryItem(LARGE_POUCH) == null)
				{
					state = "CLICK_ALTAR_2";
					break;
				}
				state = "EMPTY_LARGE";
				break;

			case "EMPTY_LARGE":
				event.setMenuEntry(emptyLargePouch());
				state = "CLICK_ALTAR_2";
				break;

			case "CLICK_ALTAR_2":
				event.setMenuEntry(getAltarMES());
				timeout += 4;
				if (getInventoryItem(GIANT_POUCH) == null)
				{
					state = "TELEPORT";
					break;
				}
				state = "EMPTY_GIANT";
				break;

			case "EMPTY_GIANT":
				event.setMenuEntry(emptyGiantPouch());
				state = "CLICK_ALTAR_3";
				break;

			case "CLICK_ALTAR_3":
				event.setMenuEntry(getAltarMES());
				timeout += 4;
				state = "TELEPORT";
				break;

			case "TELEPORT":
				event.setMenuEntry(castTeleport());
				timeout += 5;
				state = "CLICK_LADDER";
				break;

			case "CLICK_LADDER":
				event.setMenuEntry(clickLadder());
				timeout += 4; //add timeout until character starts moving else you can spam click through states. timeout value is irrelevant just needs to be >0
				state = "BANK";
				break;
		}

	}

	private boolean isBankOpen()
	{
		return client.getItemContainer(InventoryID.BANK) != null;
	}

	@Nullable
	private Collection<WidgetItem> getInventoryItems()
	{
		Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

		if (inventory == null)
		{
			return null;
		}

		return new ArrayList<>(inventory.getWidgetItems());
	}


	private MenuEntry depositAll()
	{
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				786474,
				true);
	}

	private MenuEntry getBankMES()
	{
		return createMenuEntry(
				getBanker().getIndex(),
				MenuAction.NPC_FIRST_OPTION,
				0,
				0,
				false);
	}

	private MenuEntry withdrawDaeyalt()
	{
		if (config.essenceType() == EssenceType.Daeyalt)
		{
			ESSENCE = 24704;
		}
		else
		{
			ESSENCE = 7936;
		}
		return createMenuEntry(
				7,
				MenuAction.CC_OP_LOW_PRIORITY,
				getBankIndex(ESSENCE),
				786445,
				false);
	}

	private MenuEntry drinkStamina()
	{
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(STAMINA_DOSE).getIndex(),
				983043,
				false);
	}

	private MenuEntry withdrawStamina()
	{
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				getBankIndex(STAMINA_DOSE),
				786445,
				false);
	}

	private MenuEntry withdrawFood()
	{
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				getBankIndex(config.foodID()),
				786445,
				false);
	}

	private MenuEntry eatFood()
	{
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(config.foodID()).getIndex(),
				983043,
				false);
	}


	private int getBankIndex(int id)
	{
		WidgetItem bankItem = new BankItemQuery()
				.idEquals(id)
				.result(client)
				.first();
		return bankItem.getWidget().getIndex();
	}

	private MenuEntry fillMediumPouch()
	{
		return createMenuEntry(
				9,
				MenuAction.CC_OP,
				getInventoryItem(MEDIUM_POUCH).getIndex(),
				983043,
				true);
	}

	private MenuEntry fillLargePouch()
	{
		return createMenuEntry(
				9,
				MenuAction.CC_OP,
				getInventoryItem(LARGE_POUCH).getIndex(),
				983043,
				true);
	}

	private MenuEntry fillGiantPouch()
	{
		return createMenuEntry(
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getInventoryItem(GIANT_POUCH).getIndex(),
				983043,
				false);
	}

	private MenuEntry emptyMedPouch()
	{
		return createMenuEntry(
				5510,
				MenuAction.ITEM_SECOND_OPTION,
				getInventoryItem(MEDIUM_POUCH).getIndex(),
				9764864,
				true);
	}

	private MenuEntry emptyLargePouch()
	{
		return createMenuEntry(
				5512,
				MenuAction.ITEM_SECOND_OPTION,
				getInventoryItem(LARGE_POUCH).getIndex(),
				9764864,
				true);
	}

	private MenuEntry emptyGiantPouch()
	{
		return createMenuEntry(
				5514,
				MenuAction.ITEM_SECOND_OPTION,
				getInventoryItem(GIANT_POUCH).getIndex(),
				9764864,
				false);
	}

	private GameObject getNearestAltar()
	{
		return new GameObjectQuery()
				.idEquals(29631)
				.result(client)
				.nearestTo(client.getLocalPlayer());

	}

	private MenuEntry getAltarMES()
	{
		GameObject alter = getNearestAltar();
		return createMenuEntry(
				29631,
				MenuAction.GAME_OBJECT_FIRST_OPTION,
				getLocation(alter).getX(),
				getLocation(alter).getY(),
				true);
	}

	private MenuEntry castTeleport()
	{
		return createMenuEntry(
				1,
				MenuAction.CC_OP,
				-1,
				WidgetInfo.SPELL_OURANIA_TELEPORT.getId(),
				false);
	}

	private GameObject findLadder()
	{
		return new GameObjectQuery()
				.idEquals(LADDER)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	private MenuEntry clickLadder()
	{
		GameObject ladder = findLadder();
		return createMenuEntry(
				29635,
				MenuAction.GAME_OBJECT_FIRST_OPTION,
				getLocation(ladder).getX(),
				getLocation(ladder).getY(),
				true);
	}

	private NPC getBanker()
	{
		return new NPCQuery()
				.idEquals(8132)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}

	private MenuEntry castNpcContact()
	{
		return createMenuEntry(
				2,
				MenuAction.CC_OP,
				-1,
				WidgetInfo.SPELL_NPC_CONTACT.getId(),
				false);
	}

	private MenuEntry continueChat1()
	{
		return createMenuEntry(
				0,
				MenuAction.WIDGET_TYPE_6,
				-1,
				15138821,
				true);
	}

	private MenuEntry continueChat2()
	{
		return createMenuEntry(
				0,
				MenuAction.WIDGET_TYPE_6,
				-1,
				14221317,
				false);
	}

	private Point getLocation(TileObject tileObject)
	{
		if (tileObject == null)
		{
			return new Point(0, 0);
		}
		if (tileObject instanceof GameObject)
			return ((GameObject) tileObject).getSceneMinLocation();
		return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
	}

	private WidgetItem getInventoryItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}

	private int getEmptySlots()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return 28 - inventoryWidget.getWidgetItems().size();
		}
		else
		{
			return -1;
		}
	}

	public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick)
	{
		return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
				.setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
	}
}

/*

TODO
doesnt work if in a bank tab for some reason. maybe unavoidable. not a problem really.
bank withdraw quantity must be set to one, kinda aids, not sure if theres'a  way to get the type (menu index) of an item
for some reason can still withdraw items from bank?
*/
