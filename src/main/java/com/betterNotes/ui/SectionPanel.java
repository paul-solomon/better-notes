package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Panel representing a single "section" with minimize/maximize functionality.
 */
public class SectionPanel extends JPanel
{
    private final BetterNotesPlugin plugin;
    private final BetterNotesSection section;

    private final JLabel edit = new JLabel("Edit");
    private final JLabel remove = new JLabel("Remove");
    private final JLabel save = new JLabel("Save");
    private final JLabel cancel = new JLabel("Cancel");

    private static final ImageIcon MINIMIZE_ICON;
    private static final ImageIcon MINIMIZE_ICON_HOVER;
    private static final ImageIcon MAXIMIZE_ICON;
    private static final ImageIcon MAXIMIZE_ICON_HOVER;
    private final JLabel minMaxLabel = new JLabel();

    /**
     * The panel that holds **all** collapsible content:
     * - "Notes" label + "Add new note" button
     * - "More content here..." label
     *
     * We toggle this entire panel's visibility based on isMaximized.
     */
    private final JPanel expandedContentPanel = new JPanel();

    static
    {
        BufferedImage downArrow = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/down_arrow.png");
        BufferedImage downArrowHover = ImageUtil.luminanceOffset(downArrow, -150);
        MINIMIZE_ICON = new ImageIcon(downArrow);
        MINIMIZE_ICON_HOVER = new ImageIcon(downArrowHover);

        BufferedImage rightArrow = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/right_arrow.png");
        BufferedImage rightArrowHover = ImageUtil.luminanceOffset(rightArrow, -150);
        MAXIMIZE_ICON = new ImageIcon(rightArrow);
        MAXIMIZE_ICON_HOVER = new ImageIcon(rightArrowHover);
    }

    public SectionPanel(BetterNotesPlugin plugin, BetterNotesSection section, final MouseAdapter flatTextFieldMouseAdapter)
    {
        this.plugin = plugin;
        this.section = section;

        // Top-level layout
        setLayout(new BorderLayout());
        setBackground(Helper.CONTENT_COLOR);

        // === 1) NAME WRAPPER: top row (min/max, section name, action buttons) ===
        JPanel nameWrapper = new JPanel(new GridBagLayout());
        nameWrapper.setBackground(Helper.CONTENT_COLOR);
        nameWrapper.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Helper.BACKGROUND_COLOR),
                BorderFactory.createLineBorder(Helper.CONTENT_COLOR)));

        FlatTextField nameInput = createNameInput(section.getName(), flatTextFieldMouseAdapter);
        JPanel nameActions = createNameActions(nameInput, flatTextFieldMouseAdapter);
        setupMinMaxLabel(); // sets up the icon + toggle logic

        // -- Layout constraints for top row --
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 8, 0, 0); // small left margin for the arrow
        nameWrapper.add(minMaxLabel, gc);

        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        nameWrapper.add(nameInput, gc);

        gc = new GridBagConstraints();
        gc.gridx = 2;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.EAST;
        nameWrapper.add(nameActions, gc);

        add(nameWrapper, BorderLayout.NORTH);

        // === 2) EXPANDED CONTENT PANEL (Collapsible) ===
        expandedContentPanel.setBackground(Helper.CONTENT_COLOR);
        expandedContentPanel.setLayout(new BorderLayout());
        expandedContentPanel.setVisible(section.isMaximized());

        // (A) "Notes" area at the top of the collapsible panel
        JPanel notesPanel = new JPanel(new BorderLayout(8, 0));
        notesPanel.setBackground(Helper.CONTENT_COLOR);
        // Some padding so it's not flush against edges
        notesPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setFont(FontManager.getRunescapeSmallFont());
        notesLabel.setForeground(Color.WHITE);

        JButton addNoteButton = new JButton("Add new note");
        addNoteButton.setBackground(Helper.BACKGROUND_COLOR);
        addNoteButton.setForeground(Color.WHITE);
        addNoteButton.setFont(FontManager.getRunescapeSmallFont());
        addNoteButton.addActionListener(e ->
        {
            // Your "Add new note" logic here
            plugin.addNoteToSection(section.getId());
        });

        notesPanel.add(notesLabel, BorderLayout.WEST);
        notesPanel.add(addNoteButton, BorderLayout.EAST);
        expandedContentPanel.add(notesPanel, BorderLayout.NORTH);

        // (B) "More content here..." in the center
        JPanel moreContentPanel = new JPanel();
        moreContentPanel.setLayout(new BoxLayout(moreContentPanel, BoxLayout.Y_AXIS));
        moreContentPanel.setBackground(Helper.CONTENT_COLOR);
        moreContentPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

