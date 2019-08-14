package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class Game implements MouseListener {

    private final int SQUARE = 60;
    private boolean whiteTurn = true;
    private Display display;
    boolean highlighted = false;
    Piece highlightedPiece;
    ArrayList<Cell> possibleMoves = new ArrayList<>();
    ArrayList<Piece> pieces = new ArrayList<>();

    public Game(Display display) {
        makePieces();
        this.display = display;
    }

    private void makePieces() {
        pieces.add(new Piece(PieceType.KING, true, new Cell('d', 1)));
        pieces.add(new Piece(PieceType.QUEEN, true, new Cell('e', 1)));
        pieces.add(new Piece(PieceType.BISHOP, true, new Cell('c', 1)));
        pieces.add(new Piece(PieceType.BISHOP, true, new Cell('f', 1)));
        pieces.add(new Piece(PieceType.KNIGHT, true, new Cell('b', 1)));
        pieces.add(new Piece(PieceType.KNIGHT, true, new Cell('g', 1)));
        pieces.add(new Piece(PieceType.ROOK, true, new Cell('a', 1)));
        pieces.add(new Piece(PieceType.ROOK, true, new Cell('h', 1)));
        pieces.add(new Piece(PieceType.KING, false, new Cell('d', 8)));
        pieces.add(new Piece(PieceType.QUEEN, false, new Cell('e', 8)));
        pieces.add(new Piece(PieceType.BISHOP, false, new Cell('c', 8)));
        pieces.add(new Piece(PieceType.BISHOP, false, new Cell('f', 8)));
        pieces.add(new Piece(PieceType.KNIGHT, false, new Cell('b', 8)));
        pieces.add(new Piece(PieceType.KNIGHT, false, new Cell('g', 8)));
        pieces.add(new Piece(PieceType.ROOK, false, new Cell('a', 8)));
        pieces.add(new Piece(PieceType.ROOK, false, new Cell('h', 8)));
        for (int i = 0; i < 8; i++) {
            char col = Function.getCharForNumber(i + 1);
            pieces.add(new Piece(PieceType.PAWN, true, new Cell(col, 2)));
            pieces.add(new Piece(PieceType.PAWN, false, new Cell(col, 7)));
        }
    }

    private void pickPiece(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (x < SQUARE || x > SQUARE * 9 || y < SQUARE || y > SQUARE * 9) return;

        Cell c = getCellClicked(x, y);
        Piece clicked = getClickedPiece(c);
        if (clicked != null) {
            if (whiteTurn == clicked.white) {
                highlight(clicked);
            }
        }
    }

    private Cell getCellClicked(int x, int y) {
        return new Cell(Function.getCharForNumber(x / SQUARE), 9 - (y / SQUARE));
    }

    private Piece getClickedPiece(Cell c) {
        for (Piece piece : pieces) {
            if (c.compare(piece.cell)) {
                return piece;
            }
        }
        return null;
    }

    private void highlight(Piece piece) {

        possibleMoves.clear();
        getPossibleMoves(piece);

        highlightedPiece = piece;
        highlighted = true;
        display.repaint();
    }

    private void getPossibleMoves(Piece piece) {
        switch (piece.type) {
            case PAWN:

                break;
            case KING:
                break;
            case QUEEN:
                break;
            case BISHOP:
                break;
            case KNIGHT:
                break;
            case ROOK:
                break;
        }
    }

    private void movePiece(MouseEvent e) {
        pickPiece(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (highlighted) {
            movePiece(e);
        }
        else {
            pickPiece(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
