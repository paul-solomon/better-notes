package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;
import com.google.common.collect.ImmutableList;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static net.runelite.client.hiscore.HiscoreSkill.*;

public class GridDialog extends JDialog
{
    private HiscoreSkill selectedSkill;
    private BetterNotesPlugin plugin;
    private BetterNotesNote note;
    private BetterNotesSection section;
    private final SpriteManager spriteManager;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final JPanel bossIconPanel;  // Holds the current implementation
    private final JPanel skillIconPanel; // Placeholder for Skill Icon selection

    // Paths to the normal and hover images for each selection type
    private final String ITEM_ICON_NORMAL = "/item_icon_selection.png";
    private final String ITEM_ICON_HOVER = "/item_icon_selection_hover.png";
    private final String BOSS_ICON_NORMAL = "/boss_icon_selection.png";
    private final String BOSS_ICON_HOVER = "/boss_icon_selection_hover.png";
    private final String SKILL_ICON_NORMAL = "/skill_icon_selection.png";
    private final String SKILL_ICON_HOVER = "/skill_icon_selection_hover.png";
    // Path to the background image for the main panel
    private final String MAIN_PANEL_BG = "/select_icon_type_bg.png";

    public GridDialog(Frame owner, SpriteManager spriteManager, BetterNotesPlugin plugin, BetterNotesNote note, BetterNotesSection section)
    {
        super(owner, "Please select icon type", true);
        this.spriteManager = spriteManager;
        this.cardLayout = new CardLayout();
        this.plugin = plugin;
        this.note = note;
        this.section = section;
        this.mainPanel = new JPanel(cardLayout);

        setLayout(new BorderLayout());
        setSize(230, 410); // Updated size
        setResizable(false);
        setLocationRelativeTo(owner);

        // Initialize screens
        JPanel mainScreen = createMainScreen();
        this.bossIconPanel = createBossIconScreen();
        this.skillIconPanel = createSkillsIconScreen();

        // Add all screens to the card layout
        mainPanel.add(mainScreen, "MainScreen");
        mainPanel.add(bossIconPanel, "BossIconScreen");
        mainPanel.add(skillIconPanel, "SkillIconScreen");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "MainScreen"); // Start with the main screen
    }

    /**
     * Creates the main screen with image selection buttons and a background image.
     */
    private JPanel createMainScreen()
    {
        // Load the background image for the main panel
        BufferedImage mainBgImage = loadImage(MAIN_PANEL_BG);

        // Use the custom BackgroundPanel instead of a plain JPanel.
        JPanel panel = new BackgroundPanel(mainBgImage);
        panel.setLayout(new BorderLayout());
        // The background color will show through transparent areas.
        panel.setBackground(Helper.DARK_GREY_COLOR);

        // Title label
        JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(22, 0, 22, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Image buttons panel
        JPanel imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));
        // Set a transparent background so the background image shows through.
        imagesPanel.setBackground(new Color(0, 0, 0, 0));

        // Add spacing between buttons
        int verticalSpacing = 8;

        // Item Icon Image Button
        JLabel itemIconLabel = createImageButton(
                ITEM_ICON_NORMAL,
                ITEM_ICON_HOVER,
                e ->
                {
                    if (section != null) {
                        plugin.setSectionIconFromSearch(section);
                    } else if (note != null) {
                        plugin.setNoteIconFromSearch(note);
                    }
                    dispose();
                }
        );
        itemIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        imagesPanel.add(itemIconLabel);
        imagesPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));

        // Boss Icon Image Button
        JLabel bossIconLabel = createImageButton(
                BOSS_ICON_NORMAL,
                BOSS_ICON_HOVER,
                e ->
                {
                    cardLayout.show(mainPanel, "BossIconScreen");
                    setSize(550, 550); // Ensure size remains consistent
                }
        );
        bossIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        imagesPanel.add(bossIconLabel);
        imagesPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));

        // Skill Icon Image Button
        JLabel skillIconLabel = createImageButton(
                SKILL_ICON_NORMAL,
                SKILL_ICON_HOVER,
                e ->
                {
                    cardLayout.show(mainPanel, "SkillIconScreen");
                    setSize(300, 600); // Ensure size remains consistent
                }
        );
        skillIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        imagesPanel.add(skillIconLabel);

        // Center the images panel on the main screen.
        panel.add(imagesPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Helper method to create an image button with hover effects.
     */
    private JLabel createImageButton(String normalImagePath, String hoverImagePath, ActionListener action)
    {
        // Load normal and hover images using ImageUtil.loadImageResource.
        BufferedImage normalImage = loadImage(normalImagePath);
        BufferedImage hoverImage = loadImage(hoverImagePath);

        ImageIcon normalIcon = new ImageIcon(ImageUtil.resizeImage(normalImage, 202, 104));
        ImageIcon hoverIcon = new ImageIcon(ImageUtil.resizeImage(hoverImage, 202, 104));

        JLabel label = new JLabel(normalIcon);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                label.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                label.setIcon(normalIcon);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                action.actionPerformed(null);
            }
        });

        return label;
    }

    /**
     * Helper method to load an image from resources using ImageUtil.loadImageResource.
     */
    private BufferedImage loadImage(String path)
    {
        try
        {
            BufferedImage image = ImageUtil.loadImageResource(BetterNotesPlugin.class, path);
            if (image == null)
            {
                throw new IOException("Resource not found: " + path);
            }
            return image;
        }
        catch (Exception e)
        {
            // Handle missing image
            System.err.println("Failed to load image: " + path);
            // Return a placeholder image
            BufferedImage placeholder = new BufferedImage(496, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = placeholder.createGraphics();
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, 496, 200);
            g2d.setColor(Color.RED);
            g2d.drawString("Image not found", 200, 100);
            g2d.dispose();
            return placeholder;
        }
    }

    /**
     * Creates the Boss Icon selection screen (Uses the current boss icon implementation).
     */
    private JPanel createBossIconScreen()
    {
        JPanel panel = new JPanel(new BorderLayout());

        List<HiscoreSkill> bossesList = ImmutableList.of(
                ABYSSAL_SIRE, ALCHEMICAL_HYDRA, AMOXLIATL,
                ARAXXOR, ARTIO, BARROWS_CHESTS,
                BRYOPHYTA, CALLISTO, CALVARION,
                CERBERUS, CHAMBERS_OF_XERIC, CHAMBERS_OF_XERIC_CHALLENGE_MODE,
                CHAOS_ELEMENTAL, CHAOS_FANATIC, COMMANDER_ZILYANA,
                CORPOREAL_BEAST, CRAZY_ARCHAEOLOGIST, DAGANNOTH_PRIME,
                DAGANNOTH_REX, DAGANNOTH_SUPREME, DERANGED_ARCHAEOLOGIST,
                DUKE_SUCELLUS, GENERAL_GRAARDOR, GIANT_MOLE,
                GROTESQUE_GUARDIANS, HESPORI, KALPHITE_QUEEN,
                KING_BLACK_DRAGON, KRAKEN, KREEARRA,
                KRIL_TSUTSAROTH, LUNAR_CHESTS, MIMIC,
                NEX, NIGHTMARE, PHOSANIS_NIGHTMARE,
                OBOR, PHANTOM_MUSPAH, SARACHNIS,
                SCORPIA, SCURRIUS, SKOTIZO,
                SOL_HEREDIT, SPINDEL, TEMPOROSS,
                THE_GAUNTLET, THE_CORRUPTED_GAUNTLET, THE_HUEYCOATL,
                THE_LEVIATHAN, THE_WHISPERER, THEATRE_OF_BLOOD,
                THEATRE_OF_BLOOD_HARD_MODE, THERMONUCLEAR_SMOKE_DEVIL, TOMBS_OF_AMASCUT,
                TOMBS_OF_AMASCUT_EXPERT, TZKAL_ZUK, TZTOK_JAD,
                VARDORVIS, VENENATIS, VETION,
                VORKATH, WINTERTODT, ZALCANO,
                ZULRAH
        );
        // Grid panel for boss selection
        JPanel gridPanel = createSpriteGridScreen(bossesList, 8);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Back button
        panel.add(createBackButton(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the Skill Icon selection screen.
     */
    private JPanel createSkillsIconScreen()
    {
        JPanel panel = new JPanel(new BorderLayout());

        List<HiscoreSkill> skillsList = ImmutableList.of(
                ATTACK, HITPOINTS, MINING,
                STRENGTH, AGILITY, SMITHING,
                DEFENCE, HERBLORE, FISHING,
                RANGED, THIEVING, COOKING,
                PRAYER, CRAFTING, FIREMAKING,
                MAGIC, FLETCHING, WOODCUTTING,
                RUNECRAFT, SLAYER, FARMING,
                CONSTRUCTION, HUNTER
        );
        // Grid panel for skill selection
        JPanel gridPanel = createSpriteGridScreen(skillsList, 3);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Back button
        panel.add(createBackButton(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a grid screen for displaying sprites.
     */
    private JPanel createSpriteGridScreen(List<HiscoreSkill> skills, int cols)
    {
        JPanel panel = new JPanel(new BorderLayout());

        // Grid panel for skills selection
        JPanel gridPanel = new JPanel(new GridLayout(0, cols, 4, 4)); // 8 columns, unlimited rows, 4px gap
        gridPanel.setBackground(Helper.DARK_GREY_COLOR);

        // Add grid items
        for (HiscoreSkill skill : skills)
        {
            JPanel container = new JPanel(new BorderLayout());
            container.setPreferredSize(new Dimension(50, 50)); // Adjusted size to fit grid
            container.setBackground(Helper.DARKER_GREY_COLOR);
            container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            container.setOpaque(true);

            JLabel box = new JLabel();
            box.setOpaque(false);
            box.setPreferredSize(new Dimension(40, 40));
            box.setHorizontalAlignment(SwingConstants.CENTER);
            box.setVerticalAlignment(SwingConstants.CENTER);

            // Load sprite asynchronously
            spriteManager.getSpriteAsync(skill.getSpriteId(), 0, (sprite) ->
                    SwingUtilities.invokeLater(() -> {
                        final BufferedImage scaledSprite = ImageUtil.resizeCanvas(sprite, 35, 35);
                        box.setIcon(new ImageIcon(scaledSprite));
                        box.setToolTipText(skill.getName());
                    })
            );

            // Mouse events for selection and hover effects
            MouseAdapter hoverEffect = new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    selectedSkill = skill;
                    dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    container.setBackground(Helper.DARK_GREY_COLOR);
                    container.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    container.setBackground(Helper.DARKER_GREY_COLOR);
                    container.repaint();
                }
            };

            container.addMouseListener(hoverEffect);
            box.addMouseListener(hoverEffect);

            container.add(box, BorderLayout.CENTER);
            gridPanel.add(container);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a back button to return to the main screen.
     */
    private JPanel createBackButton()
    {
        JPanel panel = new JPanel();
        panel.setBackground(Helper.DARK_GREY_COLOR);
        JButton backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.addActionListener(e ->
        {
            cardLayout.show(mainPanel, "MainScreen");
            setSize(230, 410);
        });
        panel.add(backButton);
        return panel;
    }

    public HiscoreSkill getSelectedSkill()
    {
        return selectedSkill;
    }

    /**
     * Custom panel that paints a background image.
     */
    private static class BackgroundPanel extends JPanel
    {
        private final Image backgroundImage;

        public BackgroundPanel(Image backgroundImage)
        {
            this.backgroundImage = backgroundImage;
            // Make the panel non-opaque so child components can be seen over the background.
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            if (backgroundImage != null)
            {
                // Draw the background image scaled to the full size of the panel.
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
