package br.com.mariojp.figureeditor;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("Figure Editor — Clique para inserir, arraste para definir tamanho");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            DrawingPanel drawingPanel = new DrawingPanel();

            // Criar toolbar
            JToolBar toolbar = createToolbar(drawingPanel);

            frame.setLayout(new BorderLayout());
            frame.add(toolbar, BorderLayout.NORTH);
            frame.add(drawingPanel, BorderLayout.CENTER);

            // Status bar
            JLabel statusBar = new JLabel("Clique para inserir figura | Arraste para definir tamanho | Shift+clique para selecionar");
            statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            frame.add(statusBar, BorderLayout.SOUTH);

            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JToolBar createToolbar(DrawingPanel drawingPanel) {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Botão de cor
        JButton colorButton = new JButton("Escolher Cor");
        colorButton.setBackground(drawingPanel.getCurrentColor());
        colorButton.setOpaque(true);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Escolha uma cor", drawingPanel.getCurrentColor());
            if (newColor != null) {
                drawingPanel.setCurrentColor(newColor);
                colorButton.setBackground(newColor);
            }
        });

        // Botão limpar
        JButton clearButton = new JButton("Limpar Tudo");
        clearButton.addActionListener(e -> drawingPanel.clear());

        // Botões de forma
        JButton circleButton = new JButton("Círculo");
        circleButton.addActionListener(e -> drawingPanel.setShapeType(ShapeType.CIRCLE));

        JButton rectangleButton = new JButton("Retângulo");
        rectangleButton.addActionListener(e -> drawingPanel.setShapeType(ShapeType.RECTANGLE));

        // Botões de undo/redo
        JButton undoButton = new JButton("Desfazer");
        undoButton.addActionListener(e -> drawingPanel.undo());

        JButton redoButton = new JButton("Refazer");
        redoButton.addActionListener(e -> drawingPanel.redo());

        // Botão exportar
        JButton exportButton = new JButton("Exportar PNG");
        exportButton.addActionListener(e -> drawingPanel.exportToPNG());

        toolbar.add(colorButton);
        toolbar.addSeparator();
        toolbar.add(circleButton);
        toolbar.add(rectangleButton);
        toolbar.addSeparator();
        toolbar.add(clearButton);
        toolbar.addSeparator();
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.addSeparator();
        toolbar.add(exportButton);

        return toolbar;
    }
}