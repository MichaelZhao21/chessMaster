package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

enum NotationException {NONE, CAPTURE, EN_PASSANT, CHECKMATE, DRAW}
enum GameState {NONE, HIGHLIGHTED, MOVED, END}

public class Game implements MouseListener {

    private final int SQUARE = 60;
    private final int[][] UNIT_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private boolean whiteTurn = true;
    private Display display;
    ArrayList<Piece> pieces = new ArrayList<>();
    ArrayList<String> score = new ArrayList<>();
    GameState state = GameState.NONE;
    Piece highlightedPiece;
    int turn = 0;

    public Game(Display display) {
        this.display = display;
        makePieces();
        writeMoves();
    }

    private void makePieces() {
        pieces.add(new Piece(PieceType.KING, true, new Cell('e', 1)));
        pieces.add(new Piece(PieceType.PAWN, true, new Cell('d', 1)));
        pieces.add(new Piece(PieceType.PAWN, true, new Cell('d', 2)));
        pieces.add(new Piece(PieceType.PAWN, true, new Cell('f', 1)));
        pieces.add(new Piece(PieceType.PAWN, true, new Cell('f', 2)));
        pieces.add(new Piece(PieceType.PAWN, true, new Cell('a', 1)));
        pieces.add(new Piece(PieceType.KING, false, new Cell('a', 8)));
        pieces.add(new Piece(PieceType.QUEEN, false, new Cell('a', 4)));

//        pieces.add(new Piece(PieceType.KING, true, new Cell('e', 1)));
//        pieces.add(new Piece(PieceType.QUEEN, true, new Cell('d', 1)));
//        pieces.add(new Piece(PieceType.BISHOP, true, new Cell('c', 1)));
//        pieces.add(new Piece(PieceType.BISHOP, true, new Cell('f', 1)));
//        pieces.add(new Piece(PieceType.KNIGHT, true, new Cell('b', 1)));
//        pieces.add(new Piece(PieceType.KNIGHT, true, new Cell('g', 1)));
//        pieces.add(new Piece(PieceType.ROOK, true, new Cell('a', 1)));
//        pieces.add(new Piece(PieceType.ROOK, true, new Cell('h', 1)));
//        pieces.add(new Piece(PieceType.KING, false, new Cell('e', 8)));
//        pieces.add(new Piece(PieceType.QUEEN, false, new Cell('d', 8)));
//        pieces.add(new Piece(PieceType.BISHOP, false, new Cell('c', 8)));
//        pieces.add(new Piece(PieceType.BISHOP, false, new Cell('f', 8)));
//        pieces.add(new Piece(PieceType.KNIGHT, false, new Cell('b', 8)));
//        pieces.add(new Piece(PieceType.KNIGHT, false, new Cell('g', 8)));
//        pieces.add(new Piece(PieceType.ROOK, false, new Cell('a', 8)));
//        pieces.add(new Piece(PieceType.ROOK, false, new Cell('h', 8)));
//        for (int i = 0; i < 8; i++) {
//            char col = Function.getCharForNumber(i + 1);
//            pieces.add(new Piece(PieceType.PAWN, true, new Cell(col, 2)));
//            pieces.add(new Piece(PieceType.PAWN, false, new Cell(col, 7)));
//        }
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

    private void movePiece(MouseEvent e) {
        state = GameState.NONE;
        int x = e.getX();
        int y = e.getY();
        Cell clickedCell = getCellClicked(x, y);

        for (Cell nextMove : highlightedPiece.moves) {
            if (clickedCell.compare(nextMove)) {
                Piece p = getOverlapPiece(clickedCell);
                if (p.type != PieceType.EMPTY) {
                    pieces.remove(p);
                    notate(highlightedPiece, clickedCell, NotationException.CAPTURE, p);
                }
                else {
                    notate(highlightedPiece, clickedCell, NotationException.NONE, new Piece(PieceType.NONE));
                }
                highlightedPiece.cell = clickedCell;
                state = GameState.MOVED;
                whiteTurn = !whiteTurn;
            }
        }

        if (state == GameState.MOVED) {
            Piece checkPiece = checkMateCheck();
            writeMoves();
            if (checkPiece.type != PieceType.NONE)
                endGame(NotationException.CHECKMATE, checkPiece);
            else if (drawCheck())
                endGame(NotationException.DRAW, new Piece(PieceType.NONE));
        }
        else {
            pickPiece(e);
        }

        display.repaint();
    }

    private void endGame(NotationException e, Piece extra) {
        state = GameState.END;
        if (e == NotationException.CHECKMATE) {
            System.out.println((extra.white ? "black" : "white") + " wins!");
        }
        else {
            System.out.println("Draw");
        }
    }

    private void notate(Piece piece, Cell moveTo, NotationException exception, Piece args) {
        String pieceLetter = Function.pieceTypeToLetter(piece.type);
        String moveString = Character.toString(moveTo.col) + moveTo.row;
        String note = "";
        if (exception == NotationException.NONE)
            note = (pieceLetter.equals("P") ? "" : pieceLetter) + moveString;

        addToScore(note);
    }

    private void addToScore(String nextNote) {
        if (whiteTurn) score.add(++turn + ". ");
        score.add(nextNote);
    }

    private void highlight(Piece piece) {
        highlightedPiece = piece;
        state = GameState.HIGHLIGHTED;
        display.repaint();
    }

    private void writeMoves() {
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            piece.moves = getPossibleMoves(piece);
        }
    }

