package com.maggotkingmanual;

import java.awt.Color;
import net.runelite.api.gameval.SpriteID;

enum ManualStyle
{
	RANGED("Ranged", new Color(110, 210, 100), SpriteID.Hitmark.HITSPLAT_GREEN_POISON,
		SpriteID.Prayeron.PROTECT_FROM_MISSILES),
	MAGIC("Magic", new Color(240, 200, 65), SpriteID.Hitmark._3,
		SpriteID.Prayeron.PROTECT_FROM_MAGIC);

	final String label;
	final Color color;
	final int splatId;
	final int prayerId;

	ManualStyle(String label, Color color, int splatId, int prayerId)
	{
		this.label = label;
		this.color = color;
		this.splatId = splatId;
		this.prayerId = prayerId;
	}

	ManualStyle opposite()
	{
		return this == RANGED ? MAGIC : RANGED;
	}
}
