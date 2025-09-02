package br.com.mariojp.figureeditor;

import java.util.List;
import java.util.ArrayList;

// Interface Command
interface Command {
    void execute();
    void undo();
}

// Command para adicionar figura
class AddShapeCommand implements Command {
    private DrawingPanel panel;
    private FigureShape shape;

    public AddShapeCommand(DrawingPanel panel, FigureShape shape) {
        this.panel = panel;
        this.shape = shape;
    }

    @Override
    public void execute() {
        panel.addShapeDirectly(shape);
    }

    @Override
    public void undo() {
        panel.removeShapeDirectly(shape);
    }
}

// Command para mover figura
class MoveShapeCommand implements Command {
    private FigureShape shape;
    private double dx, dy;

    public MoveShapeCommand(FigureShape shape, double dx, double dy) {
        this.shape = shape;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        shape.translate(dx, dy);
    }

    @Override
    public void undo() {
        shape.translate(-dx, -dy);
    }
}

// Command para limpar tela
class ClearCommand implements Command {
    private DrawingPanel panel;
    private List<FigureShape> savedShapes;

    public ClearCommand(DrawingPanel panel) {
        this.panel = panel;
        this.savedShapes = new ArrayList<>();
        // Fazer cópia das figuras antes de limpar
        for (FigureShape shape : panel.getShapes()) {
            savedShapes.add(shape.copy());
        }
    }

    @Override
    public void execute() {
        panel.clearDirectly();
    }

    @Override
    public void undo() {
        for (FigureShape shape : savedShapes) {
            panel.addShapeDirectly(shape);
        }
    }
}

// Command Manager (Invoker)
class CommandManager {
    private final List<Command> undoStack = new ArrayList<>();
    private final List<Command> redoStack = new ArrayList<>();

    public void executeCommand(Command command) {
        command.execute();
        undoStack.add(command);
        redoStack.clear(); // Limpar redo stack quando novo comando é executado
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            Command command = undoStack.remove(undoStack.size() - 1);
            command.undo();
            redoStack.add(command);
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.remove(redoStack.size() - 1);
            command.execute();
            undoStack.add(command);
        }
    }
}