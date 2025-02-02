package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.entities.BetterNotesSection;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A JPanel that displays a reorderable JList of BetterNotesNote objects,
 * allowing notes to be moved between different sections.
 */
public class SectionNotesReorderableList extends JPanel {
    private JList<BetterNotesNote> reorderableList;
    private DefaultListModel<BetterNotesNote> listModel;
    private BetterNotesPlugin plugin;
    private BetterNotesSection section;

    public SectionNotesReorderableList(BetterNotesPlugin plugin, BetterNotesSection section) {
        this.plugin = plugin;
        this.section = section;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        listModel = new DefaultListModel<>();
        listModel.addAll(section.getNotes());

        reorderableList = new JList<>(listModel);
        reorderableList.setDragEnabled(true);
        reorderableList.setDropMode(DropMode.INSERT);
        reorderableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        reorderableList.setTransferHandler(new ReorderTransferHandler());

        reorderableList.setCellRenderer((list, value, index, isSelected, cellHasFocus) ->
                new ReorderableSectionNotePanel(plugin, value)
        );

        JScrollPane scrollPane = new JScrollPane(reorderableList);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void saveOrder() {
        List<BetterNotesNote> newOrder = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++) {
            newOrder.add(listModel.getElementAt(i));
        }
        section.setNotes(newOrder);
        plugin.getDataManager().updateConfig();
    }

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
            sourceSection = ((SectionNotesReorderableList) SwingUtilities.getAncestorOfClass(SectionNotesReorderableList.class, sourceList)).section;
            return new BetterNotesNoteListTransferable(sourceList.getSelectedValuesList(), sourceSection);
        }

        @Override
        public boolean canImport(TransferSupport info) {
            return info.isDataFlavorSupported(BetterNotesNoteListTransferable.NOTE_LIST_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport info) {
            if (!canImport(info)) return false;

            JList.DropLocation dropLocation = (JList.DropLocation) info.getDropLocation();
            int dropIndex = dropLocation.getIndex();
            JList<BetterNotesNote> targetList = (JList<BetterNotesNote>) info.getComponent();
            DefaultListModel<BetterNotesNote> model = (DefaultListModel<BetterNotesNote>) targetList.getModel();

            try {
                BetterNotesNoteListTransferable transferable = (BetterNotesNoteListTransferable) info.getTransferable().getTransferData(BetterNotesNoteListTransferable.NOTE_LIST_FLAVOR);
                List<BetterNotesNote> droppedItems = transferable.getData();
                BetterNotesSection sourceSection = transferable.getSourceSection();

                if (targetList == sourceList) {
                    List<BetterNotesNote> oldItems = new ArrayList<>();
                    for (int idx : draggedIndices) oldItems.add(model.getElementAt(idx));

                    for (int i = draggedIndices.length - 1; i >= 0; i--) model.remove(draggedIndices[i]);

                    int removedAbove = 0;
                    for (int idx : draggedIndices)
                        if (idx < dropIndex) removedAbove++;

                    dropIndex -= removedAbove;
                    dropIndex = Math.max(0, Math.min(dropIndex, model.size()));

                    for (BetterNotesNote item : oldItems)
                        model.add(dropIndex++, item);

                    saveOrder();
                } else {
                    for (BetterNotesNote item : droppedItems) {
                        model.add(dropIndex++, item);
                        section.getNotes().add(item);
                        removeNoteFromSourceSection(item, sourceSection);
                    }
                    saveOrder();
                }
                return true;
            } catch (UnsupportedFlavorException | java.io.IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void removeNoteFromSourceSection(BetterNotesNote note, BetterNotesSection sourceSection) {
        sourceSection.getNotes().remove(note);
        plugin.getDataManager().updateConfig();
    }

    public static class BetterNotesNoteListTransferable implements Transferable {
        public static final DataFlavor NOTE_LIST_FLAVOR =
                new DataFlavor(List.class, "List of BetterNotesNote");

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
            return new DataFlavor[]{ NOTE_LIST_FLAVOR };
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
