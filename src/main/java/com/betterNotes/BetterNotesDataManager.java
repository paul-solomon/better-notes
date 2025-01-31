package com.betterNotes;

import com.betterNotes.entities.BetterNotesSection;
import com.google.gson.Gson;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.betterNotes.BetterNotesPlugin.CONFIG_GROUP;

@Slf4j
public class BetterNotesDataManager {

    private final BetterNotesPlugin plugin;
    private final ConfigManager configManager;
    private final BetterNotesCache cache;
    private final Gson gson;

    private final List<BetterNotesSection> sections;

    private BetterNotesSection unassignedNotesSection;

    public static final String CONFIG_KEY_SECTIONS = "sections";
    public static final String CONFIG_KEY_UNASSIGNED_NOTES = "unassigned_notes";

    @Inject
    public BetterNotesDataManager(final BetterNotesPlugin plugin,
                                  final ConfigManager manager,
                                  final Gson gson,
                                  final BetterNotesCache cache,
                                  final List<BetterNotesSection> sections,
                                  final BetterNotesSection unassignedNotesSection) {
        this.plugin = plugin;
        this.configManager = manager;
        this.cache = cache;
        this.gson = gson;
        this.sections = sections;
        this.unassignedNotesSection = unassignedNotesSection;
    }

    public void loadConfig() {
        sections.clear();
        cache.clearAll();

        Type dataType = new TypeToken<ArrayList<BetterNotesSection>>() {}.getType();
        sections.addAll(loadData(CONFIG_KEY_SECTIONS, dataType));

        // Load unassigned notes section
        BetterNotesSection loadedUnassignedNotes = loadSingleData(CONFIG_KEY_UNASSIGNED_NOTES, BetterNotesSection.class);

        if (loadedUnassignedNotes != null) {
            // Update the existing unassignedNotesSection if it exists
            if (unassignedNotesSection == null) {
                unassignedNotesSection = new BetterNotesSection("Unassigned notes");
            }

            unassignedNotesSection.setName(loadedUnassignedNotes.getName());
            unassignedNotesSection.setNotes(loadedUnassignedNotes.getNotes());
            cache.setUnassignedNotesSection(unassignedNotesSection);
        } else {
            // Create a new unassignedNotesSection if none exists in the config
            unassignedNotesSection = new BetterNotesSection("Unassigned notes");
            cache.setUnassignedNotesSection(unassignedNotesSection);
        }

        // Add loaded sections to cache
        for (final BetterNotesSection section : sections) {
            cache.addSection(section);
        }
    }

    public void updateConfig() {
        // Save sections
        final String jsonSections = gson.toJson(sections);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_SECTIONS, jsonSections);

        // Save unassigned notes section from cache

        final String jsonUnassignedNotes = gson.toJson(unassignedNotesSection);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_UNASSIGNED_NOTES, jsonUnassignedNotes);

        plugin.redrawMainPanel();
    }

    public void updateConfigNoRedraw() {
        // Save sections
        final String jsonSections = gson.toJson(sections);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_SECTIONS, jsonSections);

        // Save unassigned notes section from cache

        final String jsonUnassignedNotes = gson.toJson(unassignedNotesSection);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_UNASSIGNED_NOTES, jsonUnassignedNotes);
    }

    private <T> List<T> loadData(final String configKey, Type type) {
        final String storedData = configManager.getConfiguration(CONFIG_GROUP, configKey);
        if (Strings.isNullOrEmpty(storedData)) {
            return new ArrayList<>();
        } else {
            try {
                // Deserialize the internal data structure from the JSON in the configuration
                return gson.fromJson(storedData, type);
            } catch (Exception e) {
                log.error("Exception occurred while loading data", e);
                return new ArrayList<>();
            }
        }
    }

    private <T> T loadSingleData(final String configKey, Type type) {
        final String storedData = configManager.getConfiguration(CONFIG_GROUP, configKey);
        if (Strings.isNullOrEmpty(storedData)) {
            return null; // Return null if no data exists
        } else {
            try {
                // Deserialize the single object from JSON
                return gson.fromJson(storedData, type);
            } catch (Exception e) {
                log.error("Exception occurred while loading single data", e);
                return null;
            }
        }
    }

    public BetterNotesSection getUnassignedNotesSection() {
        return this.unassignedNotesSection;
    }
}
