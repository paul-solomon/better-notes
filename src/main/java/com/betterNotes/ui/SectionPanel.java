package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;
import com.google.common.collect.ImmutableList;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import static net.runelite.client.hiscore.HiscoreSkill.*;

/**
 * Panel representing a single "section" with minimize/maximize functionality.
 */
public class SectionPanel extends JPanel {
    private final BetterNotesPlugin plugin;
    private final BetterNotesSection section;
    private static final ImageIcon MINIMIZE_ICON;
    private static final ImageIcon MINIMIZE_ICON_HOVER;
    private static final ImageIcon MAXIMIZE_ICON;
    private static final ImageIcon MAXIMIZE_ICON_HOVER;
    private static final ImageIcon MORE_OPTIONS_ICON;
    private static final ImageIcon MORE_OPTIONS_ICON_HOVER;
    private static final ImageIcon ADD_NOTE_ICON;
    private static final ImageIcon ADD_NOTE_ICON_HOVER;
    private final JLabel minMaxLabel = new JLabel();
    private JPanel nameActions;
    private JLabel addNote;
    private JLabel moreOptions;
    private JLabel saveButton;
    private JLabel cancelButton;

    private final JPanel expandedContentPanel = new JPanel();

    static {
        BufferedImage downArrow = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/chevron_down.png");
        MINIMIZE_ICON = new ImageIcon(downArrow);
        MINIMIZE_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(downArrow, -150));

