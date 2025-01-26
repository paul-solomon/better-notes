package com.betterNotes.utility;

import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Helper
{
    public static final String CONFIG_GROUP = "betternotes";

    public static final Color DARK_GREY_COLOR = ColorScheme.DARK_GRAY_COLOR;
    public static final Color DARKER_GREY_COLOR = ColorScheme.DARKER_GRAY_COLOR;

    public static boolean checkClick(MouseEvent event)
    {
        if (event.getButton() == MouseEvent.BUTTON1 && event.getSource() instanceof JComponent)
        {
            Point point = event.getPoint();
            Dimension size = ((JComponent)event.getSource()).getSize();
            return point.getX() < 0 || point.getX() > size.getWidth() || point.getY() < 0 || point.getY() > size.getHeight();
        }
        return true;
    }
}