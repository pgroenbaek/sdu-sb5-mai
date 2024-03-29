/*
 * @(#)SelectionColorIcon.java  2.1  2007-05-03
 *
 * Copyright (c) 1996-2007 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw.draw.action;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.net.*;
import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import org.jhotdraw.geom.*;
import org.jhotdraw.samples.svg.Gradient;
/**
 * SelectionColorIcon draws a shape with the specified color for the selected
 * figures in the current drawing view.
 * If now figures are selcted, the specified color is taken from the DrawingEditor.
 * <p>
 * The behavior for choosing the drawn color matches with
 * {@link SelectionColorChooserAction }.
 * 
 * @author Werner Randelshofer
 * @version 2.1 2007-05-03 Added parameters for setting the color rect.
 * <br>2.0 2006-06-07 Reworked.
 * <br>1.0 25. November 2003  Created.
 */
public class SelectionGradientColorIcon extends javax.swing.ImageIcon {
    private DrawingEditor editor;
    private AttributeKey<Gradient> key;
    private int stop;
    private Shape colorShape;
    
    /** Creates a new instance.
     * @param editor The drawing editor.
     * @param key The key of the default attribute
     * @param imageLocation the icon image
     * @param colorShape The shape to be drawn with the color of the default
     * attribute.
     */
    public SelectionGradientColorIcon(
            DrawingEditor editor,
            int stop,
            AttributeKey<Gradient> key,
            URL imageLocation,
            Shape colorShape) {
        super(imageLocation);
        this.editor = editor;
        this.stop = stop;
        this.key = key;
        this.colorShape = colorShape;
    }
    public SelectionGradientColorIcon(
            DrawingEditor editor,
            int stop,
            AttributeKey<Gradient> key,
            Image image,
            Shape colorShape) {
        super(image);
        this.editor = editor;
        this.stop = stop;
        this.key = key;
        this.colorShape = colorShape;
    }
    
    @Override
    public void paintIcon(java.awt.Component c, java.awt.Graphics gr, int x, int y) {
        Graphics2D g = (Graphics2D) gr;
        super.paintIcon(c, g, x, y);
        Color color = null;
        DrawingView view = editor.getActiveView();
        Gradient gradient;
        if (view != null && view.getSelectedFigures().size() == 1) {
            gradient = key.get(view.getSelectedFigures().iterator().next());
        } else {
            gradient = key.get(editor.getDefaultAttributes());
        }
        
        if(gradient != null) {
            int length = gradient.getStopColors().length;
            if(length > stop) {
                color = gradient.getStopColors()[stop];
            }
            else {
                color = editor.getDefaultAttribute(FILL_COLOR);
            }
        }
        
        if (color != null) {
            g.setColor(color);
            g.translate(x, y);
            g.fill(colorShape);
            g.translate(-x, -y);
        }
    }
}