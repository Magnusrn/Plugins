package net.runelite.client.plugins.scheduledlogout;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Scheduled Logout",
        description = "Automatically logs out after inputted minutes",
        tags = {"log", "out","logout","timer","schedule"},
        enabledByDefault = false
)
public class ScheduledLogoutPlugin extends Plugin {

    int CountdownTimer;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ScheduledLogoutConfig config;

    @Inject
    private KeyManager keyManager;

    @Inject
    private ScheduledLogoutOverlay overlay;

    @Inject
    private OverlayManager overlayManager;


    @Provides
    ScheduledLogoutConfig provideConfig(ConfigManager configManager) {
        return (ScheduledLogoutConfig) configManager.getConfig(ScheduledLogoutConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        CountdownTimer = config.minutesToLogout()*100; //convert to ticks
        clientThread.invokeLater(() -> {Print("Logging out in " + config.minutesToLogout() + " minutes"); });
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!event.getKey().equals("minutestologout"))
        {
            return;
        }
        CountdownTimer = config.minutesToLogout()*100;
        clientThread.invokeLater(() -> {Print("Timer Updated - Logging out in " + config.minutesToLogout() + " minutes"); });
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (CountdownTimer>0)
        {
            CountdownTimer--;
        }
        else
        {
            clientThread.invoke(this::logout);
        }
    }

    private void logout(){
        if (client.getWidget(69, 23)!=null){
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, WidgetInfo.WORLD_SWITCHER_LOGOUT_BUTTON.getId());
        }
        else
        {
            client.invokeMenuAction("Logout", "", 1, MenuAction.CC_OP.getId(), -1, 11927560);
        }
    }

    private void Print(String string) //used for debugging, puts a message to the in game chat.
    {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,"",string,"");
    }
}
