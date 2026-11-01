package com.maggotkingmanual;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import net.runelite.client.config.Keybind;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaggotKingManualPluginTest
{
	private MaggotKingManualPlugin plugin;
	private MaggotKingManualConfig config;
	private ClientToolbar toolbar;
	private InfoBoxManager boxes;
	private SpriteManager sprites;
	private KeyManager keys;
	private ManualReminderInfoBox box;
	private ManualReminderPanel panel;
	private KeyListener hotkey;
	private boolean deferImages;
	private final Map<Integer, List<Consumer<BufferedImage>>> pending = new HashMap<>();

	@Before
	public void setUp()
	{
		plugin = new MaggotKingManualPlugin();
		config = mock(MaggotKingManualConfig.class);
		when(config.swapHotkey()).thenReturn(new MaggotKingManualConfig() { }.swapHotkey());
		toolbar = mock(ClientToolbar.class);
		boxes = mock(InfoBoxManager.class);
		sprites = mock(SpriteManager.class);
		keys = mock(KeyManager.class);
		plugin.config = config;
		plugin.clientToolbar = toolbar;
		plugin.infoBoxManager = boxes;
		plugin.spriteManager = sprites;
		plugin.keyManager = keys;
		doAnswer(invocation ->
		{
			int id = invocation.getArgument(0);
			Consumer<BufferedImage> callback = invocation.getArgument(2);
			if (deferImages)
			{
				pending.computeIfAbsent(id, ignored -> new ArrayList<>()).add(callback);
			}
			else
			{
				callback.accept(image());
			}
			return null;
		}).when(sprites).getSpriteAsync(anyInt(), eq(0), org.mockito.ArgumentMatchers.<Consumer<BufferedImage>>any());
	}

	@After
	public void tearDown() throws Exception
	{
		plugin.shutDown();
	}

	@Test
	public void defaultHotkeyIsUnmodifiedPeriod()
	{
		assertEquals(new Keybind(KeyEvent.VK_PERIOD, 0), new MaggotKingManualConfig() { }.swapHotkey());
	}

	@Test
	public void startupAddsSidebarAndInfoboxOnRanged() throws Exception
	{
		start();
		assertSelected("Ranged");
		assertTrue(button("Ranged").isSelected());
		assertNotNull(label("Press . to switch"));
		assertEquals(32, box.getImage().getWidth());
		assertEquals("", box.getText());
		verify(sprites).getSpriteAsync(eq(1360), eq(0), org.mockito.ArgumentMatchers.<Consumer<BufferedImage>>any());
		verify(sprites).getSpriteAsync(eq(1361), eq(0), org.mockito.ArgumentMatchers.<Consumer<BufferedImage>>any());
	}

	@Test
	public void sidebarButtonsDirectlySelectMatchingStyle() throws Exception
	{
		start();
		click("Magic");
		assertSelected("Magic");
		assertTrue(button("Magic").isSelected());
		click("Ranged");
		assertSelected("Ranged");
		assertTrue(button("Ranged").isSelected());
	}

	@Test
	public void selectingSameStyleDoesNotToggleIt() throws Exception
	{
		start();
		click("Magic");
		click("Magic");
		assertSelected("Magic");
		verify(boxes, times(1)).addInfoBox(any());
	}

	@Test
	public void periodSwapsBothWaysAndConsumesTheKey() throws Exception
	{
		start();
		click("Magic");
		KeyEvent press = event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD);
		hotkey.keyPressed(press);
		flush();
		assertSelected("Ranged");
		verify(press).consume();
		hotkey.keyReleased(event(KeyEvent.KEY_RELEASED, KeyEvent.VK_PERIOD));
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Magic");
	}

	@Test
	public void holdingPeriodOnlySwapsOnce() throws Exception
	{
		start();
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Magic");
	}

	@Test
	public void unrelatedKeyDoesNotChangeSelection() throws Exception
	{
		start();
		KeyEvent press = event(KeyEvent.KEY_PRESSED, KeyEvent.VK_A);
		hotkey.keyPressed(press);
		flush();
		assertSelected("Ranged");
		verify(press, never()).consume();
	}

	@Test
	public void customBindingTakesEffectWithoutRestart() throws Exception
	{
		start();
		when(config.swapHotkey()).thenReturn(new Keybind(KeyEvent.VK_F6, 0));
		configChanged();
		assertNotNull(label("Press F6 to switch"));
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Ranged");
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_F6));
		flush();
		assertSelected("Magic");
	}

	@Test
	public void clearingHotkeyLeavesButtonsWorking() throws Exception
	{
		start();
		when(config.swapHotkey()).thenReturn(Keybind.NOT_SET);
		configChanged();
		assertNotNull(label("Click a style to update"));
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Ranged");
		click("Magic");
		assertSelected("Magic");
	}

	@Test
	public void settingsChangesDoNotResetManualSelection() throws Exception
	{
		start();
		click("Magic");
		configChanged();
		assertSelected("Magic");
	}

	@Test
	public void losingFocusAllowsNextHotkeyPress() throws Exception
	{
		start();
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Magic");
		hotkey.focusLost();
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		assertSelected("Ranged");
	}

	@Test
	public void delayedRangedIconDoesNotReplaceSelectedMagicIcon() throws Exception
	{
		deferImages = true;
		start();
		click("Magic");
		deliver(1361);
		deliver(127);
		BufferedImage selectedImage = box.getImage();
		deliver(1360);
		deliver(128);
		assertSelected("Magic");
		assertSame(selectedImage, box.getImage());
	}

	@Test
	public void disablingRemovesSidebarInfoboxAndHotkey() throws Exception
	{
		start();
		plugin.shutDown();
		plugin.shutDown();
		verify(keys, times(1)).unregisterKeyListener(hotkey);
		verify(boxes, times(1)).removeInfoBox(box);
		verify(toolbar, times(1)).removeNavigation(any());
		clearInvocations(boxes);
		hotkey.keyPressed(event(KeyEvent.KEY_PRESSED, KeyEvent.VK_PERIOD));
		flush();
		verify(boxes, never()).updateInfoBoxImage(any());
	}

	@Test
	public void callbacksFromDisabledSessionDoNotUpdateReenabledPlugin() throws Exception
	{
		deferImages = true;
		start();
		Consumer<BufferedImage> oldRanged = pending.get(1360).remove(0);
		plugin.shutDown();
		clearInvocations(boxes, toolbar, keys);
		start();
		click("Magic");
		clearInvocations(boxes);
		oldRanged.accept(image());
		deliver(128);
		assertSelected("Magic");
		verify(boxes, never()).updateInfoBoxImage(any());
	}

	private void start() throws Exception
	{
		plugin.startUp();
		ArgumentCaptor<InfoBox> capturedBox = ArgumentCaptor.forClass(InfoBox.class);
		verify(boxes).addInfoBox(capturedBox.capture());
		box = (ManualReminderInfoBox) capturedBox.getValue();
		ArgumentCaptor<NavigationButton> navigation = ArgumentCaptor.forClass(NavigationButton.class);
		verify(toolbar).addNavigation(navigation.capture());
		panel = (ManualReminderPanel) navigation.getValue().getPanel();
		ArgumentCaptor<KeyListener> capturedKey = ArgumentCaptor.forClass(KeyListener.class);
		verify(keys).registerKeyListener(capturedKey.capture());
		hotkey = capturedKey.getValue();
	}

	private void assertSelected(String style)
	{
		assertEquals("Manual Maggot King Reminder<br>Selected attack: " + style, box.getTooltip());
		assertNotNull(label(style));
	}

	private void click(String text) throws Exception
	{
		SwingUtilities.invokeAndWait(() -> button(text).doClick(0));
	}

	private JToggleButton button(String text)
	{
		return find(panel, JToggleButton.class, text);
	}

	private JLabel label(String text)
	{
		return find(panel, JLabel.class, text);
	}

	private static <T extends Component> T find(Container parent, Class<T> type, String text)
	{
		for (Component component : parent.getComponents())
		{
			if (type.isInstance(component))
			{
				String value = component instanceof JLabel ? ((JLabel) component).getText()
					: component instanceof JToggleButton ? ((JToggleButton) component).getText() : "";
				if (text.equals(value))
				{
					return type.cast(component);
				}
			}
			if (component instanceof Container)
			{
				T found = find((Container) component, type, text);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	private void configChanged() throws Exception
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup(MaggotKingManualConfig.GROUP);
		plugin.onConfigChanged(event);
		flush();
	}

	private void deliver(int id) throws Exception
	{
		pending.get(id).remove(0).accept(image());
		flush();
	}

	private static BufferedImage image()
	{
		return new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
	}

	private static KeyEvent event(int type, int key)
	{
		KeyEvent event = mock(KeyEvent.class);
		when(event.getID()).thenReturn(type);
		when(event.getExtendedKeyCode()).thenReturn(key);
		when(event.getKeyCode()).thenReturn(key);
		when(event.getModifiersEx()).thenReturn(0);
		return event;
	}

	private static void flush() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> { });
	}
}
