package com.maggotkingmanual;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class ManualReminderLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MaggotKingManualPlugin.class);
		RuneLite.main(args);
	}
}
