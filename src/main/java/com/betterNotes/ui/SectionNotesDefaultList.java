package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SectionNotesDefaultList extends JPanel {
    private final BetterNotesPlugin plugin;
    private final BetterNotesSection section;

    public SectionNotesDefaultList(BetterNotesPlugin plugin, BetterNotesSection section) {
        this.plugin = plugin;
        this.section = section;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Helper.DARKER_GREY_COLOR);

        if (section.getNotes().isEmpty()) {
            add(createNoNotesMessage());
        } else {
            List<BetterNotesNote> notes = section.getNotes();
            int noteCount = notes.size();

            for (int i = 0; i < noteCount; i++) {
                BetterNotesNote note = notes.get(i);
                SectionNotePanel notePanel = new SectionNotePanel(plugin, note, section, null);
                add(notePanel);

                // Add a vertical strut only if this is not the last note
                if (i < noteCount - 1) {
                    add(Box.createVerticalStrut(5));
                }
            }
        }
    }

    private JPanel createNoNotesMessage() {
        JPanel noNotesPanel = new JPanel();
        noNotesPanel.setLayout(new BoxLayout(noNotesPanel, BoxLayout.Y_AXIS));
        noNotesPanel.setBackground(Helper.DARKER_GREY_COLOR);

        JLabel noNotesText = new JLabel("There are no notes in this section.");
        noNotesText.setForeground(Color.WHITE);
        noNotesText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel promptText = new JLabel("Click 'Add Note' to create one.");
        promptText.setForeground(Color.LIGHT_GRAY);
        promptText.setAlignmentX(Component.LEFT_ALIGNMENT);

        noNotesPanel.add(noNotesText);
        noNotesPanel.add(Box.createVerticalStrut(5));
        noNotesPanel.add(promptText);

        return noNotesPanel;
    }
}
