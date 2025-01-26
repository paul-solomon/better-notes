package com.betterNotes;

import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.ui.MainPanel;
import com.betterNotes.ui.NoteOverviewPanel;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "Better Notes"
)
public class BetterNotesPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "betternotes";
	@Inject
	private Client client;

	@Inject
	@Getter
	private ChatboxItemSearch itemSearch;

	@Inject
	@Getter
	private ItemManager itemManager;

	@Inject
	@Getter
	private SpriteManager spriteManager;

	@Inject
	private Gson gson;
	@Inject
	private BetterNotesConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	private MainPanel panel;
	private NavigationButton navButton;

	private BetterNotesCache cache;

	@Getter
	private BetterNotesDataManager dataManager;

	@Inject
	@Getter
	private ClientThread clientThread;

	@Getter
	private List<BetterNotesSection> sections;

	public MainPanel getPanel()
	{
		return panel;
	}

	@Override
	protected void startUp() throws Exception
	{
		sections = new ArrayList<>();

		panel = new MainPanel(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/notes_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Better Notes")
				.priority(7)
				.icon(icon)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		this.cache = new BetterNotesCache();
		this.dataManager = new BetterNotesDataManager(this, configManager, gson, cache, sections);

		clientThread.invokeLater(() -> {
			dataManager.loadConfig();
			SwingUtilities.invokeLater(() -> panel.rebuild());
			return true;
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	public void addSection()
	{
		final String msg = "Enter the name of this section (max 50 chars).";
		String name = JOptionPane.showInputDialog(panel,
				msg,
				"Add New Category",
				JOptionPane.PLAIN_MESSAGE);

		// cancel button was clicked
		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > 50)
		{
			name = name.substring(0, 50);
		}

		final String newName = name;

		BetterNotesSection newSection = new BetterNotesSection(newName);

		sections.add(newSection);
		cache.addSection(newSection);

		dataManager.updateConfig();
	}

	public void removeSection(String idToRemove) {
		sections.removeIf(section -> section.getId().equals(idToRemove));
		cache.removeSection(idToRemove);

		dataManager.updateConfig();
	}

	public void changeSectionName(String newName, String sectionIdToChange) {
		sections.stream()
				.filter(section -> section.getId().equals(sectionIdToChange))
				.findFirst().ifPresent(found -> found.setName(newName));

		cache.changeSectionName(newName, sectionIdToChange);

		dataManager.updateConfig();
	}

	public void changeSectionIsExpanded(Boolean isExpanded, String sectionIdToChange) {
		sections.stream()
				.filter(section -> section.getId().equals(sectionIdToChange))
				.findFirst().ifPresent(found -> found.setMaximized(isExpanded));

		cache.changeIsExpanded(isExpanded, sectionIdToChange);

		dataManager.updateConfig();
	}

	public void addNoteToSection(String sectionId) {
		final String msg = "Enter the name of this note (max 50 chars).";
		String name = JOptionPane.showInputDialog(panel,
				msg,
				"Add New Section",
				JOptionPane.PLAIN_MESSAGE);

		// cancel button was clicked
		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > 50)
		{
			name = name.substring(0, 50);
		}

		final String newName = name;

		BetterNotesNote newNote = new BetterNotesNote(newName);

		sections.stream()
				.filter(section -> section.getId().equals(sectionId))
				.findFirst().ifPresent(sectionToAddTo -> sectionToAddTo.getNotes().add(newNote));

		dataManager.updateConfig();
	}

	public void deleteNoteFromSection(String noteId, String sectionId) {
		// 1) Find the matching section
		BetterNotesSection targetSection = sections.stream()
				.filter(section -> section.getId().equals(sectionId))
				.findFirst()
				.orElse(null);

		// If no section found, just return
		if (targetSection == null)
		{
			return;
		}

		// 2) Remove the matching note from that section's list
		// If note IDs are unique, this removes just one
		targetSection.getNotes().removeIf(note -> note.getId().equals(noteId));

		// 3) (Optional) Persist changes or refresh UI
		dataManager.updateConfig();
	}

	public void setNoteIconFromSearch(final BetterNotesNote note, final NoteOverviewPanel detailPanel)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(panel,
					"You must be logged in to search.",
					"Cannot Search for Item",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		itemSearch
				.tooltipText("Set note icon")
				.onItemSelected((itemId) ->
				{
					clientThread.invokeLater(() ->
					{
						int finalId = itemManager.canonicalize(itemId);
						note.setItemId(finalId);

						// Optionally save right away
						dataManager.updateConfig();

						// Refresh the open detail panel, so user sees the new icon
						detailPanel.refreshIcon();
					});
				})
				.build();
	}

	public void redrawMainPanel() {
		panel.rebuild();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{

	}

	@Provides
	BetterNotesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterNotesConfig.class);
	}
}
