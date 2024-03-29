/*
 * @(#)SVGRect.java  2.1  2009-04-17
 *
 * Copyright (c) 1996-2009 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw.samples.svg.figures;

import dk.sdu.mmmi.featuretracer.lib.FeatureEntryPoint;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import org.jhotdraw.app.JHotDrawFeatures;
import org.jhotdraw.draw.*;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;
import org.jhotdraw.samples.svg.*;
import org.jhotdraw.geom.*;

/**
 * SVGRect.
 *
 * @author Werner Randelshofer
 * @version 2.1 2009-04-17 Method contains() takes now into account
 * whether the figure is filled.
 * <br>2.0 2007-04-14 Adapted for new AttributeKeys.TRANSFORM support.
 * <br>1.0 July 8, 2006 Created.
 */
public class SVGRectFigure extends SVGAttributedFigure implements SVGFigure {

    /** The variable acv is used for generating the locations of the control
     * points for the rounded rectangle using path.curveTo. */
    private static final double acv;


    static {
        double angle = Math.PI / 4.0;
        double a = 1.0 - Math.cos(angle);
        double b = Math.tan(angle);
        double c = Math.sqrt(1.0 + b * b) - 1 + a;
        double cv = 4.0 / 3.0 * a * b / c;
        acv = (1.0 - cv);
    }
    /**
     */
    private RoundRectangle2D.Double roundrect;
    /**
     * This is used to perform faster drawing.
     */
    private transient Shape cachedTransformedShape;
    /**
     * This is used to perform faster hit testing.
     */
    private transient Shape cachedHitShape;

    /** Creates a new instance. */
    public SVGRectFigure() {
        this(0, 0, 0, 0);
    }

    public SVGRectFigure(double x, double y, double width, double height) {
        this(x, y, width, height, 0, 0);
    }

    @FeatureEntryPoint(JHotDrawFeatures.RECTANGLE_TOOL)
    public SVGRectFigure(double x, double y, double width, double height, double rx, double ry) {
        roundrect = new RoundRectangle2D.Double(x, y, width, height, rx, ry);
        SVGAttributeKeys.setDefaults(this);
    }

    // DRAWING
    protected void drawFill(Graphics2D g) {
        if (getArcHeight() == 0d && getArcWidth() == 0d) {
            g.fill(roundrect.getBounds2D());
        } else {
            g.fill(roundrect);
        }
    }

    @FeatureEntryPoint(JHotDrawFeatures.RECTANGLE_TOOL)
    protected void drawStroke(Graphics2D g) {
        if (roundrect.archeight == 0 && roundrect.arcwidth == 0) {
            g.draw(roundrect.getBounds2D());
        } else {
            // We have to generate the path for the round rectangle manually,
            // because the path of a Java RoundRectangle is drawn counter clockwise
            // whereas an SVG rect needs to be drawn clockwise.
            GeneralPath p = new GeneralPath();
            double aw = roundrect.arcwidth / 2d;
            double ah = roundrect.archeight / 2d;
            p.moveTo((float) (roundrect.x + aw), (float) roundrect.y);
            p.lineTo((float) (roundrect.x + roundrect.width - aw), (float) roundrect.y);
            p.curveTo((float) (roundrect.x + roundrect.width - aw * acv), (float) roundrect.y, //
                    (float) (roundrect.x + roundrect.width), (float)(roundrect.y + ah * acv), //
                    (float) (roundrect.x + roundrect.width), (float) (roundrect.y + ah));
            p.lineTo((float) (roundrect.x + roundrect.width), (float) (roundrect.y + roundrect.height - ah));
            p.curveTo(
                    (float) (roundrect.x + roundrect.width), (float) (roundrect.y + roundrect.height - ah * acv),//
                    (float) (roundrect.x + roundrect.width - aw * acv), (float) (roundrect.y + roundrect.height),//
                    (float) (roundrect.x + roundrect.width - aw), (float) (roundrect.y + roundrect.height));
            p.lineTo((float) (roundrect.x + aw), (float) (roundrect.y + roundrect.height));
            p.curveTo((float) (roundrect.x + aw*acv), (float) (roundrect.y + roundrect.height),//
                    (float) (roundrect.x), (float) (roundrect.y + roundrect.height - ah*acv),//
                   (float) roundrect.x, (float) (roundrect.y + roundrect.height - ah));
            p.lineTo((float) roundrect.x, (float) (roundrect.y + ah));
            p.curveTo((float) (roundrect.x), (float) (roundrect.y + ah*acv),//
                    (float) (roundrect.x + aw*acv), (float)(roundrect.y),//
                    (float)(roundrect.x + aw), (float)(roundrect.y));
            p.closePath();
            g.draw(p);
        }
    }

