package net.runelite.client.plugins.tabswitcher;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.ClientTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Tab Switcher",
        description = "Switches to previous tab on keybind",
        tags = {"tab", "switch","tabswitcher"},
        enabledByDefault = false
)
public class TabSwitcherPlugin extends Plugin {

    private static int previousTab;
    private static int currentTab;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private TabSwitcherConfig config;

    @Inject
    private KeyManager keyManager;

    @Provides
    TabSwitcherConfig provideConfig(ConfigManager configManager) {
        return (TabSwitcherConfig) configManager.getConfig(TabSwitcherConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        keyManager.registerKeyListener(hotkeyListener);
    }

    private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.keybind())
    {
        @Override
        public void hotkeyPressed()
        {
            clientThread.invoke(() -> switchTab());
        }
    };

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (getCurrentTab()!=currentTab){
            previousTab = currentTab;
            currentTab = getCurrentTab();
        }
    }

    private int getCurrentTab() {
        return client.getVar(VarClientInt.INVENTORY_TAB);
    }

    private void switchTab() {
        if (config.hardCodeTabs())
        {
            if (getCurrentTab()==config.tab1())
            {
                client.runScript(915, config.tab2());
                return;
            }
            client.runScript(915, config.tab1());
            return;
        }

        if (previousTab!=-1)
        {
            client.runScript(915, previousTab);
        }
    }
}
