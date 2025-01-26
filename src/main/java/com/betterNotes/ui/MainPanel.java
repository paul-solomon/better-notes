package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import com.betterNotes.utility.Helper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MainPanel extends PluginPanel {

    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon ADD_PRESSED_ICON;

    private final JLabel title = new JLabel();
    private final BetterNotesPlugin plugin;

    private final JPanel sectionsView = new JPanel(new GridBagLayout());

    static
    {
        final BufferedImage addIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/new.png");
        ADD_ICON = new ImageIcon(addIcon);
        ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, -100));
        ADD_PRESSED_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, -50));
    }

    public MainPanel(BetterNotesPlugin plugin) {
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

        if (plugin.getSections().isEmpty())
        {
//            pointsView.add(noPointsPanel, constraints);
        }
        else
        {
            for (final BetterNotesSection section : plugin.getSections())
            {
                sectionsView.add(new SectionPanel(plugin, section, null), constraints);
                constraints.gridy++;

                JPanel spacer = new JPanel();
                spacer.setBackground(Helper.DARK_GREY_COLOR); // Set the desired color
                spacer.setPreferredSize(new Dimension(0, 10)); // Match the size of the rigid area
                sectionsView.add(spacer, constraints);
                constraints.gridy++;
            }
        }

        repaint();
        revalidate();
    }

    public void showNoteOverview(BetterNotesNote note)
    {
        // Clear out the main panel
        removeAll();

        // Create the NoteOverviewPanel, pass a "back" callback
        NoteOverviewPanel notePanel = new NoteOverviewPanel(
                plugin,
                note,
                this::showSectionsView,
                plugin.getItemManager(),
                plugin.getSpriteManager()
        );

        // Add the note panel
        add(notePanel, BorderLayout.CENTER);

        // Refresh
        revalidate();
        repaint();
    }

    public void showSectionsView()
    {
        removeAll();              // remove the note panel
        setLayout(new BorderLayout());
        // Re-add the top bar & sections layout
        // or just call rebuild() which does the same

        // We'll replicate the structure from the constructor
        JPanel topbarPanel = new JPanel(new BorderLayout());
        topbarPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Better Notes");
        title.setForeground(Color.WHITE);

        // We might re-add that "addButton" logic, or store it in a separate method
        // But an easier approach is to just call your existing constructor logic
        // or keep references to your existing top panel so you can re-add them.

        // If you want to keep it simple, just call:
        buildMainUI();

        revalidate();
        repaint();
    }

    public void buildMainUI() {
        JPanel topbarPanel = new JPanel(new BorderLayout());
        topbarPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

        title.setText("Better Notes");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeFont());

        JLabel addButton = new JLabel(ADD_ICON);
        addButton.setToolTipText("Add new section");

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

                plugin.addSection();
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

        topbarPanel.add(title, BorderLayout.WEST);
        topbarPanel.add(addButton, BorderLayout.EAST);

        add(topbarPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Helper.DARKER_GREY_COLOR);

        sectionsView.setBackground(Helper.DARKER_GREY_COLOR);

        centerPanel.add(sectionsView, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        rebuild();
    }
}