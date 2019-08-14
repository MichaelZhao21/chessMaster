package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Display extends JPanel {

    private final int SQUARE = 60;
    private final String PIECE_DIRECTORY = "resources/pieces/letters";
    private Game game = new Game();

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(192, 192, 192));

        Graphics2D g2 = (Graphics2D) g;
        drawBoard(g2);
        drawLetters(g2);
        drawPieces(g2);

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

    private void drawPieces(Graphics2D g) {
        for (int i = 0; i < game.pieces.size(); i++) {
            Piece tempPiece = game.pieces.get(i);
            String letter = tempPiece.black ? "B" : "W";
            letter += pieceTypeToLetter(tempPiece.type);

            BufferedImage img;
            try {
                img = ImageIO.read(new File(PIECE_DIRECTORY + "/" + letter + ".png"));
                g.drawImage(img,
                        Function.charLetterToInt(tempPiece.cell.col) * SQUARE,
                        tempPiece.cell.row * SQUARE,
                        null);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String pieceTypeToLetter(PieceType type) {
        switch (type) {
            case KING:
                return "K";
            case QUEEN:
                return "Q";
            case BISHOP:
                return "B";
            case KNIGHT:
                return "N";
            case ROOK:
                return "R";
        }
        return "P";
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
