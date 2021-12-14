package net.runelite.client.plugins.oneclickshades;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.List;

@Extension
@PluginDescriptor(
		name = "One Click Shades",
		description = "Left click shade burning",
		tags = {"oneclick,one,click,shades"},
		enabledByDefault = false
)

/*
``
Use,Redwood*,*
Use,Urium*,*
``
^Required Menu Entry swaps.
Plugin mostly done by Oliuyo
*/

@Slf4j
public class OneClickShadesPlugin extends Plugin {


	@Inject
	private Client client;

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!event.getOption().toLowerCase().contains("Use") && !event.getTarget().toLowerCase().contains("redwood pyre logs") && event.getTarget().toLowerCase().contains("pyre") && event.getIdentifier() == 4093)
		{
			if(findItem(ItemID.REDWOOD_PYRE_LOGS).getLeft() == -1)
			{
				return;
			}
			MenuEntry e = event.clone();
			e.setOption("Use");
			e.setTarget("<col=ff9040>Redwood pyre logs<col=ffffff> -> <col=ffff>Funeral Pyre");
			e.setForceLeftClick(true);
			insert(e);
		}
		if (!event.getOption().toLowerCase().contains("Use") && !event.getTarget().toLowerCase().contains("urium remains") && event.getTarget().toLowerCase().contains("pyre") && event.getIdentifier() == 28865)
		{
			if(findItem(ItemID.URIUM_REMAINS).getLeft() == -1)
			{
				return;
			}
			MenuEntry e = event.clone();
			e.setOption("Use");
			e.setTarget("<col=ff9040>Urium remains<col=ffffff> -> <col=ffff>Funeral Pyre");
			e.setForceLeftClick(true);
			insert(e);
		}

	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuTarget() == null)
		{
			return;
		}

		if (event.getMenuTarget().contains("<col=ff9040>Redwood pyre logs<col=ffffff> -> ") && event.getMenuTarget().toLowerCase().contains("pyre"))
		{
			if (updateSelectedItem(ItemID.REDWOOD_PYRE_LOGS))
			{
				event.setMenuAction(MenuAction.ITEM_USE_ON_GAME_OBJECT);
			}

		}
		if (event.getMenuTarget().contains("<col=ff9040>Urium remains<col=ffffff> -> ") && event.getMenuTarget().toLowerCase().contains("pyre"))
		{
			if (updateSelectedItem(ItemID.URIUM_REMAINS))
			{
				event.setMenuAction(MenuAction.ITEM_USE_ON_GAME_OBJECT);
			}
		}
	}

	public void insert(MenuEntry e)
	{
		if (client == null)
		{
			return;
		}
		client.insertMenuItem(
				e.getOption(),
				e.getTarget(),
				e.getOpcode(),
				e.getIdentifier(),
				e.getParam0(),
				e.getParam1(),
				true
		);
	}

	public Pair<Integer, Integer> findItem(int id)
	{
		if (client == null)
		{
			return Pair.of(-1, -1);
		}
		final Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		final List<WidgetItem> itemList = (List<WidgetItem>) inventoryWidget.getWidgetItems();

		for (int i = itemList.size() - 1; i >= 0; i--)
		{
			final WidgetItem item = itemList.get(i);
			if (item.getId() == id)
			{
				return Pair.of(item.getId(), item.getIndex());
			}
		}

		return Pair.of(-1, -1);
	}

	public boolean updateSelectedItem(int id)
	{
		if (client == null)
		{
			return false;
		}
		final Pair<Integer, Integer> pair = findItem(id);
		if (pair.getLeft() != -1)
		{
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(pair.getRight());
			client.setSelectedItemID(pair.getLeft());
			return true;
		}
		return false;
	}
}