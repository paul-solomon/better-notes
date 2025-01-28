package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SectionNotePanel extends JPanel
{
    private final BetterNotesPlugin plugin;
    private final BetterNotesNote note;

    private static final ImageIcon MORE_DETAILS_ICON;
    private static final ImageIcon MORE_DETAILS_ICON_HOVER;
    private static final ImageIcon FULL_SCREEN_ICON;
    private static final ImageIcon FULL_SCREEN_ICON_HOVER;

    static
    {
        BufferedImage moreDetailsImage = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/more_options.png");
        MORE_DETAILS_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreDetailsImage, 16, 16), 0.5f));
        MORE_DETAILS_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreDetailsImage, 16, 16), 1.0f));

        BufferedImage fullScreenImage = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/expand.png");
        FULL_SCREEN_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(fullScreenImage, 16, 16), 0.5f));
        FULL_SCREEN_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(fullScreenImage, 16, 16), 1.0f));
    }

    private final JLabel moreDetailsIcon = new JLabel();
    private final JLabel fullScreenIcon = new JLabel();
    private final JLabel minMaxLabel = new JLabel();
    private final JPanel expandedContentPanel = new JPanel();

    public SectionNotePanel(final BetterNotesPlugin plugin, final BetterNotesNote note, final String sectionId)
    {
        this.plugin = plugin;
        this.note = note;

        setBackground(Helper.DARK_GREY_COLOR);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout());
        setFocusable(false);
        setRequestFocusEnabled(false);

        JPanel headerPanel = createHeaderPanel(note);
        add(headerPanel, BorderLayout.NORTH);

        expandedContentPanel.setBackground(Helper.DARK_GREY_COLOR);
        expandedContentPanel.setLayout(new BorderLayout());
        expandedContentPanel.setVisible(note.isMaximized());

        ContentEditorPanel contentEditorPanel = new ContentEditorPanel(note, plugin);
        JScrollPane contentScrollPane = new JScrollPane(contentEditorPanel);
        contentScrollPane.setPreferredSize(new Dimension(0, 400));
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        expandedContentPanel.add(contentScrollPane, BorderLayout.CENTER);
        add(expandedContentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(BetterNotesNote note)
    {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(Helper.DARK_GREY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 34));

        setupMinMaxLabel();

        JLabel titleLabel = new JLabel(note.getName());
        titleLabel.setFont(FontManager.getRunescapeSmallFont());
        titleLabel.setForeground(Color.WHITE);

        setupIcons();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.WEST;
        headerPanel.add(minMaxLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        headerPanel.add(titleLabel, gc);

        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        iconsPanel.setOpaque(false);
        iconsPanel.add(moreDetailsIcon);
        iconsPanel.add(fullScreenIcon);

        gc.gridx = 2;
        gc.weightx = 0;
        headerPanel.add(iconsPanel, gc);

        return headerPanel;
    }

    private void setupIcons()
    {
        moreDetailsIcon.setIcon(MORE_DETAILS_ICON);
        moreDetailsIcon.setToolTipText("View more details");
        moreDetailsIcon.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    plugin.getPanel().showNoteOverview(note);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                moreDetailsIcon.setIcon(MORE_DETAILS_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                moreDetailsIcon.setIcon(MORE_DETAILS_ICON);
            }
        });

        fullScreenIcon.setIcon(FULL_SCREEN_ICON);
        fullScreenIcon.setToolTipText("Open in full screen");
        fullScreenIcon.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                // Hook for full-screen logic
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                fullScreenIcon.setIcon(FULL_SCREEN_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                fullScreenIcon.setIcon(FULL_SCREEN_ICON);
            }
        });
    }

    private void setupMinMaxLabel()
    {
        BufferedImage minimizeIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/chevron_down.png");
        BufferedImage minimizeHoverIcon = ImageUtil.luminanceOffset(minimizeIcon, -150);
        BufferedImage maximizeIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/chevron_right.png");
        BufferedImage maximizeHoverIcon = ImageUtil.luminanceOffset(maximizeIcon, -150);

        final ImageIcon MINIMIZE_ICON = new ImageIcon(ImageUtil.resizeImage(minimizeIcon, 16, 16));
        final ImageIcon MINIMIZE_ICON_HOVER = new ImageIcon(ImageUtil.resizeImage(minimizeHoverIcon, 16, 16));
        final ImageIcon MAXIMIZE_ICON = new ImageIcon(ImageUtil.resizeImage(maximizeIcon, 16, 16));
        final ImageIcon MAXIMIZE_ICON_HOVER = new ImageIcon(ImageUtil.resizeImage(maximizeHoverIcon, 16, 16));

        updateMinMaxLabel(MINIMIZE_ICON, MAXIMIZE_ICON);

        minMaxLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    note.setMaximized(!note.isMaximized());
                    plugin.getDataManager().updateConfig();
                    expandedContentPanel.setVisible(note.isMaximized());
                    revalidate();
                    repaint();
                }
            }
        });
    }

    private void updateMinMaxLabel(ImageIcon minimizeIcon, ImageIcon maximizeIcon)
    {
        if (note.isMaximized())
        {
            minMaxLabel.setIcon(minimizeIcon);
        }
        else
        {
            minMaxLabel.setIcon(maximizeIcon);
        }
    }

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
