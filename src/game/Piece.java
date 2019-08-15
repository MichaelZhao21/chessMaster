package game;

enum PieceType {KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN, NONE, EMPTY}

public class Piece {

    public PieceType type;
    public boolean white;
    public Cell cell;

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

}
