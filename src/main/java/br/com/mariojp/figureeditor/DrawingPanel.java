package br.com.mariojp.figureeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

class DrawingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SIZE = 60;
    private final List<Shape> shapes = new ArrayList<>();
    private Point startDrag = null;
    private Point currentDrag = null;

    DrawingPanel() {

        setBackground(Color.WHITE);
        setOpaque(true);
        setDoubleBuffered(true);

        var mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startDrag = e.getPoint();
                currentDrag = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (startDrag != null) {
                    currentDrag = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (startDrag != null) {
                    // Calcula o tamanho baseado na distância do arraste
                    int width = Math.abs(e.getX() - startDrag.x);
                    int height = Math.abs(e.getY() - startDrag.y);

                    // Se não arrastou (clique simples), usa tamanho padrão
                    if (width < 5 && height < 5) {
                        width = height = DEFAULT_SIZE;
                    } else {
                        // Garante tamanho mínimo
                        width = Math.max(width, 10);
                        height = Math.max(height, 10);
                    }

                    // Posição da figura (canto superior esquerdo)
                    int x = Math.min(startDrag.x, e.getX());
                    int y = Math.min(startDrag.y, e.getY());

                    Shape s = new Ellipse2D.Double(x, y, width, height);
                    shapes.add(s);

                    startDrag = null;
                    currentDrag = null;
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

    }

    void clear() {
        shapes.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenha as figuras já criadas
        for (Shape s : shapes) {
            g2.setColor(new Color(30,144,255));
            g2.fill(s);
            g2.setColor(new Color(0,0,0,70));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(s);
        }

        // Desenha a pré-visualização durante o arraste
        if (startDrag != null && currentDrag != null) {
            int width = Math.abs(currentDrag.x - startDrag.x);
            int height = Math.abs(currentDrag.y - startDrag.y);
            int x = Math.min(startDrag.x, currentDrag.x);
            int y = Math.min(startDrag.y, currentDrag.y);

            // Pré-visualização com linha tracejada
            g2.setColor(new Color(30,144,255,100));
            g2.fill(new Ellipse2D.Double(x, y, width, height));
            g2.setColor(new Color(0,0,0,150));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
            g2.draw(new Ellipse2D.Double(x, y, width, height));
        }

        g2.dispose();
    }

}