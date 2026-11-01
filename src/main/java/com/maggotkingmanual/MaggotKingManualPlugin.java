package com.maggotkingmanual;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Manual Maggot King Reminder",
	description = "A manual ranged/magic reminder controlled by sidebar buttons or a hotkey",
	tags = {"maggot", "king", "manual", "reminder", "sidebar"}
)
public class MaggotKingManualPlugin extends Plugin
{
	@Inject
	ClientToolbar clientToolbar;
	@Inject
	InfoBoxManager infoBoxManager;
	@Inject
	SpriteManager spriteManager;
	@Inject
	KeyManager keyManager;
	@Inject
	MaggotKingManualConfig config;

	private final Map<ManualStyle, BufferedImage> icons = new EnumMap<>(ManualStyle.class);
	private ManualReminderPanel panel;
	private ManualReminderInfoBox infoBox;
	private NavigationButton navigation;
	private ManualStyle selection = ManualStyle.RANGED;
	private boolean active;

	private final HotkeyListener swapListener = new HotkeyListener(() -> config.swapHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			onEdt(() ->
			{
				if (active)
				{
					applySelection(selection.opposite());
				}
			});
		}
	};

	@Provides
	MaggotKingManualConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MaggotKingManualConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		onEdtAndWait(() ->
		{
			active = true;
			icons.clear();
			panel = new ManualReminderPanel(this::selectStyle);
			panel.setHotkey(config.swapHotkey());
			navigation = NavigationButton.builder()
				.tooltip("Manual Maggot King Reminder")
				.icon(ManualStyleIcons.navigationIcon())
				.priority(8)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navigation);
			infoBox = new ManualReminderInfoBox(this);
			infoBoxManager.addInfoBox(infoBox);
			applySelection(ManualStyle.RANGED);
			swapListener.focusLost();
			keyManager.registerKeyListener(swapListener);
			ManualReminderPanel targetPanel = panel;
			for (ManualStyle style : ManualStyle.values())
			{
				ManualStyleIcons.load(spriteManager, style, image -> onEdt(() ->
				{
					if (!active || panel != targetPanel)
					{
						return;
					}
					icons.put(style, image);
					panel.setStyleIcon(style, image);
					if (selection == style)
					{
						infoBox.setSelection(style, image);
						infoBoxManager.updateInfoBoxImage(infoBox);
					}
				}));
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		onEdtAndWait(() ->
		{
			if (!active)
			{
				return;
			}
			active = false;
			keyManager.unregisterKeyListener(swapListener);
			swapListener.focusLost();
			if (navigation != null)
			{
				clientToolbar.removeNavigation(navigation);
				navigation = null;
			}
			if (infoBox != null)
			{
				infoBoxManager.removeInfoBox(infoBox);
				infoBox = null;
			}
			panel = null;
			icons.clear();
		});
	}

	void selectStyle(ManualStyle style)
	{
		onEdt(() ->
		{
			if (active)
			{
				applySelection(style);
			}
		});
	}

	private void applySelection(ManualStyle style)
	{
		selection = style;
		panel.setSelection(style, icons.get(style));
		infoBox.setSelection(style, icons.get(style));
		infoBoxManager.updateInfoBoxImage(infoBox);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (MaggotKingManualConfig.GROUP.equals(event.getGroup()))
		{
			onEdt(() ->
			{
				if (active)
				{
					panel.setHotkey(config.swapHotkey());
				}
			});
		}
	}

	private static void onEdt(Runnable action)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			action.run();
		}
		else
		{
			SwingUtilities.invokeLater(action);
		}
	}

	private static void onEdtAndWait(Runnable action) throws Exception
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			action.run();
		}
		else
		{
			SwingUtilities.invokeAndWait(action);
		}
	}
}
