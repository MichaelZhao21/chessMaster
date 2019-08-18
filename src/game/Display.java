package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Display extends JPanel {

    private final int SQUARE = 60;
    private final String PIECE_DIRECTORY = "resources/pieces/letters";
    private Game game = new Game(this);

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(192, 192, 192));

        Graphics2D g2 = (Graphics2D) g;
        drawBoard(g2);
        drawLetters(g2);
        drawPieces(g2);
        if (game.state == GameState.HIGHLIGHTED) drawHighlights(g2);
        if (game.state == GameState.MOVED) drawScore(g2);
    }

    private void drawBoard(Graphics2D g) {
        boolean dark = false;

        g.setColor(new Color(50, 50, 50));
        g.fillRect(SQUARE / 2, SQUARE / 2, SQUARE * 9, SQUARE * 9);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (dark) {
                    g.setColor(new Color(255, 206, 158));
                }
                else {
                    g.setColor(new Color(209, 139, 71));
                }
                if (c != 7) dark = !dark;

                g.fillRect(SQUARE * (r + 1), SQUARE * (c + 1), SQUARE, SQUARE);
            }
        }
    }

    private void drawLetters(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.PLAIN, 25));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 8; i++) {
            String text = Function.getStringCharForNumber(8 - i);
            int xOffset = -(fm.stringWidth(text) / 2);
            int yOffset = fm.getAscent() - (fm.getHeight() / 2);
            g.drawString(text, (90 + (SQUARE * (7 - i))) + xOffset, 45 + yOffset);
            g.drawString(Integer.toString(i + 1), 45 + xOffset, (90 + (SQUARE * (7 - i))) + yOffset);
        }
    }

    private void drawPieces(Graphics2D g) {
        for (int i = 0; i < game.pieces.size(); i++) {
            Piece tempPiece = game.pieces.get(i);
            String letter = tempPiece.white ? "W" : "B";
            letter += Function.pieceTypeToLetter(tempPiece.type);
            BufferedImage img;
            try {
                img = ImageIO.read(new File(PIECE_DIRECTORY + "/" + letter + ".png"));
                g.drawImage(img,
                        Function.charLetterToInt(tempPiece.cell.col) * SQUARE,
                        (9 - tempPiece.cell.row) * SQUARE,
                        null);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void drawHighlights(Graphics2D g) {
        Piece clickedPiece = game.highlightedPiece;
        int x = clickedPiece.cell.getX();
        int y = clickedPiece.cell.getY();
        drawSingleGradient(g, true, x, y);
        for (Cell cell : game.highlightedPiece.moves) {
            drawSingleGradient(g, false, cell.getX(), cell.getY());
        }
    }

    private void drawSingleGradient(Graphics2D g, boolean green, int x, int y) {
        Point2D top = new Point2D.Float(30 + x, y);
        Point2D left = new Point2D.Float(x, 30 + y);
        Point2D right = new Point2D.Float(60 + x, 30 + y);
        Point2D bottom = new Point2D.Float(30 + x, 60 + y);

        final int GRAD_OFFSET = 15;
        paintGradientLayer(g, green, x, y, top, new Point2D.Float(30 + x, 30 + y - GRAD_OFFSET));
        paintGradientLayer(g, green, x, y, left, new Point2D.Float(30 + x - GRAD_OFFSET, 30 + y));
        paintGradientLayer(g, green, x, y, right, new Point2D.Float(30 + x + GRAD_OFFSET, 30 + y));
        paintGradientLayer(g, green, x, y, bottom, new Point2D.Float(30 + x, 30 + y + GRAD_OFFSET));
    }

    private void paintGradientLayer(Graphics2D g, boolean green, int x, int y, Point2D start, Point2D end) {
        Color color = green ? new Color(150, 255, 159, 200) : new Color(133, 248, 250, 200);
        GradientPaint gradientPaint = new GradientPaint(
                start, color, end, new Color(255, 255, 255, 0));
        g.setPaint(gradientPaint);
        g.fillRect(x, y, 60, 60);
    }

    private void drawScore(Graphics2D g) {
        System.out.println(game.score);
    }

    private void createFrame() {
        JFrame f = new JFrame();
        this.addMouseListener(game);
        f.add(this);
        f.getContentPane().setPreferredSize(new Dimension(1100, 600));
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setTitle("Chess Master");
        f.pack();
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        Display d = new Display();
        d.createFrame();
    }

}
