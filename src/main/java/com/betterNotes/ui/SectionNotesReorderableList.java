package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SectionNotesReorderableList extends JPanel {
    private JList<BetterNotesNote> reorderableList;
    private DefaultListModel<BetterNotesNote> listModel;
    private BetterNotesPlugin plugin;
    private BetterNotesSection section;
    private JLabel dropPlaceholder;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public SectionNotesReorderableList(BetterNotesPlugin plugin, BetterNotesSection section) {
        this.plugin = plugin;
        this.section = section;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Create a model and populate it with the notes from this section.
        listModel = new DefaultListModel<>();
        for (BetterNotesNote note : section.getNotes()) {
            listModel.addElement(note);
        }

        // Create the JList and set up drag-and-drop.
        reorderableList = new JList<>(listModel);
        reorderableList.setDragEnabled(true);
        reorderableList.setDropMode(DropMode.INSERT);
        reorderableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        reorderableList.setTransferHandler(new ReorderTransferHandler());

        // Use a custom cell renderer to render each note.
        reorderableList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                BetterNotesNote note = (BetterNotesNote) value;
                // SectionNotePanel is assumed to be your custom component to display a note.
                SectionNotePanel notePanel = new SectionNotePanel(plugin, note, section, null);
                notePanel.setOpaque(true);
                notePanel.setBackground(isSelected ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                return notePanel;
            }
        });

        // Wrap the JList in a JScrollPane.
        JScrollPane scrollPane = new JScrollPane(reorderableList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.DARK_GRAY);
        // Set a minimum size so it does not collapse even if the model is empty.
        scrollPane.setMinimumSize(new Dimension(100, 100));

        // Use a CardLayout so we don’t actually remove the list from the layout.
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(scrollPane, "LIST");

        add(cardPanel, BorderLayout.CENTER);

        // Listen for changes in the list model and update the view.
        listModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                updateView();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateView();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                updateView();
            }
        });

        updateView();
    }

    /**
     * Updates which “card” is shown based on whether the list is empty.
     */
    private void updateView() {

        cardLayout.show(cardPanel, "LIST");
        // Debug output (optional)
        System.out.println("Section: " + section.getName() + " model size: " + listModel.getSize());
        revalidate();
        repaint();
    }

    /**
     * Saves the current order of notes back to the section and updates the config.
     */
    private void saveOrder() {
        List<BetterNotesNote> newOrder = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++) {
            newOrder.add(listModel.getElementAt(i));
        }
        section.setNotes(newOrder);
        plugin.getDataManager().updateConfig();
    }

    /**
     * Removes the given note from the source section.
     */
    public void removeNoteFromSourceSection(BetterNotesNote note, BetterNotesSection sourceSection) {
        sourceSection.getNotes().remove(note);
        plugin.getDataManager().updateConfig();
        updateView();
    }

    // --- Drag & Drop TransferHandler ---
    private class ReorderTransferHandler extends TransferHandler {
        private JList<BetterNotesNote> sourceList;
        private int[] draggedIndices;
        private BetterNotesSection sourceSection;

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            sourceList = (JList<BetterNotesNote>) c;
            draggedIndices = sourceList.getSelectedIndices();
            // Grab the section from the parent panel.
            sourceSection = ((SectionNotesReorderableList) SwingUtilities.getAncestorOfClass(
                    SectionNotesReorderableList.class, sourceList)).section;
            List<BetterNotesNote> selectedNotes = sourceList.getSelectedValuesList();
            return new BetterNotesNoteListTransferable(selectedNotes, sourceSection);
        }

        @Override
        public boolean canImport(TransferSupport info) {
            return info.isDataFlavorSupported(BetterNotesNoteListTransferable.NOTE_LIST_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport info) {
            if (!canImport(info)) {
                return false;
            }
            JList.DropLocation dropLocation = (JList.DropLocation) info.getDropLocation();
            int dropIndex = dropLocation.getIndex();
            JList<BetterNotesNote> targetList = (JList<BetterNotesNote>) info.getComponent();
            DefaultListModel<BetterNotesNote> targetModel = (DefaultListModel<BetterNotesNote>) targetList.getModel();

            try {
                BetterNotesNoteListTransferable transferable = (BetterNotesNoteListTransferable)
                        info.getTransferable().getTransferData(BetterNotesNoteListTransferable.NOTE_LIST_FLAVOR);
                List<BetterNotesNote> droppedItems = transferable.getData();
                BetterNotesSection sourceSection = transferable.getSourceSection();

                if (targetList == sourceList) {
                    // Reordering within the same list.
                    List<BetterNotesNote> removedItems = new ArrayList<>();
                    for (int i = draggedIndices.length - 1; i >= 0; i--) {
                        removedItems.add(0, targetModel.remove(draggedIndices[i]));
                    }
                    int removedAbove = 0;
                    for (int idx : draggedIndices) {
                        if (idx < dropIndex) {
                            removedAbove++;
                        }
                    }
                    dropIndex -= removedAbove;
                    dropIndex = Math.max(0, Math.min(dropIndex, targetModel.getSize()));
                    for (BetterNotesNote item : removedItems) {
                        targetModel.add(dropIndex++, item);
                    }
                    saveOrder();
                } else {
                    // Moving between different sections.
                    for (BetterNotesNote item : droppedItems) {
                        targetModel.add(dropIndex++, item);
                        section.getNotes().add(item);
                        removeNoteFromSourceSection(item, sourceSection);
                    }
                    saveOrder();
                }
                updateView();
                return true;
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    // --- Transferable Class ---
    public static class BetterNotesNoteListTransferable implements Transferable {
        public static final DataFlavor NOTE_LIST_FLAVOR = new DataFlavor(List.class, "List of BetterNotesNote");
        private final List<BetterNotesNote> data;
        private final BetterNotesSection sourceSection;

        public BetterNotesNoteListTransferable(List<BetterNotesNote> data, BetterNotesSection sourceSection) {
            this.data = new ArrayList<>(data);
            this.sourceSection = sourceSection;
        }

        public List<BetterNotesNote> getData() {
            return data;
        }

        public BetterNotesSection getSourceSection() {
            return sourceSection;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{NOTE_LIST_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return NOTE_LIST_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return this;
        }
    }
}
