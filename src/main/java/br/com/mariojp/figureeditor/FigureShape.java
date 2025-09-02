package br.com.mariojp.figureeditor;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class FigureShape {
    private Shape shape;
    private Color color;
    private boolean selected;
    private ShapeType type;
    private Rectangle2D bounds;

    public FigureShape(Shape shape, Color color, ShapeType type) {
        this.shape = shape;
        this.color = color;
        this.selected = false;
        this.type = type;
        this.bounds = shape.getBounds2D();
    }

    public Shape getShape() { return shape; }
    public Color getColor() { return color; }
    public boolean isSelected() { return selected; }
    public ShapeType getType() { return type; }
    public Rectangle2D getBounds() { return bounds; }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean contains(Point point) {
        return shape.contains(point);
    }

    public void translate(double dx, double dy) {
        if (type == ShapeType.CIRCLE && shape instanceof Ellipse2D) {
            Ellipse2D ellipse = (Ellipse2D) shape;
            double newX = ellipse.getX() + dx;
            double newY = ellipse.getY() + dy;
            shape = new Ellipse2D.Double(newX, newY, ellipse.getWidth(), ellipse.getHeight());
        } else if (type == ShapeType.RECTANGLE && shape instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D) shape;
            double newX = rect.getX() + dx;
            double newY = rect.getY() + dy;
            shape = new Rectangle2D.Double(newX, newY, rect.getWidth(), rect.getHeight());
        }
        bounds = shape.getBounds2D();
    }

    public FigureShape copy() {
        Shape newShape;
        if (type == ShapeType.CIRCLE && shape instanceof Ellipse2D) {
            Ellipse2D ellipse = (Ellipse2D) shape;
            newShape = new Ellipse2D.Double(ellipse.getX(), ellipse.getY(),
                    ellipse.getWidth(), ellipse.getHeight());
        } else if (type == ShapeType.RECTANGLE && shape instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D) shape;
            newShape = new Rectangle2D.Double(rect.getX(), rect.getY(),
                    rect.getWidth(), rect.getHeight());
        } else {
            return null;
        }

        FigureShape copy = new FigureShape(newShape, new Color(color.getRGB()), type);
        copy.selected = this.selected;
        return copy;
    }
}