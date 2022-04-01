package net.runelite.client.plugins.scheduledlogout;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;
import java.awt.*;

@Singleton
public class ScheduledLogoutOverlay extends OverlayPanel {
    private final ScheduledLogoutPlugin plugin;
    private final ScheduledLogoutConfig config;

    @Inject
    private ScheduledLogoutOverlay(final ScheduledLogoutPlugin plugin, final ScheduledLogoutConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.overlay())
        {
            return null;
        }
        panelComponent.setBackgroundColor(Color.black);
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("LOGGING OUT IN: " + plugin.CountdownTimer)
                .build());
        return super.render(graphics);
    }
}
