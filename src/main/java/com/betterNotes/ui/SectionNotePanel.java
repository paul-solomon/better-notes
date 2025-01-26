package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel for displaying an individual note within a section,
 * with a lighter background and some spacing.
 */
public class SectionNotePanel extends JPanel
{
    private final BetterNotesPlugin plugin;
    private final BetterNotesNote note;

    private final String sectionId;

    // Similar to your "Remove" label for sections
    private final JLabel removeLabel = new JLabel("Remove");

    public SectionNotePanel(final BetterNotesPlugin plugin, final BetterNotesNote note, final String sectionId)
    {
        this.plugin = plugin;
        this.note = note;
        this.sectionId = sectionId;

        // Use a lighter background than your main "Helper.CONTENT_COLOR"
        setBackground(Helper.CONTENT_COLOR.brighter());

        // Add some padding inside this panel: top, left, bottom, right = 8px
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // We'll use a simple BorderLayout for the main panel
        setLayout(new BorderLayout(8, 0)); // 8 px horizontal gap

        // --- TOP ROW: Name (left), Remove (right) ---
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false); // let the background show through
        topRow.setBorder(new EmptyBorder(0, 0, 4, 0)); // small bottom gap

        JLabel nameLabel = new JLabel(note.getName());
        nameLabel.setFont(FontManager.getRunescapeSmallFont());
        nameLabel.setForeground(Color.WHITE);

        topRow.add(nameLabel, BorderLayout.WEST);

        removeLabel.setFont(FontManager.getRunescapeSmallFont());
        removeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
        removeLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    // Hook into your plugin's "remove note" logic
//                    plugin.removeNote(note);
                    plugin.deleteNoteFromSection(note.getId(), sectionId);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                removeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                removeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
            }
        });
        topRow.add(removeLabel, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // --- MIDDLE SECTION: Vertical layout for preview & "Open details" button ---
        JPanel middleSection = new JPanel();
        middleSection.setOpaque(false);
        // Stack components vertically
        middleSection.setLayout(new BoxLayout(middleSection, BoxLayout.Y_AXIS));

        // Limit note content to 200 chars
        String content = note.getContent() != null ? note.getContent() : "";
        if (content.length() > 200)
        {
            content = content.substring(0, 200);
        }

        // Use HTML if you want wrapping at ~200px
        String htmlPreview = String.format(
                "<html><body style='width:200px;'>%s</body></html>",
                content
        );
        JLabel previewLabel = new JLabel(htmlPreview);
        previewLabel.setFont(FontManager.getRunescapeSmallFont());
        previewLabel.setForeground(Color.WHITE);

        // Make sure each component aligns on the left (or center, if you prefer)
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add the preview label
        middleSection.add(previewLabel);

        // Add some vertical spacing between preview and the button
        middleSection.add(Box.createVerticalStrut(5));

        // "Open details" button
        JButton openDetailsButton = new JButton("Open details");
        openDetailsButton.setBackground(Helper.BACKGROUND_COLOR);
        openDetailsButton.setForeground(Color.WHITE);
        openDetailsButton.setFont(FontManager.getRunescapeSmallFont());
        // Align left as well
        openDetailsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        openDetailsButton.addActionListener(e ->
        {
            // TODO: Show a detailed view for this note
            plugin.getPanel().showNoteOverview(note);
        });

        middleSection.add(openDetailsButton);

        // Finally, add the middle section to the center of the panel
        add(middleSection, BorderLayout.CENTER);
    }
}
