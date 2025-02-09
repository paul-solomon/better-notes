package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesSection;
import com.betterNotes.utility.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JPanel that displays a reorderable JList of BetterNotesSection objects,
 * each rendered as a SectionPanel.
 */
public class ReorderableSectionListPanel extends JPanel
{
    private JList<BetterNotesSection> reorderableList;
    private DefaultListModel<BetterNotesSection> listModel;
    private BetterNotesPlugin plugin;

    public ReorderableSectionListPanel(BetterNotesPlugin plugin)
    {
        this.plugin = plugin;
        setLayout(new BorderLayout());
        setBackground(Helper.DARKER_GREY_COLOR);

        // Load sections from the plugin
        listModel = new DefaultListModel<>();
        listModel.addAll(plugin.getSections());

        reorderableList = new JList<>(listModel)
        {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return super.getPreferredSize();
            }
        };
        reorderableList.setDragEnabled(true);
        reorderableList.setDropMode(DropMode.INSERT);
        reorderableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        reorderableList.setTransferHandler(new ReorderTransferHandler());

        // Custom renderer that respects preferred size
        reorderableList.setCellRenderer(new ListCellRenderer<BetterNotesSection>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends BetterNotesSection> list, BetterNotesSection value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(Helper.DARKER_GREY_COLOR);

                SectionPanel sectionPanel = new SectionPanel(plugin, value, null);
                sectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                // Ensure the list does not stretch the panel vertically
                sectionPanel.setMaximumSize(sectionPanel.getPreferredSize());

                wrapper.add(sectionPanel, BorderLayout.CENTER);

                // Spacer for consistency with MainPanel
                JPanel spacer = new JPanel();
                spacer.setPreferredSize(new Dimension(0, 10));
                spacer.setBackground(Helper.DARKER_GREY_COLOR);

                wrapper.add(spacer, BorderLayout.SOUTH);
                return wrapper;
            }
        });

        reorderableList.setFixedCellHeight(-1);  // Dynamic height
        reorderableList.setBackground(Helper.DARKER_GREY_COLOR);
        reorderableList.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(reorderableList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(scrollPane, BorderLayout.CENTER);

        JLabel debugLabel = new JLabel("debug");
        add(debugLabel, BorderLayout.SOUTH);
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

        plugin.getSections().clear();
        plugin.getSections().addAll(newOrder);
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

                saveOrder();
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
