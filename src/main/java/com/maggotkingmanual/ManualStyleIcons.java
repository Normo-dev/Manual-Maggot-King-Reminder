package com.maggotkingmanual;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

final class ManualStyleIcons
{
	private static final int SIZE = 32;

	private ManualStyleIcons()
	{
	}

	static void load(SpriteManager sprites, ManualStyle style, Consumer<BufferedImage> callback)
	{
		sprites.getSpriteAsync(style.splatId, 0, splat ->
			sprites.getSpriteAsync(style.prayerId, 0, prayer -> callback.accept(compose(splat, prayer))));
	}

	private static BufferedImage compose(BufferedImage splat, BufferedImage prayer)
	{
		BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			drawCentered(graphics, splat, SIZE);
			drawCentered(graphics, prayer, 18);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	private static void drawCentered(Graphics2D graphics, BufferedImage sprite, int size)
	{
		double scale = (double) size / Math.max(sprite.getWidth(), sprite.getHeight());
		int width = Math.max(1, (int) Math.round(sprite.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(sprite.getHeight() * scale));
		graphics.drawImage(sprite, (SIZE - width) / 2, (SIZE - height) / 2, width, height, null);
	}

	static BufferedImage navigationIcon()
	{
		BufferedImage icon = ImageUtil.loadImageResource(ManualStyleIcons.class, "icon.png");
		return ImageUtil.resizeImage(icon, 24, 24);
	}
}