        BufferedImage rightArrow = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/chevron_right.png");
        MAXIMIZE_ICON = new ImageIcon(rightArrow);
        MAXIMIZE_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(rightArrow, -150));

        BufferedImage moreOptions = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/more_options.png");
        MORE_OPTIONS_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreOptions, 16, 16), 0.5f));
        MORE_OPTIONS_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(moreOptions, 16, 16), 1.0f));

        BufferedImage addNote = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/new.png");
        ADD_NOTE_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(addNote, 16, 16), 0.5f));
        ADD_NOTE_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(addNote, 16, 16), 1.0f));
    }

    public SectionPanel(BetterNotesPlugin plugin, BetterNotesSection section, final MouseAdapter flatTextFieldMouseAdapter) {
        this.plugin = plugin;
        this.section = section;

        setLayout(new BorderLayout());
        setBackground(Helper.DARKER_GREY_COLOR);

        // === Top Bar ===
        JPanel nameWrapper = new JPanel(new GridBagLayout());
        nameWrapper.setBackground(Helper.DARKER_GREY_COLOR);
        nameWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // No gaps or borders

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setOpaque(false); // Transparent background

        if (section.hasIcon()) {
            if (section.hasSpriteIcon()) {
                // Placeholder for sprite icon
                plugin.getSpriteManager().getSpriteAsync(section.getSpriteId(), 0, (sprite) ->
                        SwingUtilities.invokeLater(() -> {
                            final BufferedImage scaledSprite = ImageUtil.resizeImage(ImageUtil.resizeCanvas(sprite, 35, 35), 32, 32); // Scale for better fit
                            iconLabel.setIcon(new ImageIcon(scaledSprite));
                        }));
            } else if (section.hasItemIcon()) {
                AsyncBufferedImage itemImg = plugin.getItemManager().getImage(section.getItemId(), 0, false);
                iconLabel.setIcon(new ImageIcon(itemImg));
            }
        }

        FlatTextField nameInput = createNameInput(section.getName(), flatTextFieldMouseAdapter);
        nameInput.setBackground(Helper.DARKER_GREY_COLOR);
        JPanel nameActions = createNameActions(nameInput, flatTextFieldMouseAdapter);
        setupMinMaxLabel();

        GridBagConstraints gc = new GridBagConstraints();

        // Minimize/Maximize button
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 8, 0, 0);
        nameWrapper.add(minMaxLabel, gc);

        // Icon (if applicable)
        if (section.hasIcon()) {
            gc = new GridBagConstraints();
            gc.gridx = 1;
            gc.gridy = 0;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 4, 0, 4); // Spacing
            nameWrapper.add(iconLabel, gc);
        }

        // Section title
        gc = new GridBagConstraints();
        gc.gridx = 2;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        nameWrapper.add(nameInput, gc);

        // Action buttons
        gc = new GridBagConstraints();
        gc.gridx = 3;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.anchor = GridBagConstraints.EAST;
        gc.insets = new Insets(0, 0, 0, 8);
        nameWrapper.add(nameActions, gc);

        add(nameWrapper, BorderLayout.NORTH);

        // === Expandable Content ===
        expandedContentPanel.setBackground(Helper.DARKER_GREY_COLOR);
        expandedContentPanel.setLayout(new BorderLayout());
        expandedContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Uniform padding on all sides
        expandedContentPanel.setVisible(section.isMaximized());

        JPanel moreContentPanel = new JPanel();
        moreContentPanel.setLayout(new BoxLayout(moreContentPanel, BoxLayout.Y_AXIS));
        moreContentPanel.setBackground(Helper.DARKER_GREY_COLOR);

        if (section.getNotes().isEmpty()) {
            moreContentPanel.add(createNoNotesMessage());
        } else {
            List<BetterNotesNote> notes = section.getNotes();
            int noteCount = notes.size();

            for (int i = 0; i < noteCount; i++) {
                BetterNotesNote note = notes.get(i);
                SectionNotePanel notePanel = new SectionNotePanel(plugin, note, section, null);
                moreContentPanel.add(notePanel);

                // Add a vertical strut only if this is not the last note
                if (i < noteCount - 1) {
                    moreContentPanel.add(Box.createVerticalStrut(5));
                }
            }
        }

        expandedContentPanel.add(moreContentPanel, BorderLayout.CENTER);
        add(expandedContentPanel, BorderLayout.CENTER);

        if (section.isNewSection()) {
            startRenaming(nameInput);
            section.setNewSection(false);
        }
    }


    private JPanel createNameActions(FlatTextField nameInput, MouseAdapter flatTextFieldMouseAdapter) {
        nameActions = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 40);
            }
        };
        nameActions.setBackground(Helper.DARKER_GREY_COLOR);

        saveButton = new JLabel("Save");
        cancelButton = new JLabel("Cancel");

        saveButton.setForeground(Color.GRAY);
        cancelButton.setForeground(Color.RED);

        moreOptions = new JLabel(MORE_OPTIONS_ICON);
        moreOptions.setToolTipText("More options");

        addNote = new JLabel(ADD_NOTE_ICON);
        addNote.setToolTipText("Add new note");
        addNote.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (section.isUnassignedNotesSection()) {
                        plugin.addNoteToUnassignedSection();
                    } else {
                        plugin.addNoteToSection(section.getId());
                    }

                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                addNote.setIcon(ADD_NOTE_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addNote.setIcon(ADD_NOTE_ICON);
            }
        });

        GridBagConstraints gc = new GridBagConstraints();

        moreOptions.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JPopupMenu optionsMenu = new JPopupMenu();

                    JMenuItem setRemoveIcon = new JMenuItem(section.hasIcon() ? "Change Section Icon" : "Set Section Icon");
                    setRemoveIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                plugin.openSectionIconPickerDialog(section);
                        }
                    });

                    JMenuItem removeIcon = new JMenuItem("Remove Section Icon");
                    removeIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.removeSectionIcon(section, false);
                        }
                    });

                    if (section.hasIcon()) {
                        optionsMenu.add(removeIcon);
                    }

                    JMenuItem renameSection = new JMenuItem("Rename Section");
                    renameSection.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            startRenaming(nameInput);
                        }
                    });

                    JMenuItem deleteSection = new JMenuItem("Delete Section");
                    deleteSection.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.deleteSection(section);
                        }
                    });

                    optionsMenu.add(setRemoveIcon);
                    optionsMenu.add(renameSection);
                    optionsMenu.add(deleteSection);

                    optionsMenu.show(moreOptions, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                moreOptions.setIcon(MORE_OPTIONS_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                moreOptions.setIcon(MORE_OPTIONS_ICON);
            }
        });

        // More Options button
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.0;
        gc.insets = new Insets(0, 0, 0, 4); // 4px spacing to the right
        gc.anchor = GridBagConstraints.CENTER;
        if (!section.isUnassignedNotesSection()) {
            nameActions.add(moreOptions, gc);
        }

        // Add Note button
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0.0;
        gc.insets = new Insets(0, 4, 0, 0); // 4px spacing to the left
        gc.anchor = GridBagConstraints.CENTER;
        nameActions.add(addNote, gc);


        return nameActions;
    }

    private JPanel createNoNotesMessage() {
        // Create a panel to hold the message
        JPanel noNotesPanel = new JPanel();
        noNotesPanel.setLayout(new BoxLayout(noNotesPanel, BoxLayout.Y_AXIS));
        noNotesPanel.setBackground(Helper.DARKER_GREY_COLOR);

        // Create the label for the first line
        JLabel noNotesText = new JLabel("There are no notes in this section.");
        noNotesText.setFont(FontManager.getRunescapeSmallFont());
        noNotesText.setForeground(Color.WHITE);
        noNotesText.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align the text

        // Create a sub-panel for the second line with icon and text
        JPanel textWithIconPanel = new JPanel();
        textWithIconPanel.setLayout(new BoxLayout(textWithIconPanel, BoxLayout.X_AXIS));
        textWithIconPanel.setBackground(Helper.DARKER_GREY_COLOR);
        textWithIconPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align the panel

        // Add "Press the " text
        JLabel prefixText = new JLabel("Press the ");
        prefixText.setFont(FontManager.getRunescapeSmallFont());
        prefixText.setForeground(Color.WHITE);

        // Add the icon
        JLabel addNoteIconLabel = new JLabel(ADD_NOTE_ICON);

        // Add " to create a new one" text
        JLabel suffixText = new JLabel(" to create a new one.");
        suffixText.setFont(FontManager.getRunescapeSmallFont());
        suffixText.setForeground(Color.WHITE);

        // Add text and icon components to the sub-panel
        textWithIconPanel.add(prefixText);
        textWithIconPanel.add(addNoteIconLabel);
        textWithIconPanel.add(suffixText);

        // Add components to the main panel
        noNotesPanel.add(noNotesText);
        noNotesPanel.add(Box.createVerticalStrut(5)); // Add spacing between lines
        noNotesPanel.add(textWithIconPanel);

        return noNotesPanel;
    }

    private FlatTextField createNameInput(String initialText, MouseAdapter flatTextFieldMouseAdapter) {
        FlatTextField nameInput = new FlatTextField();
        nameInput.setText(initialText);
        nameInput.setBorder(null);
        nameInput.setEditable(false);
        nameInput.setBackground(Helper.DARK_GREY_COLOR);
        nameInput.setPreferredSize(new Dimension(0, 24));
        nameInput.getTextField().setForeground(Color.WHITE);
        nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));
        nameInput.getTextField().addMouseListener(flatTextFieldMouseAdapter);
        return nameInput;
    }

    private void setupMinMaxLabel() {
        updateMinMaxLabel();

        minMaxLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minMaxLabel.setIcon(section.isMaximized() ? MINIMIZE_ICON_HOVER : MAXIMIZE_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateMinMaxLabel();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    section.setMaximized(!section.isMaximized());
                    plugin.getDataManager().updateConfig();
                    updateMinMaxLabel();

                    // Toggle visibility of expandedContentPanel
                    expandedContentPanel.setVisible(section.isMaximized());

                    // Adjust layout to ensure no remnant space
                    revalidate();
                    repaint();
                }
            }
        });
    }

    private void updateMinMaxLabel() {
        if (section.isMaximized()) {
            minMaxLabel.setIcon(MINIMIZE_ICON);
            minMaxLabel.setToolTipText("Click to collapse");
        } else {
            minMaxLabel.setIcon(MAXIMIZE_ICON);
            minMaxLabel.setToolTipText("Click to expand");
        }
    }

    private static BufferedImage setImageOpacity(BufferedImage image, float opacity) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    private void startRenaming(FlatTextField nameInput) {

        String initialName = nameInput.getText();

        nameInput.setEditable(true);

        SwingUtilities.invokeLater(() -> {
            nameInput.getTextField().selectAll();
            nameInput.getTextField().requestFocus();
        });

        nameActions.remove(addNote);

        nameActions.remove(moreOptions);

        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;

        gc.gridy = 0;

        gc.insets = new Insets(0, 4, 0, 4);

        gc.anchor = GridBagConstraints.CENTER;

        nameActions.add(saveButton, gc);

        gc.gridx++;

        nameActions.add(cancelButton, gc);

        nameActions.revalidate();

        nameActions.repaint();


        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {

                    String newName = nameInput.getText().trim();

                    if (!newName.equals(initialName) && !newName.isEmpty()) {
                        section.setName(newName);
                        plugin.getDataManager().updateConfig();
                        plugin.redrawMainPanel();
                    }

                    endRenaming(nameInput, nameActions, addNote, moreOptions, saveButton, cancelButton, initialName);
                }
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                String newName = nameInput.getText().trim();

                if (!newName.equals(initialName) && !newName.isEmpty()) {
                    saveButton.setForeground(Color.GREEN.darker());
                } else {
                    saveButton.setForeground(Color.GRAY.darker());
                }
            }


            @Override
            public void mouseExited(MouseEvent e) {
                String newName = nameInput.getText().trim();

                if (!newName.equals(initialName) && !newName.isEmpty()) {
                    saveButton.setForeground(Color.GREEN);
                } else {
                    saveButton.setForeground(Color.GRAY);
                }
            }
        });


        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    nameInput.setText(initialName);
                    endRenaming(nameInput, nameActions, addNote, moreOptions, saveButton, cancelButton, initialName);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setForeground(Color.RED.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setForeground(Color.RED);
            }
        });


        nameInput.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkSaveButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkSaveButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkSaveButtonState();
            }

            private void checkSaveButtonState() {
                String currentText = nameInput.getText().trim();
                if (currentText.equals(initialName) || currentText.isEmpty()) {
                    saveButton.setForeground(Color.GRAY);
                    saveButton.setToolTipText("No changes to save");
                } else {
                    saveButton.setForeground(Color.GREEN);
                    saveButton.setToolTipText(null);
                }
            }
        });
    }

    private void endRenaming(FlatTextField nameInput, JPanel nameActions, JLabel addNote, JLabel moreOptions, JLabel saveButton, JLabel cancelButton, String finalName) {
        nameInput.setEditable(false);
        nameInput.setText(finalName);

        nameActions.remove(saveButton);
        nameActions.remove(cancelButton);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 4);
        gc.anchor = GridBagConstraints.CENTER;

        nameActions.add(moreOptions, gc);
        gc.gridx++;
        nameActions.add(addNote, gc);

        nameActions.revalidate();
        nameActions.repaint();
    }
}
