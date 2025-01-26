package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A panel that shows an overview of a single note:
 * - First row: Back button (right aligned)
 * - Second row: Larger title in a FlatTextField (left) + Edit/Save/Cancel (right)
 * - Spacing
 * - JTextArea filling the rest of the space (with a distinct background)
 * - A row for selecting an item icon, which updates immediately when chosen
 */
public class NoteOverviewPanel extends JPanel
{
    private final BetterNotesPlugin plugin;
    private final BetterNotesNote note;
    private final Runnable onBackAction;
    private final ItemManager itemManager;

    private final SpriteManager spriteManager;

    // For the note title
    private final FlatTextField titleInput = new FlatTextField();

    // Editing controls
    private final JLabel editLabel = new JLabel("Edit");
    private final JLabel saveLabel = new JLabel("Save");
    private final JLabel cancelLabel = new JLabel("Cancel");

    // Text area for the note content
    private final JTextArea contentArea = new JTextArea();

    // Container for displaying the note’s item icon
    private final JPanel itemImageContainer = new JPanel(new BorderLayout());

    public NoteOverviewPanel(BetterNotesPlugin plugin, BetterNotesNote note, Runnable onBackAction, ItemManager itemManager, SpriteManager spriteManager)
    {
        this.plugin = plugin;
        this.note = note;
        this.onBackAction = onBackAction;
        this.itemManager = itemManager;
        this.spriteManager = spriteManager;

        setLayout(new BorderLayout());
        setBackground(Helper.CONTENT_COLOR);

        // 1) TOP ROW: Just the Back button, right-aligned
        JPanel topRow = buildTopRow();
        add(topRow, BorderLayout.NORTH);

        // 2) MIDDLE PANEL: Contains (a) Title row, (b) spacing, (c) text area
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.setBackground(Helper.CONTENT_COLOR);

        // 2a) Title row
        JPanel titleRow = buildTitleRow();
        middlePanel.add(titleRow, BorderLayout.NORTH);

        // 2b) Some vertical spacing on the sides
        middlePanel.add(Box.createVerticalStrut(8), BorderLayout.WEST);
        middlePanel.add(Box.createVerticalStrut(8), BorderLayout.EAST);

        // 2c) Scrollable text area in the center
        JScrollPane scrollPane = buildContentScrollPane();
        middlePanel.add(scrollPane, BorderLayout.CENTER);

        add(middlePanel, BorderLayout.CENTER);

        // 3) Item selection row at the bottom
        JPanel itemSelectionRow = buildItemSelectionRow();
        add(itemSelectionRow, BorderLayout.SOUTH);
    }

    /**
     * Builds the first row: a right-aligned "Back" button.
     */
    private JPanel buildTopRow()
    {
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

        setupEditingLabels();
        editPanel.add(editLabel);
        editPanel.add(saveLabel);
        editPanel.add(cancelLabel);

        titleRow.add(editPanel, BorderLayout.EAST);

        return titleRow;
    }

    /**
     * Creates a scroll pane for the content area, with a distinct background.
     */
    private JScrollPane buildContentScrollPane()
    {
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(FontManager.getRunescapeSmallFont());
        contentArea.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentArea.setForeground(Color.WHITE);
        contentArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        contentArea.setText(note.getContent() != null ? note.getContent() : "");

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        return scrollPane;
    }

    /**
     * Bottom row: "Select item icon" button + item image container.
     */
    private JPanel buildItemSelectionRow()
    {
        JPanel itemSelectionRow = new JPanel(new BorderLayout());
        itemSelectionRow.setBackground(Helper.CONTENT_COLOR);
        itemSelectionRow.setBorder(new EmptyBorder(5, 10, 5, 10));

        // A button to pick the note's icon
        JButton selectItemButton = new JButton("Select item icon");
        selectItemButton.setBackground(Helper.BACKGROUND_COLOR);
        selectItemButton.setForeground(Color.WHITE);
        selectItemButton.setFont(FontManager.getRunescapeSmallFont());
        // Pass "this" so the plugin can refresh this panel after selection
        selectItemButton.addActionListener(e -> plugin.setNoteIconFromSearch(note, this));
        itemSelectionRow.add(selectItemButton, BorderLayout.CENTER);

        // The item icon container at the bottom
        itemImageContainer.setBackground(Helper.BACKGROUND_COLOR.darker());
        itemImageContainer.setPreferredSize(new Dimension(50, 50));
        itemImageContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        itemSelectionRow.add(itemImageContainer, BorderLayout.SOUTH);

        // If note has an item already set, load it
        if (note.isItemSet(note))
        {
            loadItemIcon();
        }

//        Example of how to load a sprite -> we will use this for boss/skill selection
//        JLabel testLabel = new JLabel();
//
//        spriteManager.getSpriteAsync(SpriteID.HISCORE_THE_GAUNTLET, 0, (sprite) ->
//                SwingUtilities.invokeLater(() ->
//                {
//                    // Icons are all 25x25 or smaller, so they're fit into a 25x25 canvas to give them a consistent size for
//                    // better alignment. Further, they are then scaled down to 20x20 to not be overly large in the panel.
//                    final BufferedImage scaledSprite = ImageUtil.resizeImage(ImageUtil.resizeCanvas(sprite, 25, 25), 30, 30);
//                    testLabel.setIcon(new ImageIcon(scaledSprite));
//                    itemSelectionRow.add(testLabel, BorderLayout.SOUTH);
//                }));

        return itemSelectionRow;
    }

    /**
     * Loads the note's item icon into the container.
     * Called once when constructing the panel and again after changing itemId.
     */
    private void loadItemIcon()
    {
        // Clear old icon
        itemImageContainer.removeAll();

        // If note doesn't actually have a valid item
        if (!note.isItemSet(note))
        {
            itemImageContainer.revalidate();
            itemImageContainer.repaint();
            return;
        }

        // Load asynchronously
        AsyncBufferedImage itemImg = itemManager.getImage(note.getItemId(), 0, false);

        JLabel imageLabel = new JLabel(new ImageIcon(itemImg));

        // Once loaded, revalidate
        itemImg.onLoaded(() ->
        {
            imageLabel.setIcon(new ImageIcon(itemImg));
            itemImageContainer.revalidate();
            itemImageContainer.repaint();
        });

        itemImageContainer.add(imageLabel, BorderLayout.CENTER);
        itemImageContainer.revalidate();
        itemImageContainer.repaint();
    }

    /**
     * Public method for plugin to refresh the icon in place (called after item selection).
     */
    public void refreshIcon()
    {
        loadItemIcon();
    }

    /**
     * Configures the edit/save/cancel logic for the note's title.
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
                    // commit title
                    note.setName(titleInput.getText());
                    // plugin.dataManager.updateConfig(); (optional)
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
     * Show/hide the Edit vs. Save/Cancel controls for the note's title.
     */
    private void updateTitleActions(boolean editing)
    {
        editLabel.setVisible(!editing);
        saveLabel.setVisible(editing);
        cancelLabel.setVisible(editing);
    }
}