    private ArrayList<Cell> getPossibleMoves(Piece piece) {
        ArrayList<Cell> output = getUncheckedMoves(piece);
        ArrayList<Cell> bad = new ArrayList<>();
        for (Cell c : output) {
            if (checkCheck(piece, c)) bad.add(c);
        }
        for (Cell c : bad) {
            output.remove(c);
        }
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
                return getKnightMoves(piece);
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
        return testMovesFromMoveList(UNIT_DIRECTIONS, piece);
    }

    private ArrayList<Cell> getKnightMoves(Piece piece) {
        ArrayList<Cell> output = new ArrayList<>();
        Cell moveA;
        Cell moveB;
        for (int[] moveSet : UNIT_DIRECTIONS) {
            if (moveSet[0] != 0) {
                moveA = new Cell(piece.cell.getAddedColChar(2 * moveSet[0]),
                        piece.cell.row + 1);
                moveB = new Cell(piece.cell.getAddedColChar(2 * moveSet[0]),
                        piece.cell.row - 1);
            }
            else {
                moveA = new Cell(piece.cell.getAddedColChar(1),
                        piece.cell.row + (2 * moveSet[1]));
                moveB = new Cell(piece.cell.getAddedColChar(-1),
                        piece.cell.row + (2 * moveSet[1]));
            }
            if (captureCheck(moveA, piece) || checkEmpty(moveA)) output.add(moveA);
            if (captureCheck(moveB, piece) || checkEmpty(moveB)) output.add(moveB);
        }
        return output;
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

    private boolean checkCheck(Piece piece, Cell newLocation) {
        Cell oldLocation = piece.cell;
        Piece eatenPiece = getOverlapPiece(newLocation);

        if (eatenPiece.type != PieceType.EMPTY) pieces.remove(eatenPiece);
        piece.cell = newLocation;

        boolean check = checkCheck(piece.white);

        if (eatenPiece.type != PieceType.EMPTY) pieces.add(eatenPiece);
        piece.cell = oldLocation;

        return check;
    }

    private boolean checkCheck(boolean white) {
        Cell kingCell = getKing(white).cell;
        if (kingCell != null) {
            for (Piece piece : pieces) {
                if (piece.white != white) {
                    ArrayList<Cell> pieceMoves = getUncheckedMoves(piece);
                    for (Cell testCell : pieceMoves) {
                        if (testCell.compare(kingCell)) return true;
                    }
                }
            }
        }
        return false;
    }

    private Piece checkMateCheck() {
        Piece king = getKing(whiteTurn);
        if (checkCheck(whiteTurn)) {
            ArrayList<Cell> possibleKingMoves = getPossibleMoves(king);
            for (Cell c : possibleKingMoves) {
                if (!checkCheck(king, c)) return new Piece(PieceType.NONE);
            }
            return king;
        }
        return new Piece(PieceType.NONE);
    }

    private Piece getKing(boolean white) {
        for (Piece piece : pieces) {
            if (piece.type == PieceType.KING && piece.white == white) return piece;
        }
        return new Piece(PieceType.NONE);
    }

    private boolean drawCheck() {
        // TODO DRAW CHECK - Stalemate,
        //  & insuff check material:KvK, KvB/K, KvK/K, K/BvK/B (B same color)
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (state == GameState.HIGHLIGHTED) {
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
