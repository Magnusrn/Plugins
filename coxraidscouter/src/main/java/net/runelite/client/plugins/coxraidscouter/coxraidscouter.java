package net.runelite.client.plugins.coxraidscouter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.*;
import net.runelite.api.queries.DecorativeObjectQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.MenuAction;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.raids.RaidsConfig;
import net.runelite.client.plugins.raids.RaidsPlugin;
import net.runelite.client.util.Text;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
@PluginDescriptor(
	name = "Cox Raid Scouter",
	description = "Read github readme for setup",
	tags = {"cox", "raid", "scouter"}
)
@Slf4j
@PluginDependency(RaidsPlugin.class)
public class coxraidscouter extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private coxraidscouterconfig config;

	@Inject
	private RaidsConfig coxhelperconfig;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Notifier notifier;

	@Provides
	coxraidscouterconfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(coxraidscouterconfig.class);
	}

	private int timeout = 0;
	private String state = "read";
	private String raidLeaverState = "Awaiting Raider";
	private boolean raidFound = false;
	private boolean raidStarted = false;
	private String raidLayout = null;
	private String lastRooms = null;

	@Override
	protected void startUp() throws Exception {
		configManager.setConfiguration("raids","layoutMessage",true);
		for (Plugin plugin: pluginManager.getPlugins())
		{
			if (plugin.getName().equals("Chambers Of Xeric"))
			{
				pluginManager.setPluginEnabled(plugin,true);
			}
		}
	}

	private void reset()
	{
		timeout=0;
		state ="read";
		raidLeaverState ="Awaiting Raider";
		raidFound = false;
		raidLayout = null;
		raidStarted = false;
	}

	private Point getLocation(TileObject tileObject)
	{
		if (tileObject instanceof GameObject)
		{

			return ((GameObject) tileObject).getSceneMinLocation();
		}
		else
		{
			return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
		}
	}

	private void pressEnter()
	{
		KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED);
		client.getCanvas().dispatchEvent(keyPress);
		KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED);
		client.getCanvas().dispatchEvent(keyRelease);
		KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED);
		client.getCanvas().dispatchEvent(keyTyped);
	}

	private void Print(String string) //used for debugging, puts a message to the in game chat.
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"",string,"");
	}

	private void StateHandler() throws IOException {
		if (timeout!=0)
		{
			return;
		}
		System.out.println("State: "+state);

		switch (state)
		{
			case "read":
				DecorativeObject recruitingBoardObject = new DecorativeObjectQuery()
						.idEquals(ObjectID.RECRUITING_BOARD)
						.result(client)
						.nearestTo(client.getLocalPlayer());
				if (recruitingBoardObject != null) {
					client.invokeMenuAction("Read", "<col=ffff>Recruiting board", recruitingBoardObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), getLocation(recruitingBoardObject).getX(), getLocation(recruitingBoardObject).getY());
					state = "check for existing raid";
				}
				break;

			case "check for existing raid":
				if (client.getWidget(229,1)!=null) //if raid occupied message shows
				{
					if (client.getWidget(229,1).getText().startsWith("Your party has embarked on its raid"))
					{
						if (raidStarted)
						{
							reset(); //restarts the scouter when the raid is started
						}
					}
				}

				if (client.getWidget(499, 2) != null) //if board is visible scouting can be immediately started
				{
					state = "make party";
					raidFound = false;
				}
				break;

			case "make party":
				if (client.getWidget(499, 58) != null) {
					client.invokeMenuAction("Make party", "", 1, MenuAction.CC_OP.getId(), -1, 32702522);
					state = "enter";
				}
				break;
			case "enter":
				GameObject EnterRaidObject = new GameObjectQuery()
						.idEquals(ObjectID.CHAMBERS_OF_XERIC)
						.result(client)
						.nearestTo(client.getLocalPlayer());
				if (EnterRaidObject != null) {
					client.invokeMenuAction("Enter", "<col=ffff>Chambers of Xeric", EnterRaidObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), getLocation(EnterRaidObject).getX(), getLocation(EnterRaidObject).getY());

					state = "start raid";
				}
				break;
			case "start raid": //main logic for leaving raid, either starts and continues scouting or waits til raider joins then leaves and rejoins cc

				if (config.fivehHandler()) //clicks through the 5h check at raid entrance if visible.
				{
					if (client.getWidget(229,1) != null)
					{
						if (client.getWidget(229, 1).getText().startsWith("You have been logged in for a very long time"))
						{
							client.invokeMenuAction("Continue", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15007746);
							return;
						}
					}

					if (client.getWidget(219,1) != null)
					{
						if (client.getWidget(219, 1).getChild(1).getText().startsWith("Yes, and don't ask again in this session"))
						{
							client.invokeMenuAction("Continue", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 14352385);
							return;
						}
					}
				}

				if (raidFound) {
					System.out.println("raidleaverstate : " + raidLeaverState);
					if (config.autoLeaveCC()) {
						switch (raidLeaverState) {
							case "Awaiting Raider":
								if (client.getWidget(707, 3) != null) {
									client.invokeMenuAction("Chat-channel", "", 1, MenuAction.CC_OP.getId(), -1, 46333955);
									raidLeaverState = "Leaving CC";
								}
								break;
							case "Leaving CC":
								if (client.getWidget(7, 18) != null) {
									if (client.getPlayers().size() > 1) {
										if (!config.webhook().equals("")) //if webhook exists, post to webhook that raid is taken
										{
											DiscordWebhook webhook = new DiscordWebhook(config.webhook());
											webhook.setContent(client.getLocalPlayer().getName() + " - Raid Taken. Now Scouting.");
											webhook.execute();
										}
										Print("Raider in raid, should be leaving cc");
										client.invokeMenuAction("Leave", "", 6, MenuAction.CC_OP_LOW_PRIORITY.getId(), -1, 458770);
										raidLeaverState = "RejoinCC";
									}
								}
								break;
							case "RejoinCC":
								recruitingBoardObject = new DecorativeObjectQuery()
										.idEquals(ObjectID.RECRUITING_BOARD)
										.result(client)
										.nearestTo(client.getLocalPlayer());
								if (recruitingBoardObject != null && client.getWidget(7, 18) != null) {
									client.invokeMenuAction("Join", "", 1, MenuAction.CC_OP.getId(), -1, 458770);
									raidLeaverState = "PressEnter";
								}
								break;
							case "PressEnter":
								if (client.getWidget(162, 37) != null) {
									Executors.newSingleThreadExecutor() //runs pressKey function to press enter once
											.submit(this::pressEnter);
									raidLeaverState = "Delay";
								}
								break;
							case "Delay": //adds a delay after rejoining cc as sometimes the cc bugs out and doesn't post the raid started messageif raid is start quickly i think? cox problems smh
								timeout = config.leaveCCTimeout();
								raidLeaverState = "Reset";
								break;
							case "Reset": //restart scouting
								reset();
								break;
						}
					} else {
						timeout += 36000; //if raid found and auto/rejoin is disabled then wait for 6h
					}
				} else if (client.getWidget(500, 14) != null
						&& client.getWidget(500,14).getChild(9)!=null
						&& client.getWidget(500,14).getChild(9).getText().equals("Start raid")) {
					client.invokeMenuAction("Start raid", "", 1, MenuAction.CC_OP.getId(), -1, 32768014);
					state = "continue";
				}
				break;
			case "continue":
				if (client.getWidget(219, 1) != null) {
					client.invokeMenuAction("Continue", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 14352385);
					state = "climb";
				}
				break;
			case "climb":
				GameObject ClimbStepsObject = new GameObjectQuery()
						.idEquals(ObjectID.STEPS_29778)
						.result(client)
						.nearestTo(client.getLocalPlayer());
				if (ClimbStepsObject != null) {
					client.invokeMenuAction("Climb", "<col=ffff>Steps", ClimbStepsObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), getLocation(ClimbStepsObject).getX(), getLocation(ClimbStepsObject).getY());
					state = "leave raid";
				}
				break;
			case "leave raid":
				if (client.getWidget(219, 1) != null) {
					client.invokeMenuAction("Continue", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 14352385);
					state = "read";
				}
				break;
		}
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		if (event.getDecorativeObject().getId()==29776)
		{
			//board isn't clickable for after it's visible due to still being leaving raid
			timeout=config.timeout();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (event.getGameObject().getId()==29778)
		{
			//timeout to allow time for layout to post
			timeout=config.timeout();
		}
	}

	@Override
	protected void shutDown()
	{
		reset();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) throws IOException {
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (!config.webhook().equals("")) //if webhook exists, post to webhook that scouter has logged out
			{
				DiscordWebhook webhook = new DiscordWebhook(config.webhook());
				webhook.setContent(client.getLocalPlayer().getName() + " - Logged out");
				webhook.execute();
			}
			reset();
		}
	}

	@Subscribe
	private void onGameTick(GameTick event) throws IOException {
		if (timeout!=0)
		{
			timeout--;
		}
		StateHandler();
	}

	private void SendLayoutToCC(String layout)
	{
		VarClientStr var = VarClientStr.CHATBOX_TYPED_TEXT;
		String text = "/" + layout;
		client.setVar(var, text);
		Executors.newSingleThreadExecutor().submit(this::pressEnter);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) throws IOException {
		if (event.getType() == ChatMessageType.FRIENDSCHAT
			&& config.RespondToLayoutRequest()
			&& Text.removeTags(event.getMessage()).equalsIgnoreCase("?l")
			&& raidFound
		)
		{
			SendLayoutToCC(raidLayout + " " + lastRooms);
		}

		if ((Text.removeTags((event.getMessage())).endsWith("start the raid without you. Sorry."))) //Restarts the plugin if someone starts the raid currently held
		{
			raidStarted = true;
		}
		if ((Text.removeTags(event.getMessage())).startsWith("Layout"))
		{
			//kinda clunky code, can definitely be written better
			//Splits Chatmessage into list rooms and strips whitespace, also pulls raid layout
			String[] rooms = Text.removeTags(event.getMessage()).split(":")[2].split(",");
			raidLayout = Text.removeTags(event.getMessage()).split(":")[1];
			List<String> roomsTrimmed = new ArrayList<>();
			//loop through arraylist and remove whitespace
			for (String room: rooms)
			{
				roomsTrimmed.add(room.trim());
			}
			lastRooms = roomsTrimmed.toString();

			if (roomsTrimmed.size()!=5) //returns if not 3c2p (or 4c1p but these are unscoutable)
			{
				if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
				{
					Print("Raid is incorrect size");
				}
				return;
			}

			if (config.desiredRotationsToggle()) //if desiredRotations enabled then only allows specific rotations through
			{
				boolean rotationMatchFound = false;
				//a lot of bodged additions to make the string fit with the Match
				String compareRotation  = roomsTrimmed
						.toString()
						.replace("[","")
						.replace("]","")
						.replace("Tightrope","")
						.replace("Crabs","")
						.replace("Ice Demon","")
						.replace("Thieving","")
						.replaceAll(",+","")
						.replace("  "," ")
						.strip();
				List<String> allMatches = new ArrayList<String>();
				Matcher m = Pattern.compile("\\[(.*?)]")
						.matcher(config.desiredRotations());
				while (m.find()) {
					allMatches.add(m.group());
				}
				for (String item : allMatches)
				{
					item = item
							.replace(","," ")
							.replace("[","")
							.replace("]","")
							.strip();
					if (item.equals(compareRotation))
					{
						rotationMatchFound = true;
					}
				}
				if (!rotationMatchFound)
				{
					if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
					{
						Print("No rotation match found");
					}
					return;
				}
			}

			if (config.requireRope()) //checks to see if Tightrope is in raid
			{
				if (!(roomsTrimmed.contains("Tightrope")))
				{
					if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
					{
						Print("Raid doesn't contain Tightrope");
					}
					return; //returns if no tightrope
				}
			}

			if (config.requireGoodCrabs()) //checks to see if Crabs are good if they exist within raid
			{
				if(roomsTrimmed.contains("Crabs")) //If crabs exist in raid rotation
				{
					if (!(raidLayout.strip().startsWith("[SCS"))) //SCS is always good crabs so break out of if statement if it starts with SCS
					{
						if (!roomsTrimmed.get(4).equals("Crabs")) //Crabs 5th is always good crabs so return if it's not crabs
						{
							if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
							{
								Print("Raid contains bad crabs");
							}
							return; //returns if bad crabs
						}
					}
				}
			}


			for (String room: roomsTrimmed) //checks through blacklisted rooms
			{
				String[] blacklistedRooms = config.blacklistedRooms().split(",");
				for (String blacklistedRoom: blacklistedRooms)
				{
					if (room.equals(blacklistedRoom))
					{
						if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
						{
							Print("Blacklisted room found: " + room);
						}
						return; //returns if any blacklisted room in raid
					}
				}
			}

			for (String room: roomsTrimmed) //checks for Mutt/Tek(Ovl)
			{
				String[] ovlRooms = {"Tekton", "Muttadiles"};
				for (String ovlRoom: ovlRooms )
				{
					if (room.equals(ovlRoom) && config.requireOverload()) //skip if require overload disabled
					{
						if (config.SendLayoutToCC())
						{
							SendLayoutToCC(raidLayout + " " + lastRooms);
						}
						if (config.Notify()) {
							notifier.notify("Raid Found!");
						}
						Print("Raid Found, waiting for raider.");
						raidFound=true;
						if (!config.webhook().equals("")) //if webhook exists, post raid, world, cc to webhook
						{
							DiscordWebhook webhook = new DiscordWebhook(config.webhook());
							webhook.setTts(true);
							webhook.addEmbed(new DiscordWebhook.EmbedObject()
									.setTitle(client.getLocalPlayer().getName())
									.setDescription(roomsTrimmed.toString())
									.setColor(Color.GREEN)
									.addField("World", String.valueOf(client.getWorld()), true)
									.addField("Layout", raidLayout, true));
							webhook.execute(); //Handle exception
						}
						return; //returns if mutt or tek found, returns to prevent double printing Chat Message
					}
				}
			}

			if (!config.requireOverload()) //dupe code cba cleaning up rn
			{
				if (config.Notify()) {
					notifier.notify("Raid Found!");
				}
				Print("Raid Found, waiting for raider.");
				raidFound=true;
				if (!config.webhook().equals("")) //if webhook exists, post raid, world, cc to webhook
				{
					DiscordWebhook webhook = new DiscordWebhook(config.webhook());
					webhook.setTts(true);
					webhook.addEmbed(new DiscordWebhook.EmbedObject()
							.setTitle(client.getLocalPlayer().getName())
							.setDescription(roomsTrimmed.toString())
							.setColor(Color.GREEN)
							.addField("World", String.valueOf(client.getWorld()), true)
							.addField("Layout", raidLayout, true));
					webhook.execute(); //Handle exception
				}
				return;
			}
			if (config.debugScouting()) //if debugScouting enabled adds reason for leaving raid to game chat
			{
				Print("No Overload in raid");
			}
		}
	}
}