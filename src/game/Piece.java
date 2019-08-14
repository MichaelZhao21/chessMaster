package game;

enum PieceType {KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN}

public class Piece {

    public PieceType type;
    public boolean black;
    public Cell cell;

    public Piece (PieceType type, boolean black, Cell cell) {
        this.type = type;
        this.black = black;
        this.cell = cell;
    }

}
