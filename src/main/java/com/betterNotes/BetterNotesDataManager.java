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

    private Gson gson;

    private final List<BetterNotesSection> sections;

    public static final String CONFIG_KEY_DATA = "data";

    @Inject
    public BetterNotesDataManager(final BetterNotesPlugin plugin,
                                  final ConfigManager manager,
                                  final Gson gson,
                                  final BetterNotesCache cache,
                                  final List<BetterNotesSection> sections) {
        this.plugin = plugin;
        this.configManager = manager;
        this.cache = cache;
        this.gson = gson;
        this.sections = sections;
    }

    public void loadConfig() {
        sections.clear();
        cache.clearAll();

        Type dataType = new TypeToken<ArrayList<BetterNotesSection>>(){}.getType();

        sections.addAll(loadData(CONFIG_KEY_DATA, dataType));

        for (final BetterNotesSection section: sections) {
            cache.addSection(section);
        }
    }

    public void updateConfig() {
        final String jsonSections = gson.toJson(sections);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_DATA, jsonSections);
        plugin.redrawMainPanel();
    }


    private <T> List<T> loadData(final String configKey, Type type)
    {
        final String storedData = configManager.getConfiguration(CONFIG_GROUP, configKey);
        if (Strings.isNullOrEmpty(storedData))
        {
            return new ArrayList<>();
        }
        else
        {
            try
            {
                // serialize the internal data structure from the json in the configuration
                return gson.fromJson(storedData, type);
            }
            catch (Exception e)
            {
                log.error("Exception occurred while loading data", e);
                return new ArrayList<>();
            }
        }
    }
}
