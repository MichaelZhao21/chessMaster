package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

enum GameState {NONE, HIGHLIGHTED, MOVED, END}

public class Game implements MouseListener {

    private final int SQUARE = 60;
    private final int[][] UNIT_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private final String BOARD_NAME = "default";
    private boolean whiteTurn = true;
    private Display display;
    ArrayList<Piece> pieces = new ArrayList<>();
    ArrayList<NotationObject> score = new ArrayList<>();
    GameState state = GameState.NONE;
    Piece highlightedPiece;
    NotationObject moveNotation;

    public Game(Display display) {
        this.display = display;
        makePieces();
        writeMoves();
    }

    private void makePieces() {
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(new File("resources/boards/" + BOARD_NAME + ".txt")));
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                processRow(line, row);
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processRow(String line, int row) {
        boolean white;
        PieceType pieceType;
        int col = 0;
        while (line.length() > 0) {
            col++;
            if (!line.substring(0, 1).equals("0")) {
                white = line.substring(0, 1).equals("W");
                pieceType = letterToPieceType(line.substring(1, 2));
                pieces.add(new Piece(pieceType, white, new Cell(getCharForNumber(col), 9 - row)));

            }
            line = line.substring(2);
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
        return new Cell(getCharForNumber(x / SQUARE), 9 - (y / SQUARE));
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
        moveNotation = new NotationObject(pieces, highlightedPiece);
        Cell clickedCell = getCellClicked(x, y);

        for (Cell nextMove : highlightedPiece.moves) {
            if (clickedCell.compare(nextMove)) {
                Piece p = getOverlapPiece(clickedCell);
                if (p.type != PieceType.EMPTY) {
                    pieces.remove(p);
                    moveNotation.extraNotationList.add(SpecialNotation.CAPTURE);
                }
                moveNotation.moveTo = nextMove;
                state = GameState.MOVED;
            }
        }

        if (state == GameState.MOVED) {
            if (enPassantCheck(highlightedPiece.cell, clickedCell)) {
                Piece p = getOverlapPiece(new Cell(
                        clickedCell.col,
                        clickedCell.row + (highlightedPiece.white ? -1 : 1)));
                pieces.remove(p);
                moveNotation.extraNotationList.add(SpecialNotation.EN_PASSANT);
                moveNotation.extraNotationList.add(SpecialNotation.CAPTURE);
            }
            highlightedPiece.cell = clickedCell;
            whiteTurn = !whiteTurn;
            if (checkMateCheck().type != PieceType.NONE) {
                state = GameState.END;
                moveNotation.extraNotationList.add(SpecialNotation.CHECKMATE);
            }
            else if (drawCheck()) {
                moveNotation.extraNotationList.add(SpecialNotation.DRAW);
            }
            score.add(moveNotation);
            System.out.println(moveNotation.getString());
            writeMoves();
        }
        else {
            pickPiece(e);
        }

        if (state == GameState.END) endGame();

        display.repaint();
    }

    private boolean enPassantCheck(Cell originalPos, Cell eatPos) {
        if (score.size() == 0) return false;
        NotationObject previousMove = score.get(score.size() - 1);
        if (previousMove.type == PieceType.PAWN &&
                previousMove.oldCell.row == (previousMove.white ? 2 : 7) &&
                previousMove.moveTo.row == originalPos.row &&
                previousMove.moveTo.col != originalPos.col &&
                previousMove.moveTo.col == eatPos.col)
            return true;
        return false;
    }

    private void endGame() {
        state = GameState.END;
        if (moveNotation.extraNotationList.indexOf(SpecialNotation.CHECKMATE) != -1) {
            System.out.println((moveNotation.white ? "white" : "black") + " wins!");
        }
        else {
            System.out.println("Draw");
        }
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

        // Capturing
        Cell captureLeft = new Cell(piece.cell.getAddedColChar(piece.white ? 1 : -1),
                piece.cell.row + (piece.white ? 1 : -1));
        Cell captureRight = new Cell(piece.cell.getAddedColChar(piece.white ? -1 : 1),
                piece.cell.row + (piece.white ? 1 : -1));
        if (captureCheck(captureLeft, piece) || enPassantCheck(piece.cell, captureLeft))
            output.add(captureLeft);
        if (captureCheck(captureRight, piece) || enPassantCheck(piece.cell, captureRight))
            output.add(captureRight);

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
                charLetterToInt(cell.col) < 1 ||
                charLetterToInt(cell.col) > 8) {
            return new Piece(PieceType.NONE);
        } else {
            return new Piece(PieceType.EMPTY);
        }
    }

    private boolean checkCheck(Piece piece, Cell newLocation) {
        Cell oldLocation = piece.cell;
        Piece eatenPiece = getOverlapPiece(newLocation);

        if (eatenPiece.type != PieceType.EMPTY) eatenPiece.tempDelete = true;
        piece.cell = newLocation;

        boolean check = checkCheck(piece.white);

        if (eatenPiece.type != PieceType.EMPTY) eatenPiece.tempDelete = false;
        piece.cell = oldLocation;

        return check;
    }

    //TODO: King can eat INTO Check -- NO
    private boolean checkCheck(boolean white) {
        Cell kingCell = getKing(white).cell;
        if (kingCell != null) {
            for (Piece piece : pieces) {
                if (piece.white != white && !piece.tempDelete) {
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
            for (Piece piece : pieces) {
                for (Cell c : piece.moves) {
                    if (!checkCheck(king, c)) return new Piece(PieceType.NONE);
                }
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
        if (state != GameState.END) {
            if (state == GameState.HIGHLIGHTED) {
                movePiece(e);
            } else {
                pickPiece(e);
            }
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

    public static String getStringCharForNumber(int i) {
        return String.valueOf(getCharForNumber(i));
    }

    public static char getCharForNumber(int i) {
        return (char)(i + 96);
    }

    public static int charLetterToInt(char c) {
        return Character.getNumericValue(c) - 9;
    }

    public static String pieceTypeToLetter(PieceType type) {
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

    public static PieceType letterToPieceType(String letter) {
        switch (letter) {
            case "K":
                return PieceType.KING;
            case "Q":
                return PieceType.QUEEN;
            case "B":
                return PieceType.BISHOP;
            case "N":
                return PieceType.KNIGHT;
            case "R":
                return PieceType.ROOK;
        }
        return PieceType.PAWN;
    }

}
