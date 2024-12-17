import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Tetris extends JPanel {

    private static final int BOARD_WIDTH = 12;
    private static final int BOARD_HEIGHT = 24;
    private static final int INITIAL_DELAY = 500;
    private static final int MIN_DELAY = 100;
    private static final int DELAY_DECREMENT = 20;

    private final Point[][][] tetrominos = {
            // I-Piece
            {{new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)}},
            // O-Piece
            {{new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
                    {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
                    {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
                    {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)}},
            // T-Piece
            {{new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 1)}},
            // L-Piece
            {{new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0)}},
            // J-Piece
            {{new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}},
            // S-Piece
            {{new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)},
                    {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)},
                    {new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)},
                    {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}},
            // Z-Piece
            {{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)},
                    {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)}}
    };

    private final Color[] tetrominoColors = {
            Color.cyan, Color.yellow, Color.magenta, Color.orange, Color.blue, Color.green, Color.red
    };

    private Point pieceOrigin;
    private int currentPiece;
    private int rotation;
    private final Queue<Integer> nextPieces = new LinkedList<>();
    private Color[][] well;
    private int score;
    private int delay;

    public Tetris() {
        setFocusable(true);
        initKeyListener();
        initializeGame();
    }

    private void initKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> move(-1);
                    case KeyEvent.VK_RIGHT -> move(1);
                    case KeyEvent.VK_DOWN -> dropDown();
                    case KeyEvent.VK_UP -> rotate(1);
                }
                repaint();
            }
        });
    }

    private void initializeGame() {
        well = new Color[BOARD_WIDTH][BOARD_HEIGHT];
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                well[x][y] = (x == 0 || x == BOARD_WIDTH - 1 || y == BOARD_HEIGHT - 1) ? Color.GRAY : Color.BLACK;
            }
        }
        delay = INITIAL_DELAY;
        newPiece();
    }

    private void newPiece() {
        pieceOrigin = new Point(5, 2);
        rotation = 0;

        if (nextPieces.isEmpty()) {
            List<Integer> pieces = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
            Collections.shuffle(pieces);
            nextPieces.addAll(pieces);
        }

        currentPiece = nextPieces.poll();

        if (collidesAt(0, 0, rotation)) {
            endGame();
        }
    }

    private void move(int dx) {
        if (!collidesAt(dx, 0, rotation)) {
            pieceOrigin.translate(dx, 0);
        }
    }

    private void rotate(int dr) {
        int newRotation = (rotation + dr + 4) % 4;
        if (tryRotate(newRotation)) {
            rotation = newRotation;
        }
    }

    private boolean tryRotate(int newRotation) {
        if (!collidesAt(0, 0, newRotation)) return true;
        if (!collidesAt(-1, 0, newRotation)) {
            pieceOrigin.translate(-1, 0);
            return true;
        }
        if (!collidesAt(1, 0, newRotation)) {
            pieceOrigin.translate(1, 0);
            return true;
        }
        return false;
    }

    private boolean collidesAt(int dx, int dy, int rotation) {
        for (Point p : tetrominos[currentPiece][rotation]) {
            int x = pieceOrigin.x + p.x + dx;
            int y = pieceOrigin.y + p.y + dy;
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT || well[x][y] != Color.BLACK) {
                return true;
            }
        }
        return false;
    }

    private void dropDown() {
        if (!collidesAt(0, 1, rotation)) {
            pieceOrigin.translate(0, 1);
        } else {
            fixToWell();
        }
    }

    private void fixToWell() {
        for (Point p : tetrominos[currentPiece][rotation]) {
            well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetrominoColors[currentPiece];
        }
        clearRows();
        newPiece();
    }

    private void clearRows() {
        for (int y = BOARD_HEIGHT - 2; y >= 0; y--) {
            if (isRowFull(y)) {
                shiftRowsDown(y);
                score += 100;
                delay = Math.max(MIN_DELAY, delay - DELAY_DECREMENT);
                y++;
            }
        }
    }

    private boolean isRowFull(int row) {
        for (int x = 1; x < BOARD_WIDTH - 1; x++) {
            if (well[x][row] == Color.BLACK) return false;
        }
        return true;
    }

    private void shiftRowsDown(int row) {
        for (int y = row; y > 0; y--) {
            for (int x = 1; x < BOARD_WIDTH - 1; x++) {
                well[x][y] = well[x][y - 1];
            }
        }
    }

    private void endGame() {
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score);
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int scale = Math.min(getWidth() / BOARD_WIDTH, getHeight() / BOARD_HEIGHT);

        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT - 1; y++) {
                g.setColor(well[x][y]);
                g.fillRect(scale * x, scale * y, scale - 1, scale - 1);
            }
        }
        drawCurrentPiece(g, scale);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, scale * BOARD_WIDTH, scale);
    }

    private void drawCurrentPiece(Graphics g, int scale) {
        g.setColor(tetrominoColors[currentPiece]);
        for (Point p : tetrominos[currentPiece][rotation]) {
            g.fillRect((pieceOrigin.x + p.x) * scale, (pieceOrigin.y + p.y) * scale, scale - 1, scale - 1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            Tetris game = new Tetris();
            frame.add(game);
            frame.setSize(400, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            new Timer(game.delay, e -> {
                game.dropDown();
                game.repaint();
            }).start();
        });
    }
}