    // SHAPE AND BOUNDS
    public double getX() {
        return roundrect.x;
    }

    public double getY() {
        return roundrect.y;
    }

    public double getWidth() {
        return roundrect.width;
    }

    public double getHeight() {
        return roundrect.height;
    }

    public double getArcWidth() {
        return roundrect.arcwidth / 2d;
    }

    public double getArcHeight() {
        return roundrect.archeight / 2d;
    }

    public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) roundrect.getBounds2D();
    }

    @Override
    public Rectangle2D.Double getDrawingArea() {
        Rectangle2D rx = getTransformedShape().getBounds2D();
        Rectangle2D.Double r = (rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
        if (TRANSFORM.get(this) == null) {
            double g = SVGAttributeKeys.getPerpendicularHitGrowth(this) * 2d + 1d;
            Geom.grow(r, g, g);
        } else {
            double strokeTotalWidth = AttributeKeys.getStrokeTotalWidth(this);
            double width = strokeTotalWidth / 2d;
            if (STROKE_JOIN.get(this) == BasicStroke.JOIN_MITER) {
                width *= STROKE_MITER_LIMIT.get(this);
            }
            if (STROKE_CAP.get(this) != BasicStroke.CAP_BUTT) {
                width += strokeTotalWidth * 2;
            }
            width++;
            Geom.grow(r, width, width);
        }
        return r;
    }

    /**
     * Checks if a Point2D.Double is inside the figure.
     */
    public boolean contains(Point2D.Double p) {
        return getHitShape().contains(p);
    }

    @Override
    public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
        invalidateTransformedShape();
        roundrect.x = Math.min(anchor.x, lead.x);
        roundrect.y = Math.min(anchor.y, lead.y);
        roundrect.width = Math.max(0.1, Math.abs(lead.x - anchor.x));
        roundrect.height = Math.max(0.1, Math.abs(lead.y - anchor.y));
        invalidate();
    }

    private void invalidateTransformedShape() {
        cachedTransformedShape = null;
        cachedHitShape = null;
    }

    private Shape getTransformedShape() {
        if (cachedTransformedShape == null) {
            if (getArcHeight() == 0 || getArcWidth() == 0) {
                cachedTransformedShape = roundrect.getBounds2D();
            } else {
                cachedTransformedShape = (Shape) roundrect.clone();
            }
            if (TRANSFORM.get(this) != null) {
                cachedTransformedShape = TRANSFORM.get(this).createTransformedShape(cachedTransformedShape);
            }
        }
        return cachedTransformedShape;
    }

    private Shape getHitShape() {
        if (cachedHitShape == null) {
            if (FILL_COLOR.get(this) != null || FILL_GRADIENT.get(this) != null) {
                cachedHitShape = new GrowStroke(
                        (float) SVGAttributeKeys.getStrokeTotalWidth(this) / 2f,
                        (float) SVGAttributeKeys.getStrokeTotalMiterLimit(this)).createStrokedShape(getTransformedShape());
            } else {
                cachedHitShape = SVGAttributeKeys.getHitStroke(this).createStrokedShape(getTransformedShape());
            }
        }
        return cachedHitShape;
    }

    /**
     * Transforms the figure.
     * @param tx The transformation.
     */
    public void transform(AffineTransform tx) {
        invalidateTransformedShape();
        if (TRANSFORM.get(this) != null ||
                //              (tx.getType() & (AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_MASK_SCALE)) != tx.getType()) {
                (tx.getType() & (AffineTransform.TYPE_TRANSLATION)) != tx.getType()) {
            if (TRANSFORM.get(this) == null) {
                TRANSFORM.basicSet(this, (AffineTransform) tx.clone());
            } else {
                AffineTransform t = TRANSFORM.getClone(this);
                t.preConcatenate(tx);
                TRANSFORM.basicSet(this, t);
            }
        } else {
            Point2D.Double anchor = getStartPoint();
            Point2D.Double lead = getEndPoint();
            setBounds(
                    (Point2D.Double) tx.transform(anchor, anchor),
                    (Point2D.Double) tx.transform(lead, lead));
            if (FILL_GRADIENT.get(this) != null &&
                    !FILL_GRADIENT.get(this).isRelativeToFigureBounds()) {
                Gradient g = FILL_GRADIENT.getClone(this);
                g.transform(tx);
                FILL_GRADIENT.basicSet(this, g);
            }
            if (STROKE_GRADIENT.get(this) != null &&
                    !STROKE_GRADIENT.get(this).isRelativeToFigureBounds()) {
                Gradient g = STROKE_GRADIENT.getClone(this);
                g.transform(tx);
                STROKE_GRADIENT.basicSet(this, g);
            }
        }
    }
    // ATTRIBUTES

    public void setArc(double w, double h) {
        roundrect.arcwidth = Math.max(0d, Math.min(roundrect.width, w * 2d));
        roundrect.archeight = Math.max(0d, Math.min(roundrect.height, h * 2d));
    }

    public void setArc(Dimension2DDouble arc) {
        roundrect.arcwidth = Math.max(0d, Math.min(roundrect.width, arc.width * 2d));
        roundrect.archeight = Math.max(0d, Math.min(roundrect.height, arc.height * 2d));
    }

    public Dimension2DDouble getArc() {
        return new Dimension2DDouble(
                roundrect.arcwidth / 2d,
                roundrect.archeight / 2d);
    }

    public void restoreTransformTo(Object geometry) {
        invalidateTransformedShape();
        Object[] restoreData = (Object[]) geometry;
        roundrect = (RoundRectangle2D.Double) ((RoundRectangle2D.Double) restoreData[0]).clone();
        TRANSFORM.basicSetClone(this, (AffineTransform) restoreData[1]);
        FILL_GRADIENT.basicSetClone(this, (Gradient) restoreData[2]);
        STROKE_GRADIENT.basicSetClone(this, (Gradient) restoreData[3]);
    }

    public Object getTransformRestoreData() {
        return new Object[]{
                    roundrect.clone(),
                    TRANSFORM.getClone(this),
                    FILL_GRADIENT.getClone(this),
                    STROKE_GRADIENT.getClone(this),};
    }

    // EDITING
    @Override
    public Collection<Handle> createHandles(int detailLevel) {
        LinkedList<Handle> handles = new LinkedList<Handle>();
        switch (detailLevel % 2) {
            case -1: // Mouse hover handles
                handles.add(new BoundsOutlineHandle(this, false, true));
                break;
            case 0:
                ResizeHandleKit.addResizeHandles(this, handles);
                handles.add(new SVGRectRadiusHandle(this));
                handles.add(new LinkHandle(this));
                GradientHandleKit.addGradientHandles(this, handles);
                break;
            case 1:
                TransformHandleKit.addTransformHandles(this, handles);
                break;
            default:
                break;
        }
        return handles;
    }
    // CONNECTING

    public boolean canConnect() {
        return false; // SVG does not support connecting
    }

    public Connector findConnector(Point2D.Double p, ConnectionFigure prototype) {
        return null; // SVG does not support connectors
    }

    public Connector findCompatibleConnector(Connector c, boolean isStartConnector) {
        return null; // SVG does not support connectors
    }

    // COMPOSITE FIGURES
    // CLONING
    public SVGRectFigure clone() {
        SVGRectFigure that = (SVGRectFigure) super.clone();
        that.roundrect = (RoundRectangle2D.Double) this.roundrect.clone();
        that.cachedTransformedShape = null;
        that.cachedHitShape = null;
        return that;
    }

    public boolean isEmpty() {
        Rectangle2D.Double b = getBounds();
        return b.width <= 0 || b.height <= 0;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateTransformedShape();
    }
}
