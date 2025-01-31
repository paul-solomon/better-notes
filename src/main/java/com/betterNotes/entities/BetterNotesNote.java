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
    private int spriteId;

    @Getter @Setter
    private boolean isMaximized;

    @Getter @Setter
    private boolean isNewNote;

    public BetterNotesNote(final String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = "";
        this.itemId = -1;
        this.spriteId = -1;
        this.isMaximized = true;
        this.isNewNote = true;
    }

    public boolean hasItemIcon() {
        return this.getItemId() != -1;
    }

    public boolean hasSpriteIcon() {
        return this.getSpriteId() != -1;
    }

    public boolean hasIcon() {
        return this.hasSpriteIcon() || this.hasItemIcon();
    }
}
