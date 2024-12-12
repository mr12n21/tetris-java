import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Tetris extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int BOARD_WIDTH = 12;
    private static final int BOARD_HEIGHT = 23;
    private static final int BLOCK_SIZE = 26;

    // Tetramino shapes and colors
    private static final Point[][][] TETRAMINOS = {
            {{new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)}, {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)}}, // I-Piece
            {{new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0)}, {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)}}, // J-Piece
            {{new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2)}, {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}}, // L-Piece
            {{new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)}}, // O-Piece
            {{new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)}}, // S-Piece
            {{new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)}}, // T-Piece
            {{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)}}  // Z-Piece
    };

    private static final Color[] TETRAMINO_COLORS = {
            Color.cyan, Color.blue, Color.orange, Color.yellow, Color.green, Color.pink, Color.red
    };

    private Point pieceOrigin;
    private int currentPiece;
    private int rotation;
    private List<Integer> nextPieces = new ArrayList<>();
    private long score;
    private Color[][] well;

    private long dropSpeed = 1000;
    private long minDropSpeed = 200;
    private long scoreIncreaseInterval = 10;
    private long speedIncreaseAmount = 50;

    // Initialize game
    private void init() {
        well = new Color[BOARD_WIDTH][BOARD_HEIGHT];
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT - 1; j++) {
                well[i][j] = (i == 0 || i == BOARD_WIDTH - 1 || j == BOARD_HEIGHT - 2) ? Color.GRAY : Color.BLACK;
            }
        }
        newPiece();
    }

    // Generate a new random piece
    public void newPiece() {
        pieceOrigin = new Point(5, 2);
        rotation = 0;
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
            Collections.shuffle(nextPieces);
        }
        currentPiece = nextPieces.remove(0);

        // Collision detection when spawning a new piece
        if (collidesAt(pieceOrigin.x, pieceOrigin.y, rotation)) {
            System.out.println("Game Over! Your score: " + score);
            System.exit(0);
        }

        // Increase speed as score increases
        if (score / scoreIncreaseInterval > (score - scoreIncreaseInterval) / scoreIncreaseInterval) {
            dropSpeed = Math.max(dropSpeed - speedIncreaseAmount, minDropSpeed);
        }
    }

    // Collision detection for the current piece
    private boolean collidesAt(int x, int y, int rotation) {
        for (Point p : TETRAMINOS[currentPiece][rotation]) {
            if (well[p.x + x][p.y + y] != Color.BLACK) {
                return true;
            }
        }
        return false;
    }

    // Rotate the piece
    public void rotate(int direction) {
        int newRotation = (rotation + direction + 4) % 4;
        if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
            rotation = newRotation;
        }
        repaint();
    }

    // Move the piece left or right
    public void move(int dx) {
        if (!collidesAt(pieceOrigin.x + dx, pieceOrigin.y, rotation)) {
            pieceOrigin.x += dx;
        }
        repaint();
    }

    // Drop the piece or fix it in place if it can't drop further
    public void dropDown() {
        if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
            pieceOrigin.y++;
        } else {
            fixToWell();
        }
        repaint();
    }

    // Fix the piece to the well
    public void fixToWell() {
        for (Point p : TETRAMINOS[currentPiece][rotation]) {
            well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = TETRAMINO_COLORS[currentPiece];
        }
        clearRows();
        newPiece();
    }

    // Delete a row and shift the above rows down
    private void deleteRow(int row) {
        for (int j = row - 1; j > 0; j--) {
            for (int i = 1; i < BOARD_WIDTH - 1; i++) {
                well[i][j + 1] = well[i][j];
            }
        }
    }

    // Clear full rows and update the score
    public void clearRows() {
        int clearedRows = 0;
        for (int j = BOARD_HEIGHT - 2; j > 0; j--) {
            boolean isFull = true;
            for (int i = 1; i < BOARD_WIDTH - 1; i++) {
                if (well[i][j] == Color.BLACK) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                deleteRow(j);
                j++;
                clearedRows++;
            }
        }

        switch (clearedRows) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 500;
            case 4 -> score += 800;
        }
    }

    // Draw the falling piece
    private void drawPiece(Graphics g) {
        g.setColor(TETRAMINO_COLORS[currentPiece]);
        for (Point p : TETRAMINOS[currentPiece][rotation]) {
            g.fillRect((p.x + pieceOrigin.x) * BLOCK_SIZE, (p.y + pieceOrigin.y) * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillRect(0, 0, BLOCK_SIZE * BOARD_WIDTH, BLOCK_SIZE * BOARD_HEIGHT);

        // Draw the well
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT - 1; j++) {
                g.setColor(well[i][j]);
                g.fillRect(i * BLOCK_SIZE, j * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
            }
        }

        // Draw the score
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, BLOCK_SIZE * 9, BLOCK_SIZE * 2);

        // Draw the current piece
        drawPiece(g);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(BLOCK_SIZE * BOARD_WIDTH + 10, BLOCK_SIZE * BOARD_HEIGHT + 30);
        frame.setVisible(true);

        final Tetris game = new Tetris();
        game.init();
        frame.add(game);

        // Keyboard controls
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> game.rotate(-1);
                    case KeyEvent.VK_DOWN -> game.rotate(1);
                    case KeyEvent.VK_LEFT -> game.move(-1);
                    case KeyEvent.VK_RIGHT -> game.move(1);
                    case KeyEvent.VK_SPACE -> {
                        game.dropDown();
                        game.score++;
                    }
                }
            }
        });

        // Drop piece every interval
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(game.dropSpeed);
                    game.dropDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
