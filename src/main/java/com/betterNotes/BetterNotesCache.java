package com.betterNotes;

import com.betterNotes.entities.BetterNotesSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BetterNotesCache {

    public BetterNotesCache() {
        this.sections = new ArrayList<>();
    }

    public void addSection (final BetterNotesSection section) {
        sections.add(section);
    }

    public void removeSection (final String idToRemove) { sections.removeIf(section -> section.getId().equals(idToRemove)); }

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
    public void clearAll()
    {
        sections.clear();
    }

    @Getter
    private final List<BetterNotesSection> sections;

}
