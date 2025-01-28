package com.betterNotes.ui;

import com.betterNotes.BetterNotesPlugin;
import com.betterNotes.entities.BetterNotesNote;
import com.betterNotes.utility.Helper;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.io.StringWriter;

public class ContentEditorPanel extends JPanel
{
    private final BetterNotesNote note;
    private final BetterNotesPlugin plugin;
    private final JTextPane contentTextPane = new JTextPane();

    private static final ImageIcon COLOR_PICKER_ICON;

    private static final ImageIcon TEXT_SIZE_ICON;
    private Timer saveTimer;

    static
    {
        final BufferedImage colorPickerIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/color_picker.png");
        COLOR_PICKER_ICON = new ImageIcon(ImageUtil.resizeImage(colorPickerIcon, 16, 16));

        final BufferedImage textSizeIcon = ImageUtil.loadImageResource(BetterNotesPlugin.class, "/text_size.png");
        TEXT_SIZE_ICON = new ImageIcon(ImageUtil.resizeImage(textSizeIcon, 18, 18));
    }


    public ContentEditorPanel(final BetterNotesNote note, final BetterNotesPlugin plugin)
    {
        this.note = note;
        this.plugin = plugin;

        setBackground(Helper.DARKER_GREY_COLOR);
        setLayout(new BorderLayout());

        setupContentTextPane();
        setupSaveDebounce();

        JScrollPane contentScrollPane = new JScrollPane(contentTextPane);
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(contentScrollPane, BorderLayout.CENTER);

        JPanel stylePanel = createStylePanel();
        add(stylePanel, BorderLayout.NORTH);
    }

    private JPanel createStylePanel()
    {
        JPanel stylePanel = new JPanel();
        stylePanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        stylePanel.setBackground(Helper.DARK_GREY_COLOR);
        stylePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

        JButton boldButton = new JButton("B");
        boldButton.setPreferredSize(new Dimension(24, 24));
        boldButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        boldButton.setToolTipText("Bold");
        boldButton.addActionListener(e -> toggleStyle(StyleConstants.Bold));

        JButton italicButton = new JButton("I");
        italicButton.setPreferredSize(new Dimension(24, 24));
        italicButton.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        italicButton.setToolTipText("Italic");
        italicButton.addActionListener(e -> toggleStyle(StyleConstants.Italic));

        // Button for size options
        JButton sizeButton = new JButton(TEXT_SIZE_ICON); // Replace with appropriate size icon
        sizeButton.setToolTipText("Text Size");
        sizeButton.setPreferredSize(new Dimension(24, 24));

        // Create a popup menu for size options
        JPopupMenu sizeMenu = new JPopupMenu();
        String[] sizeOptions = {"Huge", "Big", "Normal", "Small"};
        for (String size : sizeOptions)
        {
            JMenuItem sizeOption = new JMenuItem(size);
            sizeOption.addActionListener(e -> applySizeStyle(size));
            sizeMenu.add(sizeOption);
        }

        // Show the popup menu when the size button is clicked
        sizeButton.addActionListener(e -> sizeMenu.show(sizeButton, 0, sizeButton.getHeight()));

        JButton colorButton = new JButton(COLOR_PICKER_ICON);
        colorButton.setPreferredSize(new Dimension(24, 24));
        colorButton.setToolTipText("Pick color");
        colorButton.addActionListener(e -> changeTextColor());

        stylePanel.add(boldButton);
        stylePanel.add(italicButton);
        stylePanel.add(colorButton);
        stylePanel.add(sizeButton);

        return stylePanel;
    }

    private void toggleStyle(Object styleAttribute)
    {
        StyledDocument doc = contentTextPane.getStyledDocument();
        int start = contentTextPane.getSelectionStart();
        int end = contentTextPane.getSelectionEnd();

        if (start == end) // No text selected
        {
            return;
        }

        AttributeSet currentAttributes = doc.getCharacterElement(start).getAttributes();
        boolean isActive = false;

        if (styleAttribute == StyleConstants.Bold)
        {
            isActive = StyleConstants.isBold(currentAttributes);
        }
        else if (styleAttribute == StyleConstants.Italic)
        {
            isActive = StyleConstants.isItalic(currentAttributes);
        }

        SimpleAttributeSet newAttributes = new SimpleAttributeSet();
        if (styleAttribute == StyleConstants.Bold)
        {
            StyleConstants.setBold(newAttributes, !isActive);
        }
        else if (styleAttribute == StyleConstants.Italic)
        {
            StyleConstants.setItalic(newAttributes, !isActive);
        }

        doc.setCharacterAttributes(start, end - start, newAttributes, false);
    }

