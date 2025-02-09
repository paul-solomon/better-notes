package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ReorderableSectionPanel extends JPanel {
    private final BetterNotesPlugin plugin;
    private final BetterNotesSection section;

    public ReorderableSectionPanel(BetterNotesPlugin plugin, BetterNotesSection section) {
        this.plugin = plugin;
        this.section = section;

        setLayout(new BorderLayout());
        setBackground(Helper.DARKER_GREY_COLOR);

        // --- Header Panel (icon + title) ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(Helper.DARKER_GREY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Icon label (left side)
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setOpaque(false);

        // Get the cached image if available.
        BufferedImage cachedIcon = null;
        if (section.hasIcon()) {
            if (section.hasSpriteIcon()) {
                cachedIcon = plugin.getSprite(section.getSpriteId());
            } else if (section.hasItemIcon()) {
                cachedIcon = plugin.getItem(section.getItemId());
            }
            if (cachedIcon != null) {
                iconLabel.setIcon(new ImageIcon(cachedIcon));
            }
        }

        // Title label (right side)
        JLabel titleLabel = new JLabel(section.getName());
        titleLabel.setForeground(Color.WHITE);

        // Layout constraints for header
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 8, 0, 4);
        headerPanel.add(iconLabel, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(0, 0, 0, 8);
        headerPanel.add(titleLabel, gc);

        add(headerPanel, BorderLayout.NORTH);

        // --- Content Panel (the reorderable list) ---
        JPanel expandedContentPanel = new JPanel(new BorderLayout());
        expandedContentPanel.setBackground(Helper.DARKER_GREY_COLOR);
        expandedContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create the reorderable list for this section.
        SectionNotesReorderableList reorderableNotesList = new SectionNotesReorderableList(plugin, section);
        expandedContentPanel.add(reorderableNotesList, BorderLayout.CENTER);

        add(expandedContentPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
