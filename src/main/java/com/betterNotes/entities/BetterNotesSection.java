package com.betterNotes.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BetterNotesSection
{
    @Getter
    @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private boolean isMaximized;

    @Getter @Setter
    private List<BetterNotesNote> notes;

    public BetterNotesSection(final String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isMaximized = true;
        this.notes = new ArrayList<>();
    }
}
