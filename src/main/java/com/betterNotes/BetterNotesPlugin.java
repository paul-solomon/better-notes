package com.betterNotes;

import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.ui.GridDialog;
import com.betterNotes.ui.MainPanel;
import com.betterNotes.ui.NoteOverviewPanel;
import com.google.common.collect.ImmutableList;
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
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.runelite.client.hiscore.HiscoreSkill.*;
import static net.runelite.client.hiscore.HiscoreSkill.ZULRAH;

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
	private ColorPickerManager colorPickerManager;

	@Inject
	@Getter
	private ClientThread clientThread;

	@Getter
	private List<BetterNotesSection> sections;

	@Getter
	private BetterNotesSection unassignedNotesSection;

	public MainPanel getPanel()
	{
		return panel;
	}

	@Override
	protected void startUp() throws Exception
	{
		sections = new ArrayList<>();

		unassignedNotesSection = new BetterNotesSection("Unassigned notes");
		unassignedNotesSection.setUnassignedNotesSection(true);

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
		this.dataManager = new BetterNotesDataManager(this, configManager, gson, cache, sections, unassignedNotesSection);

		clientThread.invokeLater(() -> {
			dataManager.loadConfig();
			SwingUtilities.invokeLater(() -> {
				unassignedNotesSection = dataManager.getUnassignedNotesSection();
				panel.rebuild();
			});
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
		BetterNotesSection newSection = new BetterNotesSection("New section");

		sections.add(newSection);
		cache.addSection(newSection);

		dataManager.updateConfig();
	}

	public void openColorPicker(String title, Color startingColor, Consumer<Color> onColorChange)
	{

		RuneliteColorPicker colorPicker = getColorPickerManager().create(
				SwingUtilities.windowForComponent(panel),
				startingColor,
				title,
				false);

		colorPicker.setLocation(panel.getLocationOnScreen());
		colorPicker.setOnColorChange(onColorChange);

		colorPicker.setVisible(true);
	}


	public void deleteSection(BetterNotesSection sectionToDelete) {
		// Show confirmation dialog
		String message;
		if (sectionToDelete.getNotes().isEmpty()) {
			message = "Are you sure you want to delete this section?";
		} else {
			message = "Are you sure you want to delete this section? All notes inside it will be moved to the unassigned notes section.";
		}
		int choice = JOptionPane.showConfirmDialog(
				null,
				message,
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION
		);

		// Proceed only if the user confirms
		if (choice == JOptionPane.YES_OPTION) {
			if (!sectionToDelete.getNotes().isEmpty()) {
				unassignedNotesSection.getNotes().addAll(sectionToDelete.getNotes());
			}
			sections.removeIf(section -> section.getId().equals(sectionToDelete.getId()));
			cache.removeSection(sectionToDelete.getId());

			dataManager.updateConfig();
		}
	}

	public void deleteNote(BetterNotesNote noteToDelete, BetterNotesSection section) {
		int choice = JOptionPane.showConfirmDialog(
				null,
				"Are you sure you want to delete this note?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.YES_OPTION) {
			if (section.isUnassignedNotesSection()) {
				// Remove from unassignedNotesSection
				unassignedNotesSection.getNotes().removeIf(note -> note.getId().equals(noteToDelete.getId()));
			} else {
				// Search for the section and remove the note
				sections.stream()
						.filter(sec -> sec.getId().equals(section.getId()))
						.findFirst()
						.ifPresent(foundSection -> foundSection.getNotes().removeIf(note -> note.getId().equals(noteToDelete.getId())));
			}

			// Update configuration to persist changes
			getDataManager().updateConfig();

			// Refresh the UI to reflect changes
			redrawMainPanel();
		}
	}

	public void changeSectionName(String newName, String sectionIdToChange) {
		sections.stream()
				.filter(section -> section.getId().equals(sectionIdToChange))
				.findFirst().ifPresent(found -> found.setName(newName));

		cache.changeSectionName(newName, sectionIdToChange);

		dataManager.updateConfig();
	}

	public void addNote() {
		// Check if there are no sections
		if (sections.isEmpty()) {
			// Directly add the note to the unassigned section
			addNoteToUnassignedSection();
			return;
		}

		// Create a list of section names for the modal
		List<String> sectionNames = sections.stream()
				.map(BetterNotesSection::getName)
				.collect(Collectors.toList());

		// Add the "Unassigned notes" option to the list
		sectionNames.add("Unassigned notes");

		// Display a modal to choose a section
		String[] options = sectionNames.toArray(new String[0]);
		String chosenSection = (String) JOptionPane.showInputDialog(
				null,
				"Choose a section for the new note:",
				"Add Note",
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]
		);

		// If the user canceled the modal or didn't make a selection, do nothing
		if (chosenSection == null) {
			return;
		}

		// Determine which method to call based on the chosen section
		if (chosenSection.equals("Unassigned notes")) {
			addNoteToUnassignedSection();
		} else {
			// Find the section ID corresponding to the chosen section name
			sections.stream()
					.filter(section -> section.getName().equals(chosenSection))
					.findFirst()
					.ifPresent(section -> addNoteToSection(section.getId()));
		}
	}
	public void addNoteToSection(String sectionId) {

		BetterNotesNote newNote = new BetterNotesNote("New note");

		sections.stream()
				.filter(section -> section.getId().equals(sectionId))
				.findFirst().ifPresent(sectionToAddTo -> sectionToAddTo.getNotes().add(newNote));

		dataManager.updateConfig();
	}

	public void openSectionIconPickerDialog(BetterNotesSection section) {
		SwingUtilities.invokeLater(() -> {

			HiscoreSkill selectedSkill = openIconPickerDialog(section, null);

			if (selectedSkill != null) {
				removeSectionIcon(section, true);
				section.setSpriteId(selectedSkill.getSpriteId());
				dataManager.updateConfig();
			} else {
				System.out.println("No selection made.");
			}
		});
	}

	public void openNoteIconPickerDialog(BetterNotesNote note) {
		SwingUtilities.invokeLater(() -> {

			HiscoreSkill selectedSkill = openIconPickerDialog(null, note);

			if (selectedSkill != null) {
				removeNoteIcon(note, true);
				note.setSpriteId(selectedSkill.getSpriteId());
				dataManager.updateConfig();
			} else {
				System.out.println("No selection made.");
			}
		});
	}

	public void removeSectionIcon(BetterNotesSection section, Boolean skipSave) {
		section.setSpriteId(-1);
		section.setItemId(-1);
		if (!skipSave) {
			dataManager.updateConfig();
		}
	}

	public void removeNoteIcon(BetterNotesNote note, Boolean skipSave) {
		note.setSpriteId(-1);
		note.setItemId(-1);
		if (!skipSave) {
			dataManager.updateConfig();
		}
	}

	public HiscoreSkill openIconPickerDialog(BetterNotesSection section, BetterNotesNote note) {

		GridDialog dialog = new GridDialog(null, spriteManager, this, note, section); // Pass `null` as the owner if there's no parent frame
		dialog.setVisible(true);

		// Handle the result
		return dialog.getSelectedSkill();
	}

	public void addNoteToUnassignedSection() {
		BetterNotesNote newNote = new BetterNotesNote("New note");

		unassignedNotesSection.getNotes().add(newNote);

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

	public void setNoteIconFromSearch(final BetterNotesNote note)
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
						removeNoteIcon(note, true);
						note.setItemId(finalId);

						// Optionally save right away
						dataManager.updateConfig();

					});
				})
				.build();
	}

	public void setSectionIconFromSearch(final BetterNotesSection section)
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
						removeSectionIcon(section, true);
						section.setItemId(finalId);

						// Optionally save right away
						dataManager.updateConfig();
					});
				})
				.build();
	}

	public void redrawMainPanel() {
		panel.rebuild();
	}

	@Provides
	BetterNotesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterNotesConfig.class);
	}
}
