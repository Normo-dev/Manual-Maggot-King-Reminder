package com.maggotkingmanual;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;

final class ManualReminderInfoBox extends InfoBox
{
	ManualReminderInfoBox(MaggotKingManualPlugin plugin)
	{
		super(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB), plugin);
	}

	void setSelection(ManualStyle style, BufferedImage image)
	{
		setTooltip("Manual Maggot King Reminder<br>Selected attack: " + style.label);
		setImage(image != null ? image : new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
	}

	@Override
	public String getText()
	{
		return "";
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}
}
