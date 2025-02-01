package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MainPanel extends PluginPanel
{
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon ADD_PRESSED_ICON;

    // -- Added icons for 'more options' --
    private static final ImageIcon MORE_OPTIONS_ICON;
    private static final ImageIcon MORE_OPTIONS_ICON_HOVER;

    private final JLabel title = new JLabel();
    private final BetterNotesPlugin plugin;

    private final JPanel sectionsView = new JPanel(new GridBagLayout());

    private boolean isReorderMode = false;

    static
    {
        // Existing add button icons
        final BufferedImage addIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/new.png");
        ADD_ICON = new ImageIcon(addIcon);
        ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, -100));
        ADD_PRESSED_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, -50));

        // More-options icons from SectionNotePanel logic
        final BufferedImage moreOptionsImg = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/more_options.png");
        // Resize to 16x16, then apply different opacities for default vs. hover
        MORE_OPTIONS_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreOptionsImg, 16, 16), 0.5f));
        MORE_OPTIONS_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreOptionsImg, 16, 16), 1.0f));
    }

    public MainPanel(BetterNotesPlugin plugin)
    {
        this.plugin = plugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildMainUI();
    }

    public void rebuild()
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridy = 0;

        sectionsView.removeAll();

        // If no sections and no unassigned notes, show placeholder
        if (plugin.getSections().isEmpty() && plugin.getUnassignedNotesSection() == null)
        {
            JLabel noSectionsLabel = new JLabel("No sections available.");
            noSectionsLabel.setForeground(Color.LIGHT_GRAY);
            noSectionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sectionsView.add(noSectionsLabel, constraints);
            constraints.gridy++;
        }
        else
        {
            // Add each regular section
            for (final BetterNotesSection section : plugin.getSections())
            {
                sectionsView.add(new SectionPanel(plugin, section, null), constraints);
                constraints.gridy++;

                // Spacer
                JPanel spacer = new JPanel();
                spacer.setBackground(Helper.DARK_GREY_COLOR);
                spacer.setPreferredSize(new Dimension(0, 10));
                sectionsView.add(spacer, constraints);
                constraints.gridy++;
            }

            // Add unassigned notes section if present
            BetterNotesSection unassignedNotesSection = plugin.getUnassignedNotesSection();
            if (unassignedNotesSection != null && !unassignedNotesSection.getNotes().isEmpty())
            {
                SectionPanel unassignedPanel = new SectionPanel(plugin, unassignedNotesSection, null);
                unassignedPanel.setBackground(Helper.DARKER_GREY_COLOR); // Optional styling
                sectionsView.add(unassignedPanel, constraints);
                constraints.gridy++;
            }
        }

        repaint();
        revalidate();
    }

    public void showNoteOverview(BetterNotesNote note)
    {
        removeAll();

        NoteOverviewPanel notePanel = new NoteOverviewPanel(
                plugin,
                note,
                this::showSectionsView,
                plugin.getItemManager(),
                plugin.getSpriteManager()
        );

        add(notePanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public void showReorderMode() {
        removeAll();

        JPanel topBarPanel = buildHeaderPanel();
        add(topBarPanel, BorderLayout.NORTH);

        // -- Center panel with sections --
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Helper.DARKER_GREY_COLOR);

        ReoderViewPanel reorderView = new ReoderViewPanel(plugin);
        centerPanel.add(reorderView, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Finally, build/rebuild the sections
        rebuild();
    }

    public void showSectionsView()
    {
        removeAll();
        setLayout(new BorderLayout());

        buildMainUI();

        revalidate();
        repaint();
    }

    public void buildMainUI()
    {

        JPanel topBarPanel = buildHeaderPanel();
        add(topBarPanel, BorderLayout.NORTH);

        // -- Center panel with sections --
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Helper.DARKER_GREY_COLOR);

        sectionsView.setBackground(Helper.DARKER_GREY_COLOR);
        centerPanel.add(sectionsView, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Finally, build/rebuild the sections
        rebuild();
    }

    public JPanel buildHeaderPanel() {
        // -- Top bar --
        JPanel topBarPanel = new JPanel(new BorderLayout());
        topBarPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Better Notes");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeFont());

        topBarPanel.add(title, BorderLayout.WEST);

        if (!isReorderMode) {
            JPanel mainRightIconsPanel = buildMainTopBarButtons();

            topBarPanel.add(mainRightIconsPanel, BorderLayout.EAST);
        } else {
            JPanel reorderModeRightIconsPanel = buildReorderModeTopBarPanel();

            topBarPanel.add(reorderModeRightIconsPanel, BorderLayout.EAST);
        }


        return topBarPanel;
    }

    public JPanel buildMainTopBarButtons () {
        // 1) The more-options button
        JLabel moreOptionsButton = new JLabel(MORE_OPTIONS_ICON);
        moreOptionsButton.setToolTipText("More options");

        // Popup menu for moreOptions
        JPopupMenu moreOptionsMenu = new JPopupMenu();
        JMenuItem reorderItem = new JMenuItem("Reorder sections and notes");
        reorderItem.addActionListener(e -> {
            isReorderMode = true;
            showReorderMode();
        });
        moreOptionsMenu.add(reorderItem);

        moreOptionsButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (Helper.checkClick(e))
                {
                    return;
                }
                // We could show a "pressed" look if you like
                // or just set to hover icon again
                moreOptionsButton.setIcon(MORE_OPTIONS_ICON_HOVER);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (Helper.checkClick(e))
                {
                    return;
                }
                // Show menu on release
                moreOptionsMenu.show(moreOptionsButton, e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                moreOptionsButton.setIcon(MORE_OPTIONS_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                moreOptionsButton.setIcon(MORE_OPTIONS_ICON);
            }
        });

        // 2) The add button
        JLabel addButton = new JLabel(ADD_ICON);
        addButton.setToolTipText("Add new section/note");

        JPopupMenu addMenu = new JPopupMenu();
        JMenuItem addSectionItem = new JMenuItem("Add Section");
        JMenuItem addNoteItem = new JMenuItem("Add Note");

        addSectionItem.addActionListener(e -> plugin.addSection());
        addNoteItem.addActionListener(e -> plugin.addNote());

        addMenu.add(addSectionItem);
        addMenu.add(addNoteItem);

        addButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (Helper.checkClick(mouseEvent))
                {
                    return;
                }
                addButton.setIcon(ADD_PRESSED_ICON);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent)
            {
                if (Helper.checkClick(mouseEvent))
                {
                    return;
                }
                addMenu.show(addButton, mouseEvent.getX(), mouseEvent.getY());
                addButton.setIcon(ADD_HOVER_ICON);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                addButton.setIcon(ADD_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                addButton.setIcon(ADD_ICON);
            }
        });

        // Put the two buttons side by side on the top bar (right side)
        JPanel rightIconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightIconsPanel.setOpaque(false);
        rightIconsPanel.add(moreOptionsButton);
        rightIconsPanel.add(addButton);

        return rightIconsPanel;
    }

    public JPanel buildReorderModeTopBarPanel () {

        JLabel finishButton = new JLabel("Finish");
        finishButton.setForeground(Color.WHITE);

        finishButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isReorderMode = false;
                    showSectionsView();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                finishButton.setForeground(Color.WHITE.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
               finishButton.setForeground(Color.WHITE);
            }
        });

        // Put the two buttons side by side on the top bar (right side)
        JPanel rightIconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightIconsPanel.setOpaque(false);
        rightIconsPanel.add(finishButton);

        return rightIconsPanel;
    }

    /**
     * Helper function to set opacity of an image.
     * Re-implements logic from SectionNotePanel for consistent icons.
     */
    private static BufferedImage setImageOpacity(BufferedImage image, float opacity)
    {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return newImage;
    }
}
