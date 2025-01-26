package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A panel that shows an overview of a single note:
 * - First row: Back button (right aligned)
 * - Second row: Larger title in a FlatTextField (left) + Edit/Save/Cancel (right)
 * - Spacing
 * - JTextArea filling the rest of the space (with a distinct background)
 */
public class NoteOverviewPanel extends JPanel
{
    private final BetterNotesPlugin plugin;
    private final BetterNotesNote note;
    private final Runnable onBackAction;

    // For the note title
    private final FlatTextField titleInput = new FlatTextField();

    // Editing controls
    private final JLabel editLabel = new JLabel("Edit");
    private final JLabel saveLabel = new JLabel("Save");
    private final JLabel cancelLabel = new JLabel("Cancel");

    // Text area for note content
    private final JTextArea contentArea = new JTextArea();

    public NoteOverviewPanel(BetterNotesPlugin plugin, BetterNotesNote note, Runnable onBackAction)
    {
        this.plugin = plugin;
        this.note = note;
        this.onBackAction = onBackAction;

        // The main panel uses BorderLayout
        setLayout(new BorderLayout());
        setBackground(Helper.CONTENT_COLOR);

        // 1) TOP ROW: Just the Back button, right-aligned
        JPanel topRow = buildTopRow();
        add(topRow, BorderLayout.NORTH);

        // 2) MIDDLE PANEL: Contains (a) Title row, (b) spacing, (c) text area
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.setBackground(Helper.CONTENT_COLOR);

        // 2a) Title row: bigger note title (left) and Edit/Save/Cancel on the right
        JPanel titleRow = buildTitleRow();
        middlePanel.add(titleRow, BorderLayout.NORTH);

        // 2b) Some vertical spacing, then the text area in the CENTER
        middlePanel.add(Box.createVerticalStrut(8), BorderLayout.WEST); // optional side spacing
        middlePanel.add(Box.createVerticalStrut(8), BorderLayout.EAST); // optional side spacing

        // 2c) The text area (distinct background) in the center
        // We'll wrap it in a scrollpane so it can expand and scroll
        JScrollPane scrollPane = buildContentScrollPane();
        middlePanel.add(scrollPane, BorderLayout.CENTER);

        add(middlePanel, BorderLayout.CENTER);
    }

    /**
     * Builds the first row: a right-aligned "Back" button.
     */
    private JPanel buildTopRow()
    {
        // FlowLayout with RIGHT alignment
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topRow.setBackground(Helper.CONTENT_COLOR);

        JButton backButton = new JButton("Back");
        backButton.setBackground(Helper.BACKGROUND_COLOR);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(FontManager.getRunescapeSmallFont());
        backButton.addActionListener(e ->
        {
            note.setContent(contentArea.getText());
            plugin.getDataManager().updateConfig();
            onBackAction.run();
        });

        topRow.add(backButton);
        return topRow;
    }

    /**
     * Builds the second row: a bigger title on the left, and Edit/Save/Cancel on the right.
     */
    private JPanel buildTitleRow()
    {
        // We'll use a BorderLayout so the title is left, edit controls are right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Helper.CONTENT_COLOR);
        titleRow.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Title input on the LEFT
        titleInput.setText(note.getName());
        titleInput.setEditable(false);
        // Make the title bigger
        titleInput.getTextField().setFont(
                titleInput.getTextField().getFont().deriveFont(16f) // 16px font
        );
        titleInput.setBackground(Helper.BACKGROUND_COLOR);
        titleInput.getTextField().setForeground(Color.WHITE);
        titleInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 8));

        titleRow.add(titleInput, BorderLayout.CENTER);

        // Edit/Save/Cancel labels on the RIGHT
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        editPanel.setBackground(Helper.CONTENT_COLOR);

        // Configure the labels
        setupEditingLabels();
        editPanel.add(editLabel);
        editPanel.add(saveLabel);
        editPanel.add(cancelLabel);

        titleRow.add(editPanel, BorderLayout.EAST);

        return titleRow;
    }

    /**
     * Configures the edit/save/cancel logic for the title.
     */
    private void setupEditingLabels()
    {
        // Edit label
        editLabel.setFont(FontManager.getRunescapeSmallFont());
        editLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        editLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    titleInput.setEditable(true);
                    updateTitleActions(true);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                editLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                editLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });

        // Save label
        saveLabel.setFont(FontManager.getRunescapeSmallFont());
        saveLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        saveLabel.setVisible(false);
        saveLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    // Commit changes
                    note.setName(titleInput.getText());
                    // plugin.dataManager.updateConfig(); // If you'd like to persist now

                    // End editing mode
                    titleInput.setEditable(false);
                    updateTitleActions(false);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (saveLabel.isEnabled())
                {
                    saveLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
                }
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                saveLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
            }
        });

        // Cancel label
        cancelLabel.setFont(FontManager.getRunescapeSmallFont());
        cancelLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        cancelLabel.setVisible(false);
        cancelLabel.addMouseListener(new MouseAdapter()
        {
            private final String originalName = note.getName();

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    // Revert
                    titleInput.setText(originalName);
                    titleInput.setEditable(false);
                    updateTitleActions(false);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                cancelLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                cancelLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
        });
    }

    /**
     * Creates a scroll pane for the content area, with a distinct background.
     */
    private JScrollPane buildContentScrollPane()
    {
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(FontManager.getRunescapeSmallFont());
        // Give it a different background color for distinction
        contentArea.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentArea.setForeground(Color.WHITE);
        contentArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        contentArea.setText(note.getContent() != null ? note.getContent() : "");

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Let it fill the remaining space in the middlePanel
        return scrollPane;
    }

    /**
     * Show/hide the Edit vs. Save/Cancel controls for the note's title.
     */
    private void updateTitleActions(boolean editing)
    {
        editLabel.setVisible(!editing);
        saveLabel.setVisible(editing);
        cancelLabel.setVisible(editing);
    }
}