// For each note, add a SectionNotePanel
        if (section.getNotes().isEmpty())
        {
            String htmlText =
                    "<html><body style='width:150px;'>"
                            + "No notes in this section.<br>"
                            + "Press the \"Add new note\" button to create one."
                            + "</body></html>";

            JLabel noNotesLabel = new JLabel(htmlText);
            noNotesLabel.setForeground(Color.WHITE);
            moreContentPanel.add(noNotesLabel);
        }
        else
        {
            for (BetterNotesNote note : section.getNotes())
            {
                SectionNotePanel notePanel = new SectionNotePanel(plugin, note, section.getId());
                moreContentPanel.add(notePanel);

                // Optional vertical gap between note panels
                moreContentPanel.add(Box.createVerticalStrut(5));
            }
        }

        expandedContentPanel.add(moreContentPanel, BorderLayout.CENTER);

        // Add the collapsible panel to the main panel
        add(expandedContentPanel, BorderLayout.CENTER);
    }

    /**
     * Creates a read-only text field for the section name.
     */
    private FlatTextField createNameInput(String initialText, MouseAdapter flatTextFieldMouseAdapter)
    {
        FlatTextField nameInput = new FlatTextField();
        nameInput.setText(initialText);
        nameInput.setBorder(null);
        nameInput.setEditable(false);
        nameInput.setBackground(Helper.CONTENT_COLOR);
        nameInput.setPreferredSize(new Dimension(0, 24));
        nameInput.getTextField().setForeground(Color.WHITE);
        nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));

        // Make the text field clickable in the normal (non-edit) state
        nameInput.getTextField().addMouseListener(flatTextFieldMouseAdapter);

        return nameInput;
    }

    /**
     * Create the panel holding action buttons (Edit/Remove/Save/Cancel).
     */
    private JPanel createNameActions(FlatTextField nameInput, MouseAdapter flatTextFieldMouseAdapter)
    {
        JPanel nameActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        nameActions.setBackground(Helper.CONTENT_COLOR);

        // Edit button
        edit.setFont(FontManager.getRunescapeSmallFont());
        edit.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        edit.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    nameInput.getTextField().removeMouseListener(flatTextFieldMouseAdapter);
                    nameInput.setEditable(true);
                    updateNameActions(true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                edit.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                edit.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });

        // Remove button
        remove.setFont(FontManager.getRunescapeSmallFont());
        remove.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        remove.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    plugin.removeSection(section.getId());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                remove.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                remove.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });

        // Save button
        save.setFont(FontManager.getRunescapeSmallFont());
        save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        save.setVisible(false);
        save.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    nameInput.getTextField().addMouseListener(flatTextFieldMouseAdapter);
                    // Persist new name in plugin
                    String newName = nameInput.getText();
                    plugin.changeSectionName(newName, section.getId());

                    nameInput.setEditable(false);
                    updateNameActions(false);
                    requestFocusInWindow();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (save.isEnabled())
                {
                    save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
            }
        });

        // Cancel button
        cancel.setFont(FontManager.getRunescapeSmallFont());
        cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        cancel.setVisible(false);
        cancel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    nameInput.getTextField().addMouseListener(flatTextFieldMouseAdapter);
                    nameInput.setEditable(false);
                    nameInput.getTextField().setCaretPosition(0);
                    updateNameActions(false);
                    requestFocusInWindow();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
        });

        nameActions.add(edit);
        nameActions.add(remove);
        nameActions.add(save);
        nameActions.add(cancel);

        return nameActions;
    }

    /**
     * Sets up min/max label (arrow icon) and toggles expandedContentPanel visibility.
     */
    private void setupMinMaxLabel()
    {
        updateMinMaxLabel();

        minMaxLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                minMaxLabel.setIcon(section.isMaximized() ? MINIMIZE_ICON_HOVER : MAXIMIZE_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                updateMinMaxLabel();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    section.setMaximized(!section.isMaximized());
                    plugin.changeSectionIsExpanded(section.isMaximized(), section.getId());
                    updateMinMaxLabel();
                    // Toggle entire collapsible panel (notes + more content)
                    expandedContentPanel.setVisible(section.isMaximized());
                }
            }
        });
    }

    /**
     * Updates the arrow icon & tooltip based on whether the panel is expanded.
     */
    private void updateMinMaxLabel()
    {
        if (section.isMaximized())
        {
            minMaxLabel.setIcon(MINIMIZE_ICON);
            minMaxLabel.setToolTipText("Click to collapse");
        }
        else
        {
            minMaxLabel.setIcon(MAXIMIZE_ICON);
            minMaxLabel.setToolTipText("Click to expand");
        }
    }

    /**
     * Toggles visibility between Edit/Remove vs. Save/Cancel.
     */
    private void updateNameActions(boolean editing)
    {
        save.setVisible(editing);
        cancel.setVisible(editing);
        edit.setVisible(!editing);
        remove.setVisible(!editing);
    }
}
