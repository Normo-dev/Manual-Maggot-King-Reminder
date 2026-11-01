package com.maggotkingmanual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

final class ManualReminderPanel extends PluginPanel
{
	private final JLabel selectedStyle = new JLabel("Ranged", SwingConstants.CENTER);
	private final JLabel selectedIcon = new JLabel();
	private final JLabel hotkeyHint = new JLabel("", SwingConstants.CENTER);
	private final JToggleButton ranged = new JToggleButton("Ranged");
	private final JToggleButton magic = new JToggleButton("Magic");
	private ManualStyle selection = ManualStyle.RANGED;

	ManualReminderPanel(Consumer<ManualStyle> onSelect)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel title = new JLabel("Manual Maggot King Reminder", SwingConstants.CENTER);
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
		title.setAlignmentX(CENTER_ALIGNMENT);
		add(title);
		add(Box.createVerticalStrut(14));

		JPanel card = new JPanel(new BorderLayout(0, 10));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
		JLabel heading = new JLabel("Selected attack", SwingConstants.CENTER);
		heading.setForeground(Color.LIGHT_GRAY);
		card.add(heading, BorderLayout.NORTH);
		selectedIcon.setHorizontalAlignment(SwingConstants.CENTER);
		selectedIcon.setPreferredSize(new Dimension(32, 38));
		card.add(selectedIcon, BorderLayout.CENTER);
		selectedStyle.setFont(selectedStyle.getFont().deriveFont(Font.BOLD, 18f));
		card.add(selectedStyle, BorderLayout.SOUTH);
		add(card);
		add(Box.createVerticalStrut(12));

		ButtonGroup group = new ButtonGroup();
		group.add(ranged);
		group.add(magic);
		JPanel choices = new JPanel(new GridLayout(1, 2, 8, 0));
		choices.setOpaque(false);
		choices.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
		for (JToggleButton button : new JToggleButton[] {ranged, magic})
		{
			button.setFocusPainted(false);
			button.setFocusable(false);
			button.setVerticalTextPosition(SwingConstants.BOTTOM);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setPreferredSize(new Dimension(90, 74));
			choices.add(button);
		}
		ranged.addActionListener(event -> onSelect.accept(ManualStyle.RANGED));
		magic.addActionListener(event -> onSelect.accept(ManualStyle.MAGIC));
		add(choices);
		add(Box.createVerticalStrut(12));
		hotkeyHint.setForeground(Color.LIGHT_GRAY);
		hotkeyHint.setAlignmentX(CENTER_ALIGNMENT);
		add(hotkeyHint);
		setSelection(ManualStyle.RANGED, null);
	}

	void setSelection(ManualStyle style, BufferedImage image)
	{
		selection = style;
		selectedStyle.setText(style.label);
		selectedStyle.setForeground(style.color);
		selectedIcon.setIcon(image == null ? null : new ImageIcon(image));
		ranged.setSelected(style == ManualStyle.RANGED);
		magic.setSelected(style == ManualStyle.MAGIC);
		ranged.setForeground(style == ManualStyle.RANGED ? ManualStyle.RANGED.color : Color.WHITE);
		magic.setForeground(style == ManualStyle.MAGIC ? ManualStyle.MAGIC.color : Color.WHITE);
	}

	void setStyleIcon(ManualStyle style, BufferedImage image)
	{
		(style == ManualStyle.RANGED ? ranged : magic).setIcon(new ImageIcon(image));
		if (selection == style)
		{
			selectedIcon.setIcon(new ImageIcon(image));
		}
	}

	void setHotkey(Keybind keybind)
	{
		String label = keybind.getKeyCode() == KeyEvent.VK_PERIOD && keybind.getModifiers() == 0
			? "." : keybind.toString();
		hotkeyHint.setText(Keybind.NOT_SET.equals(keybind) ? "Click a style to update"
			: "Press " + label + " to switch");
	}
}
