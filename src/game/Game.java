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

        possibleMoves = getPossibleMoves(piece);
        highlightedPiece = piece;
        highlighted = true;
        display.repaint();
    }

    private ArrayList<Cell> getPossibleMoves(Piece piece) {
        ArrayList<Cell> output = getUncheckedMoves(piece);
        return output;
    }

    private ArrayList<Cell> getUncheckedMoves(Piece piece) {
        switch (piece.type) {
            case KING:
                return getKingMoves(piece);
            case QUEEN:
                return getQueenMoves(piece);
            case BISHOP:
                return getBishopMoves(piece);
            case KNIGHT:
            case ROOK:
                return getRookMoves(piece);
            case PAWN:
                return getPawnMoves(piece);
        }
        return new ArrayList<>();
    }

    private ArrayList<Cell> getKingMoves(Piece piece) {
        ArrayList<Cell> output = new ArrayList<>();
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                if (!(row == 0 && col == 0)) {
                    Cell newCell = new Cell(piece.cell.getAddedColChar(col), piece.cell.row + row);
                    if (getOverlapPiece(newCell).type == PieceType.EMPTY ||
                            captureCheck(newCell, piece)) output.add(newCell);
                }
            }
        }
        return output;
    }

    private ArrayList<Cell> getPawnMoves(Piece piece) {
        ArrayList<Cell> output = new ArrayList<>();

        // Move 1 space
        Cell oneMove = new Cell(piece.cell.col, piece.cell.row + (piece.white ? 1 : -1));
        if (checkEmpty(oneMove)) output.add(oneMove);

        // Move 2 spaces
        if (output.size() > 0) {
            if ((piece.white && piece.cell.row == 2) ||
                    (!piece.white && piece.cell.row == 7)) {
                Cell twoMove = new Cell(piece.cell.col, piece.cell.row + (piece.white ? 2 : -2));
                if (checkEmpty(twoMove)) output.add(twoMove);
            }
        }

        // Capture
        Cell captureLeft = new Cell(piece.cell.getAddedColChar(piece.white ? 1 : -1),
                piece.cell.row + (piece.white ? 1 : -1));
        Cell captureRight = new Cell(piece.cell.getAddedColChar(piece.white ? -1 : 1),
                piece.cell.row + (piece.white ? 1 : -1));
        if (captureCheck(captureLeft, piece)) output.add(captureLeft);
        if (captureCheck(captureRight, piece)) output.add(captureRight);
        // TODO: Add En passant rules (get the previous move)
        return output;
    }

    private ArrayList<Cell> getQueenMoves(Piece piece) {
        ArrayList<Cell> output = getBishopMoves(piece);
        output.addAll(getRookMoves(piece));
        return output;
    }

    private ArrayList<Cell> getBishopMoves(Piece piece) {
        int[][] moveList = {{1,1}, {-1, -1}, {-1, 1}, {1, -1}};
        return testMovesFromMoveList(moveList, piece);
    }

    private ArrayList<Cell> getRookMoves(Piece piece) {
        int[][] moveList = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        return testMovesFromMoveList(moveList, piece);
    }

    private ArrayList<Cell> testMovesFromMoveList(int[][] moveList, Piece piece) {
        boolean nextEmpty;
        int incr;
        Cell nextCell;
        ArrayList<Cell> output = new ArrayList<>();

        for (int[] moveSet : moveList) {
            nextEmpty = true;
            incr = 0;
            while (nextEmpty) {
                incr++;
                nextCell = new Cell(piece.cell.getAddedColChar(incr * moveSet[0]),
                        piece.cell.row + (incr * moveSet[1]));
                if (captureCheck(nextCell, piece)) {
                    output.add(nextCell);
                    nextEmpty = false;
                }
                else if (checkEmpty(nextCell)) {
                    output.add(nextCell);
                }
                else {
                    nextEmpty = false;
                }
            }
        }
        return output;
    }

    private boolean captureCheck(Cell enemyCell, Piece attackPiece) {
        Piece piece = getOverlapPiece(enemyCell);
        return (piece.type != PieceType.NONE &&
                piece.type != PieceType.EMPTY &&
                piece.white != attackPiece.white);
    }

    private boolean checkEmpty(Cell cell) {
        return (getOverlapPiece(cell).type == PieceType.EMPTY);
    }

    private Piece getOverlapPiece(Cell cell) {
        for (Piece piece : pieces) {
            if (cell.compare(piece.cell)) return piece;
        }
        if (cell.row < 1 ||
                cell.row > 8 ||
                Function.charLetterToInt(cell.col) < 1 ||
                Function.charLetterToInt(cell.col) > 8) {
            return new Piece(PieceType.NONE);
        } else {
            return new Piece(PieceType.EMPTY);
        }
    }

//
//    private boolean checkCheck(Piece piece, Cell newLocation) {
//        Cell oldLocation = piece.cell;
//        piece.cell = newLocation;
//        boolean check = checkCheck(piece.white);
//        piece.cell = oldLocation;
//        return check;
//    }
//
//    private boolean checkCheck(boolean white) {
//        Cell kingCell = null;
//        for (Piece piece : pieces) {
//            if (piece.type == PieceType.KING && piece.white == white) kingCell = piece.cell;
//        }
//        if (kingCell != null) {
//            for (Piece piece : pieces) {
//                if (piece.white == white) {
//                    if (testForCheckMoves(kingCell, piece)) return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean testForCheckMoves(Cell kingCell, Piece movePiece) {
//        ArrayList<Cell> moves = getPossibleMoves(movePiece);
//        for (Cell c : moves) {
//            if (c.compare(kingCell)) return true;
//        }
//        return false;
//    }

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
