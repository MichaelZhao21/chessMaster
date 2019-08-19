package game;

import java.util.ArrayList;

enum PieceType {KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN, NONE, EMPTY}

public class Piece {

    public PieceType type;
    public boolean white;
    public Cell cell;
    public ArrayList<Cell> moves = new ArrayList<>();
    public boolean tempDelete = false;

    public Piece(PieceType type, boolean white, Cell cell) {
        this.type = type;
        this.white = white;
        this.cell = cell;
    }

    public Piece(PieceType type) {
        this.type = type;
        this.white = true;
        this.cell = new Cell();
    }

    public void print() {
        System.out.println(String.format(
                "PIECE | %s %s -> %c%d", (white ? "White" : "Black"), type, cell.col, cell.row));
    }

    public void printMoves() {
        print();
        for (Cell c : moves) {
            c.print();
        }
    }

}
