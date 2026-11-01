package com.maggotkingmanual;

import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup(MaggotKingManualConfig.GROUP)
public interface MaggotKingManualConfig extends Config
{
	String GROUP = "manualmaggotkingreminder";

	@ConfigItem(
		keyName = "swapHotkey",
		name = "Swap hotkey",
		description = "Switch the manual reminder between Ranged and Magic"
	)
	default Keybind swapHotkey()
	{
		return new Keybind(KeyEvent.VK_PERIOD, 0);
	}
}
