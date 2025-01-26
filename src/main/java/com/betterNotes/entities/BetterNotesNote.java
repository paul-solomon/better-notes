package com.betterNotes.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class BetterNotesNote
{
    @Getter
    @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String content;

    @Getter @Setter
    private int itemId;

    @Getter @Setter
    private boolean isMaximized;

    public BetterNotesNote(final String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = "";
        this.itemId = -1;
        this.isMaximized = true;
    }

    public boolean isItemSet(final BetterNotesNote note) {
        return note.getItemId() != -1;
    }
}
