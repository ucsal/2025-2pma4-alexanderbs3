package br.com.mariojp.figureeditor;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class DrawingPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SIZE = 60;
    private static final int SNAP_DISTANCE = 10;

    private final List<FigureShape> shapes = new ArrayList<>();
    private final CommandManager commandManager = new CommandManager();

    private Point startDrag = null;
    private Point endDrag = null;
    private boolean isDragging = false;
    private boolean isSelecting = false;

    private Color currentColor = new Color(30, 144, 255);
    private ShapeType currentShapeType = ShapeType.CIRCLE;

    private FigureShape selectedShape = null;
    private Point lastMousePos = null;

    DrawingPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);
        setDoubleBuffered(true);
        setFocusable(true);

        setupMouseListeners();
        setupKeyListeners();
    }

    private void setupMouseListeners() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                startDrag = e.getPoint();
                lastMousePos = e.getPoint();

                // Verificar se está com Shift pressionado para seleção
                if (e.isShiftDown()) {
                    isSelecting = true;
                    selectShapeAt(e.getPoint());
                } else {
                    isSelecting = false;
                    // Se há uma figura selecionada e clicamos nela, preparar para mover
                    if (selectedShape != null && selectedShape.contains(e.getPoint())) {
                        // Preparar para movimento
                    } else {
                        // Deselecionar figura atual
                        if (selectedShape != null) {
                            selectedShape.setSelected(false);
                            selectedShape = null;
                        }
                        isDragging = true;
                    }
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSelecting && selectedShape != null) {
                    // Mover figura selecionada
                    double dx = e.getX() - lastMousePos.x;
                    double dy = e.getY() - lastMousePos.y;

                    // Aplicar snap magnético
                    Point snapped = applySnap(new Point(e.getX(), e.getY()));
                    dx = snapped.x - lastMousePos.x;
                    dy = snapped.y - lastMousePos.y;

                    selectedShape.translate(dx, dy);
                    lastMousePos = snapped;
                } else if (isDragging) {
                    endDrag = e.getPoint();
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isSelecting && selectedShape != null && lastMousePos != null) {
                    // Finalizar movimento
                    double dx = e.getX() - startDrag.x;
                    double dy = e.getY() - startDrag.y;

                    if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
                        // Criar comando de movimento
                        Command moveCommand = new MoveShapeCommand(selectedShape, dx, dy);
                        // Note: o movimento já foi aplicado durante o drag, então fazemos undo e execute
                        moveCommand.undo();
                        commandManager.executeCommand(moveCommand);
                    }
                } else if (isDragging) {
                    if (endDrag != null &&
                            (Math.abs(endDrag.x - startDrag.x) > 5 || Math.abs(endDrag.y - startDrag.y) > 5)) {
                        // Criar figura com tamanho definido pelo arraste
                        createShapeFromDrag();
                    } else if (startDrag != null) {
                        // Clique simples - criar figura com tamanho padrão
                        createDefaultShape(startDrag);
                    }

                    isDragging = false;
                    endDrag = null;
                }

                startDrag = null;
                lastMousePos = null;
                repaint();
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedShape != null) {
                    removeShape(selectedShape);
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    undo();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    redo();
                }
            }
        });
    }

    private void selectShapeAt(Point point) {
        // Deselecionar figura anterior
        if (selectedShape != null) {
            selectedShape.setSelected(false);
        }

        // Buscar figura no ponto (do mais recente para o mais antigo)
        for (int i = shapes.size() - 1; i >= 0; i--) {
            FigureShape shape = shapes.get(i);
            if (shape.contains(point)) {
                selectedShape = shape;
                shape.setSelected(true);
                break;
            }
        }

        if (selectedShape == null) {
            // Nenhuma figura encontrada no ponto
        }
    }

    private Point applySnap(Point point) {
        Point snapped = new Point(point);

        // Snap para bordas de outras figuras
        for (FigureShape shape : shapes) {
            if (shape == selectedShape) continue;

            Rectangle2D bounds = shape.getBounds();

            // Snap horizontal
            if (Math.abs(point.x - bounds.getX()) < SNAP_DISTANCE) {
                snapped.x = (int) bounds.getX();
            } else if (Math.abs(point.x - (bounds.getX() + bounds.getWidth())) < SNAP_DISTANCE) {
                snapped.x = (int) (bounds.getX() + bounds.getWidth());
            }

            // Snap vertical
            if (Math.abs(point.y - bounds.getY()) < SNAP_DISTANCE) {
                snapped.y = (int) bounds.getY();
            } else if (Math.abs(point.y - (bounds.getY() + bounds.getHeight())) < SNAP_DISTANCE) {
                snapped.y = (int) (bounds.getY() + bounds.getHeight());
            }
        }

        return snapped;
    }

    private void createShapeFromDrag() {
        int x = Math.min(startDrag.x, endDrag.x);
        int y = Math.min(startDrag.y, endDrag.y);
        int width = Math.abs(endDrag.x - startDrag.x);
        int height = Math.abs(endDrag.y - startDrag.y);

        // Tamanho mínimo
        width = Math.max(width, 10);
        height = Math.max(height, 10);

        Shape shape = createShape(x, y, width, height);
        FigureShape figureShape = new FigureShape(shape, currentColor, currentShapeType);

        Command addCommand = new AddShapeCommand(this, figureShape);
        commandManager.executeCommand(addCommand);
    }

    private void createDefaultShape(Point point) {
        Shape shape = createShape(point.x - DEFAULT_SIZE/2, point.y - DEFAULT_SIZE/2,
                DEFAULT_SIZE, DEFAULT_SIZE);
        FigureShape figureShape = new FigureShape(shape, currentColor, currentShapeType);

        Command addCommand = new AddShapeCommand(this, figureShape);
        commandManager.executeCommand(addCommand);
    }

    private Shape createShape(int x, int y, int width, int height) {
        return switch (currentShapeType) {
            case CIRCLE -> new Ellipse2D.Double(x, y, width, height);
            case RECTANGLE -> new Rectangle2D.Double(x, y, width, height);
        };
    }

    public void addShapeDirectly(FigureShape shape) {
        shapes.add(shape);
        repaint();
    }

    public void removeShapeDirectly(FigureShape shape) {
        shapes.remove(shape);
        if (selectedShape == shape) {
            selectedShape = null;
        }
        repaint();
    }

    private void removeShape(FigureShape shape) {
        Command removeCommand = new Command() {
            @Override
            public void execute() {
                removeShapeDirectly(shape);
            }

            @Override
            public void undo() {
                addShapeDirectly(shape);
            }
        };
        commandManager.executeCommand(removeCommand);
    }

    public void clear() {
        if (!shapes.isEmpty()) {
            Command clearCommand = new ClearCommand(this);
            commandManager.executeCommand(clearCommand);
        }
    }

    public void clearDirectly() {
        shapes.clear();
        selectedShape = null;
        repaint();
    }

    public void undo() {
        commandManager.undo();
        repaint();
    }

    public void redo() {
        commandManager.redo();
        repaint();
    }

    public void exportToPNG() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("drawing.png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();

                // Temporariamente remover seleção para exportar
                FigureShape tempSelected = selectedShape;
                if (selectedShape != null) {
                    selectedShape.setSelected(false);
                }

                paint(g2);
                g2.dispose();

                // Restaurar seleção
                if (tempSelected != null) {
                    tempSelected.setSelected(true);
                }

                ImageIO.write(image, "PNG", file);
                JOptionPane.showMessageDialog(this, "Imagem exportada com sucesso!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar: " + ex.getMessage());
            }
        }
    }

    // Getters e setters
    public Color getCurrentColor() { return currentColor; }
    public void setCurrentColor(Color color) { this.currentColor = color; }
    public void setShapeType(ShapeType type) { this.currentShapeType = type; }
    public List<FigureShape> getShapes() { return new ArrayList<>(shapes); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenhar figuras
        for (FigureShape figureShape : shapes) {
            g2.setColor(figureShape.getColor());
            g2.fill(figureShape.getShape());

            // Borda
            g2.setColor(new Color(0, 0, 0, 70));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(figureShape.getShape());

            // Destacar figura selecionada
            if (figureShape.isSelected()) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
                Rectangle2D bounds = figureShape.getBounds();
                g2.draw(new Rectangle2D.Double(bounds.getX() - 2, bounds.getY() - 2,
                        bounds.getWidth() + 4, bounds.getHeight() + 4));
            }
        }

        // Desenhar preview durante drag
        if (isDragging && startDrag != null && endDrag != null) {
            int x = Math.min(startDrag.x, endDrag.x);
            int y = Math.min(startDrag.y, endDrag.y);
            int width = Math.abs(endDrag.x - startDrag.x);
            int height = Math.abs(endDrag.y - startDrag.y);

            g2.setColor(new Color(currentColor.getRed(), currentColor.getGreen(),
                    currentColor.getBlue(), 100));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{5}, 0));

            Shape previewShape = createShape(x, y, Math.max(width, 10), Math.max(height, 10));
            g2.draw(previewShape);
        }

        g2.dispose();
    }
}