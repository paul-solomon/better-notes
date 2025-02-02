package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesSection;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JPanel that displays a reorderable JList of BetterNotesSection objects,
 * each rendered as a simple JPanel with a title (section name).
 * Uses remove-then-insert + drop-index adjustment to avoid duplication or misplaced items.
 */
public class ReorderViewPanel extends JPanel
{
    private JList<BetterNotesSection> reorderableList;
    private DefaultListModel<BetterNotesSection> listModel;
    private BetterNotesPlugin plugin;

    public ReorderViewPanel(BetterNotesPlugin plugin)
    {
        this.plugin = plugin;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Load sections from the plugin
        listModel = new DefaultListModel<>();
        listModel.addAll(plugin.getSections());

        reorderableList = new JList<>(listModel);
        reorderableList.setDragEnabled(true);
        reorderableList.setDropMode(DropMode.INSERT);
        reorderableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        reorderableList.setTransferHandler(new ReorderTransferHandler());

        // Custom cell renderer
        reorderableList.setCellRenderer((list, value, index, isSelected, cellHasFocus) ->
                new ReorderableSectionPanel(plugin, value)
        );

        JScrollPane scrollPane = new JScrollPane(reorderableList);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Save the current order of sections back to the plugin and update config.
     */
    private void saveOrder()
    {
        List<BetterNotesSection> newOrder = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++)
        {
            newOrder.add(listModel.getElementAt(i));
        }

        // Update the plugin's section list
        plugin.getSections().clear();
        plugin.getSections().addAll(newOrder);

        // Persist changes
        plugin.getDataManager().updateConfig();
        System.out.println("Order saved!");
    }

    /**
     * TransferHandler for drag-and-drop reordering.
     */
    private class ReorderTransferHandler extends TransferHandler
    {
        private JList<BetterNotesSection> sourceList;
        private int[] draggedIndices;

        @Override
        public int getSourceActions(JComponent c)
        {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            sourceList = (JList<BetterNotesSection>) c;
            draggedIndices = sourceList.getSelectedIndices();
            return new BetterNotesSectionListTransferable(sourceList.getSelectedValuesList());
        }

        @Override
        public boolean canImport(TransferSupport info)
        {
            return info.isDataFlavorSupported(BetterNotesSectionListTransferable.SECTION_LIST_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport info)
        {
            if (!canImport(info)) return false;

            JList.DropLocation dropLocation = (JList.DropLocation) info.getDropLocation();
            int dropIndex = dropLocation.getIndex();
            JList<BetterNotesSection> targetList = (JList<BetterNotesSection>) info.getComponent();
            DefaultListModel<BetterNotesSection> model = (DefaultListModel<BetterNotesSection>) targetList.getModel();

            try
            {
                List<BetterNotesSection> droppedItems = (List<BetterNotesSection>)
                        info.getTransferable().getTransferData(BetterNotesSectionListTransferable.SECTION_LIST_FLAVOR);

                if (targetList == sourceList)
                {
                    List<BetterNotesSection> oldItems = new ArrayList<>();
                    for (int idx : draggedIndices) oldItems.add(model.getElementAt(idx));

                    for (int i = draggedIndices.length - 1; i >= 0; i--) model.remove(draggedIndices[i]);

                    int removedAbove = 0;
                    for (int idx : draggedIndices)
                        if (idx < dropIndex) removedAbove++;

                    dropIndex -= removedAbove;
                    dropIndex = Math.max(0, Math.min(dropIndex, model.size()));

                    for (BetterNotesSection item : oldItems)
                        model.add(dropIndex++, item);
                }
                else
                {
                    for (BetterNotesSection item : droppedItems)
                        model.add(dropIndex++, item);

                    DefaultListModel<BetterNotesSection> sourceModel =
                            (DefaultListModel<BetterNotesSection>) sourceList.getModel();
                    for (int i = draggedIndices.length - 1; i >= 0; i--)
                        sourceModel.remove(draggedIndices[i]);
                }

                saveOrder();  // ✅ Save changes after reorder
                return true;
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * A simple Transferable for BetterNotesSection lists.
     */
    public static class BetterNotesSectionListTransferable implements Transferable
    {
        public static final DataFlavor SECTION_LIST_FLAVOR =
                new DataFlavor(List.class, "List of BetterNotesSection");

        private final List<BetterNotesSection> data;

        public BetterNotesSectionListTransferable(List<BetterNotesSection> data)
        {
            this.data = new ArrayList<>(data);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]{ SECTION_LIST_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return SECTION_LIST_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return data;
        }
    }
}
