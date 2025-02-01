package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ReorderableSectionNotePanel extends JPanel {
    private final BetterNotesPlugin plugin;
    private final BetterNotesNote note;

    public ReorderableSectionNotePanel(BetterNotesPlugin plugin, BetterNotesNote note) {
        this.plugin = plugin;
        this.note = note;

        setLayout(new BorderLayout());
        setBackground(Helper.DARK_GREY_COLOR);

        // === Header Panel (icon + title) ===
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(Helper.DARK_GREY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Icon label (left side)
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setOpaque(false); // Transparent background

        // Load icon from cache
        if (note.hasIcon()) {
            BufferedImage iconImage = null;
            if (note.hasSpriteIcon()) {
                iconImage = plugin.getSprite(note.getSpriteId());
            } else if (note.hasItemIcon()) {
                iconImage = plugin.getItem(note.getItemId());
            }

            if (iconImage != null) {
                iconLabel.setIcon(new ImageIcon(iconImage));
            }
        }

        // Title label (right side)
        JLabel titleLabel = new JLabel(note.getName());
        titleLabel.setForeground(Color.WHITE);

        // === Layout constraints ===
        GridBagConstraints gc = new GridBagConstraints();

        // Add icon label in column 0
        if (note.hasIcon()) {
            gc.gridx = 0;
            gc.gridy = 0;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 8, 0, 4); // Some spacing around the icon
            headerPanel.add(iconLabel, gc);
        }

        // Add title label in column 1
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1.0; // Let the title expand horizontally
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(0, 0, 0, 8); // Some spacing on the right
        headerPanel.add(titleLabel, gc);

        // Add the header to this main panel
        add(headerPanel, BorderLayout.NORTH);

        repaint();
        revalidate();
    }
}
