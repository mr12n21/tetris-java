import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Tetris extends JPanel {
    private final Point[][][] Tetraminos = {
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
                    {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(1, 2)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
                    {new Point(1, 0), new Point(1, 1), new Point(0, 2), new Point(1, 2)}},
            // J-Piece
            {{new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
                    {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
                    {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2)},
                    {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}},
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

    private final Color[] tetraminoColors = {
            Color.cyan, Color.yellow, Color.magenta, Color.orange, Color.blue, Color.green, Color.red
    };

    private Point pieceOrigin;
    private int currentPiece;
    private int rotation;
    private ArrayList<Integer> nextPieces = new ArrayList<>();
    private Color[][] well;
    private int score;
    private int delay = 500;

    public Tetris() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: move(-1); break;
                    case KeyEvent.VK_RIGHT: move(1); break;
                    case KeyEvent.VK_DOWN: dropDown(); break;
                    case KeyEvent.VK_UP: rotate(-1); break;
                }
                repaint();
            }
        });
        initGame();
    }

    private void initGame() {
        well = new Color[12][24];
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 24; j++) {
                if (i == 0 || i == 11 || j == 23) {
                    well[i][j] = Color.GRAY;
                } else {
                    well[i][j] = Color.BLACK;
                }
            }
        }
        newPiece();
    }

    public void newPiece() {
        pieceOrigin = new Point(5, 2);
        rotation = 0;
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
            Collections.shuffle(nextPieces);
        }
        currentPiece = nextPieces.get(0);
        nextPieces.remove(0);
        if (collidesAt(0, 0, rotation)) {
            gameOver();
        }
    }

    private boolean collidesAt(int x, int y, int rotation) {
        for (Point p : Tetraminos[currentPiece][rotation]) {
            if (well[pieceOrigin.x + p.x + x][pieceOrigin.y + p.y + y] != Color.BLACK) {
                return true;
            }
        }
        return false;
    }

    public void move(int dx) {
        if (!collidesAt(dx, 0, rotation)) {
            pieceOrigin.x += dx;
        }
    }

    public void rotate(int dr) {
        int newRotation = (rotation + dr) % 4;
        if (newRotation < 0) newRotation = 3;
        if (!collidesAt(0, 0, newRotation)) {
            rotation = newRotation;
        }
    }

    public void dropDown() {
        if (!collidesAt(0, 1, rotation)) {
            pieceOrigin.y += 1;
        } else {
            fixToWell();
        }
    }

    private void fixToWell() {
        for (Point p : Tetraminos[currentPiece][rotation]) {
            well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetraminoColors[currentPiece];
        }
        clearRows();
        newPiece();
    }

    private void clearRows() {
        boolean fullRow;
        for (int row = 22; row >= 0; row--) {
            fullRow = true;
            for (int col = 1; col < 11; col++) {
                if (well[col][row] == Color.BLACK) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                score += 100;
                delay = Math.max(100, delay - 20); // Accelerate the game
                for (int r = row; r > 0; r--) {
                    for (int c = 1; c < 11; c++) {
                        well[c][r] = well[c][r - 1];
                    }
                }
                row++;
            }
        }
    }

    private void drawPiece(Graphics g) {
        g.setColor(tetraminoColors[currentPiece]);
        for (Point p : Tetraminos[currentPiece][rotation]) {
            g.fillRect((pieceOrigin.x + p.x) * getScale(), (pieceOrigin.y + p.y) * getScale(), getScale() - 1, getScale() - 1);
        }
    }

    private int getScale() {
        return Math.min(getWidth() / 12, getHeight() / 24);
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score);
        System.exit(0);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int scale = getScale();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 23; j++) {
                g.setColor(well[i][j]);
                g.fillRect(scale * i, scale * j, scale - 1, scale - 1);
            }
        }
        drawPiece(g);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, scale * 12, scale);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Tetris");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 800); // Adjusted for dynamic scaling
        f.setVisible(true);
        final Tetris game = new Tetris();
        f.add(game);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(game.delay);
                    game.dropDown();
                    game.repaint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}