package com.betterNotes;

import com.betterNotes.entities.BetterNotesSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BetterNotesCache {

    @Getter
    private final List<BetterNotesSection> sections;

    @Getter
    private BetterNotesSection unassignedNotesSection;

    public BetterNotesCache() {
        this.sections = new ArrayList<>();
        this.unassignedNotesSection = null; // Initialize as null
    }

    public void addSection(final BetterNotesSection section) {
        sections.add(section);
    }

    public void removeSection(final String idToRemove) {
        sections.removeIf(section -> section.getId().equals(idToRemove));
    }

    public void changeSectionName(String newName, String sectionIdToChange) {
        sections.stream()
                .filter(section -> section.getId().equals(sectionIdToChange))
                .findFirst().ifPresent(found -> found.setName(newName));
    }

    public void changeIsExpanded(Boolean isExpanded, String sectionIdToChange) {
        sections.stream()
                .filter(section -> section.getId().equals(sectionIdToChange))
                .findFirst().ifPresent(found -> found.setMaximized(isExpanded));
    }

    public void clearAll() {
        sections.clear();
        unassignedNotesSection = null; // Clear the unassigned notes section as well
    }

    public void setUnassignedNotesSection(BetterNotesSection section) {
        this.unassignedNotesSection = section;
    }
}
