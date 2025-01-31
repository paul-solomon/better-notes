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
    private int itemId;

    @Getter @Setter
    private int spriteId;

    @Getter @Setter
    private List<BetterNotesNote> notes;

    @Getter @Setter
    private boolean isUnassignedNotesSection;

    @Getter @Setter
    private boolean isNewSection;

    public BetterNotesSection(final String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isMaximized = true;
        this.notes = new ArrayList<>();
        this.itemId = -1;
        this.spriteId = -1;
        this.isUnassignedNotesSection = false;
        this.isNewSection = true;
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