    private void changeTextColor()
    {
        // Open a color picker dialog
        plugin.openColorPicker("Choose a Display color", Color.WHITE,
                c ->
                {
                    Color selectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);

                        StyledDocument doc = contentTextPane.getStyledDocument();
                        int start = contentTextPane.getSelectionStart();
                        int end = contentTextPane.getSelectionEnd();

                        if (start == end) // No text selected
                        {
                            return;
                        }

                        SimpleAttributeSet newAttributes = new SimpleAttributeSet();
                        StyleConstants.setForeground(newAttributes, selectedColor);

                        doc.setCharacterAttributes(start, end - start, newAttributes, false);
                }
        );

    }

    private void applySizeStyle(String size)
    {
        StyledDocument doc = contentTextPane.getStyledDocument();
        int start = contentTextPane.getSelectionStart();
        int end = contentTextPane.getSelectionEnd();

        if (start == end) // No text selected
        {
            return;
        }

        SimpleAttributeSet newAttributes = new SimpleAttributeSet();

        switch (size)
        {
            case "Huge":
                StyleConstants.setFontSize(newAttributes, 20);
                StyleConstants.setBold(newAttributes, true);
                break;
            case "Big":
                StyleConstants.setFontSize(newAttributes, 18);
                StyleConstants.setBold(newAttributes, true);
                break;
            case "Normal":
                StyleConstants.setFontSize(newAttributes, 14);
                StyleConstants.setBold(newAttributes, false);
                break;
            case "Small":
                StyleConstants.setFontSize(newAttributes, 12);
                StyleConstants.setBold(newAttributes, false);
                break;
        }

        doc.setCharacterAttributes(start, end - start, newAttributes, false);
    }

    private void setupContentTextPane()
    {
        contentTextPane.setFont(FontManager.getRunescapeFont());
        contentTextPane.setBackground(Helper.DARK_GREY_COLOR);
        contentTextPane.setForeground(Color.WHITE);
        contentTextPane.setCaretColor(Color.WHITE);
        contentTextPane.setBorder(new EmptyBorder(8, 0, 8, 0));

        loadHtmlContent(note.getContent());

        contentTextPane.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                onTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                onTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                onTextChanged();
            }
        });

        // Define the custom paste action
        Action pastePlainTextAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // Get clipboard content
                    String clipboardText = (String) Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .getData(DataFlavor.stringFlavor);

                    // Insert as plain text
                    int caretPosition = contentTextPane.getCaretPosition();
                    contentTextPane.getDocument().insertString(caretPosition, clipboardText, null);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };

        // Bind both Ctrl+V and Cmd+V to the custom paste action
        contentTextPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), "paste-plain-text");
        contentTextPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "paste-plain-text");
        contentTextPane.getActionMap().put("paste-plain-text", pastePlainTextAction);
    }

    private void loadHtmlContent(String htmlContent)
    {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = new HTMLDocument();
        contentTextPane.setEditorKit(kit);
        contentTextPane.setDocument(doc);

        try
        {
            kit.read(new StringReader(htmlContent), doc, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getHtmlContent()
    {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) contentTextPane.getDocument();

        try
        {
            StringWriter writer = new StringWriter();
            kit.write(writer, doc, 0, doc.getLength());
            String fullContent = writer.toString();

            // Extract the content inside the <body> tags
            String bodyContent = fullContent.replaceAll("(?s).*<body.*?>(.*?)</body>.*", "$1");

            // Convert newlines back to <br> tags for storage
            bodyContent = bodyContent
                    .replaceAll("\r?\n", "<br>") // Convert newlines to <br>
                    .trim(); // Remove surrounding whitespace

            // Remove leading <br> tag if it exists
            if (bodyContent.startsWith("<br>"))
            {
                bodyContent = bodyContent.substring(4).trim();
            }

            return bodyContent;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private void setupSaveDebounce()
    {
        saveTimer = new Timer(500, e -> saveNoteContent());
        saveTimer.setRepeats(false);
    }

    private void onTextChanged()
    {
        if (saveTimer.isRunning())
        {
            saveTimer.restart();
        }
        else
        {
            saveTimer.start();
        }
    }

    private void saveNoteContent()
    {
        String htmlContent = getHtmlContent();
        note.setContent(htmlContent);
        plugin.getDataManager().updateConfigNoRedraw();
    }
}
