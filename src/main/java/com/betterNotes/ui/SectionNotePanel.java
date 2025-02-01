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

public class SectionNotePanel extends JPanel {
    private final BetterNotesPlugin plugin;
    private final BetterNotesSection section;
    private final BetterNotesNote note;
    private static final ImageIcon MINIMIZE_ICON;
    private static final ImageIcon MINIMIZE_ICON_HOVER;
    private static final ImageIcon MAXIMIZE_ICON;
    private static final ImageIcon MAXIMIZE_ICON_HOVER;
    private static final ImageIcon MORE_OPTIONS_ICON;
    private static final ImageIcon MORE_OPTIONS_ICON_HOVER;
    private static final ImageIcon FULL_SCREEN_ICON;
    private static final ImageIcon FULL_SCREEN_ICON_HOVER;
    private final JLabel minMaxLabel = new JLabel();
    private JPanel nameActions;
    private JLabel expandNote;
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

        BufferedImage fullScreenImage = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/expand.png");
        FULL_SCREEN_ICON = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(fullScreenImage, 16, 16), 0.5f));
        FULL_SCREEN_ICON_HOVER = new ImageIcon(setImageOpacity(ImageUtil.resizeImage(fullScreenImage, 16, 16), 1.0f));
    }

    public SectionNotePanel(BetterNotesPlugin plugin, BetterNotesNote note, BetterNotesSection section, final MouseAdapter flatTextFieldMouseAdapter) {
        this.plugin = plugin;
        this.section = section;
        this.note = note;

        setLayout(new BorderLayout());
        setBackground(Helper.DARK_GREY_COLOR);

        // === Top Bar ===
        JPanel nameWrapper = new JPanel(new GridBagLayout());
        nameWrapper.setBackground(Helper.DARK_GREY_COLOR);
        nameWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // No gaps or borders

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setOpaque(false); // Transparent background

        if (note.hasIcon()) {
            if (note.hasSpriteIcon()) {
                plugin.getSpriteAsync(note.getSpriteId(), (spriteImage) ->
                {
                    if (spriteImage != null)
                    {
                        SwingUtilities.invokeLater(() ->
                        {
                            iconLabel.setIcon(new ImageIcon(spriteImage));
                            iconLabel.revalidate();
                            iconLabel.repaint();
                        });
                    }
                });
            } else if (note.hasItemIcon()) {
                plugin.getItemAsync(note.getItemId(), (itemImg) ->
                {
                    if (itemImg != null)
                    {
                        SwingUtilities.invokeLater(() ->
                        {
                            iconLabel.setIcon(new ImageIcon(itemImg));
                            iconLabel.revalidate();
                            iconLabel.repaint();
                        });
                    }
                });
            }
        }

        FlatTextField nameInput = createNameInput(note.getName(), flatTextFieldMouseAdapter);
        nameInput.setBackground(Helper.DARK_GREY_COLOR);
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
        if (note.hasIcon()) {
            gc = new GridBagConstraints();
            gc.gridx = 1;
            gc.gridy = 0;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 4, 0, 4); // Spacing
            nameWrapper.add(iconLabel, gc);
        }

        // Note title
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
        expandedContentPanel.setBackground(Helper.DARK_GREY_COLOR);
        expandedContentPanel.setLayout(new BorderLayout());
        expandedContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Uniform padding on all sides
        expandedContentPanel.setVisible(note.isMaximized());

        ContentEditorPanel contentEditorPanel = new ContentEditorPanel(note, plugin);
        JScrollPane contentScrollPane = new JScrollPane(contentEditorPanel);
        contentScrollPane.setPreferredSize(new Dimension(0, 400));
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        expandedContentPanel.add(contentScrollPane, BorderLayout.CENTER);
        add(expandedContentPanel, BorderLayout.CENTER);

        if (note.isNewNote()) {
            startRenaming(nameInput);
            note.setNewNote(false);
        }
    }


    private JPanel createNameActions(FlatTextField nameInput, MouseAdapter flatTextFieldMouseAdapter) {
        nameActions = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 40);
            }
        };
        nameActions.setBackground(Helper.DARK_GREY_COLOR);

        saveButton = new JLabel("Save");
        cancelButton = new JLabel("Cancel");

        saveButton.setForeground(Color.GRAY);
        cancelButton.setForeground(Color.RED);

        moreOptions = new JLabel(MORE_OPTIONS_ICON);
        moreOptions.setToolTipText("More options");
        moreOptions.setCursor(new Cursor(Cursor.HAND_CURSOR));

        expandNote = new JLabel(FULL_SCREEN_ICON);
        expandNote.setToolTipText("Expand note");
        expandNote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        expandNote.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                        //expand note
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                expandNote.setIcon(FULL_SCREEN_ICON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                expandNote.setIcon(FULL_SCREEN_ICON);
            }
        });

        GridBagConstraints gc = new GridBagConstraints();

        moreOptions.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JPopupMenu optionsMenu = new JPopupMenu();

                    JMenuItem setRemoveIcon = new JMenuItem(note.hasIcon() ? "Change Note Icon" : "Set Note Icon");
                    setRemoveIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.openNoteIconPickerDialog(note);
                        }
                    });

                    JMenuItem removeIcon = new JMenuItem("Remove Note Icon");
                    removeIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.removeNoteIcon(note, false);
                        }
                    });

                    if (note.hasIcon()) {
                        optionsMenu.add(removeIcon);
                    }

                    JMenuItem renameNote = new JMenuItem("Rename Note");
                    renameNote.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            startRenaming(nameInput);
                        }
                    });

                    JMenuItem deleteNote = new JMenuItem("Delete Note");
                    deleteNote.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            plugin.deleteNote(note, section);
                        }
                    });

                    optionsMenu.add(setRemoveIcon);
                    optionsMenu.add(renameNote);
                    optionsMenu.add(deleteNote);

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
        nameActions.add(moreOptions, gc);

        // Add Note button
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0.0;
        gc.insets = new Insets(0, 4, 0, 0); // 4px spacing to the left
        gc.anchor = GridBagConstraints.CENTER;
        nameActions.add(expandNote, gc);

        return nameActions;
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
                minMaxLabel.setIcon(note.isMaximized() ? MINIMIZE_ICON_HOVER : MAXIMIZE_ICON_HOVER);
                minMaxLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateMinMaxLabel();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    note.setMaximized(!note.isMaximized());
                    plugin.getDataManager().updateConfig();
                    updateMinMaxLabel();

                    // Toggle visibility of expandedContentPanel
                    expandedContentPanel.setVisible(note.isMaximized());

                    // Adjust layout to ensure no remnant space
                    revalidate();
                    repaint();
                }
            }
        });
    }

    private void updateMinMaxLabel() {
        if (note.isMaximized()) {
            minMaxLabel.setIcon(MINIMIZE_ICON);
            minMaxLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            minMaxLabel.setToolTipText("Click to collapse");
        } else {
            minMaxLabel.setIcon(MAXIMIZE_ICON);
            minMaxLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        nameActions.remove(expandNote);

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
                        note.setName(newName);
                        plugin.getDataManager().updateConfig();
                        plugin.redrawMainPanel();
                    }

                    endRenaming(nameInput, nameActions, expandNote, moreOptions, saveButton, cancelButton, initialName);
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
                    endRenaming(nameInput, nameActions, expandNote, moreOptions, saveButton, cancelButton, initialName);
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
