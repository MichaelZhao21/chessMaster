package game;

import java.util.ArrayList;

enum SameState {NONE, LETTER, NUM, BOTH}
enum SpecialNotation {CAPTURE, CASTLE_KING, CASTLE_QUEEN, CHECKMATE, DRAW, PAWN_PROMO, CHECK, EN_PASSANT}

public class NotationObject {

    private ArrayList<Piece> pieces;
    public PieceType type;
    public boolean white;
    public Cell oldCell;
    public Cell moveTo;
    public ArrayList<SpecialNotation> extraNotationList;

    public NotationObject(ArrayList<Piece> pieces,
                          Piece piece) {
        this.pieces = pieces;
        this.type = piece.type;
        this.white = piece.white;
        this.oldCell = piece.cell;
        this.extraNotationList = new ArrayList<>();
    }

    public String getString() {
        SameState state = getConflictingPieces();
        StringBuilder sb = new StringBuilder();
        if (getExtra(SpecialNotation.CASTLE_KING)) {
            sb.append("0-0");
        }
        else if (getExtra(SpecialNotation.CASTLE_QUEEN)) {
            sb.append("0-0-0");
        }
        else {
            if (type == PieceType.PAWN) {
                if (getExtra(SpecialNotation.CAPTURE)) sb.append(moveTo.col);
            }
            else {
                sb.append(Game.pieceTypeToLetter(type));
                if (state == SameState.LETTER || state == SameState.BOTH) sb.append(oldCell.col);
                if (state == SameState.NUM || state == SameState.BOTH) sb.append(oldCell.row);
            }
            if (getExtra(SpecialNotation.CAPTURE)) sb.append("x");
            sb.append(moveTo.col);
            sb.append(moveTo.row);
            if (getExtra(SpecialNotation.EN_PASSANT)) sb.append("e.p.");
        }

        return sb.toString();
    }

    private SameState getConflictingPieces() {
        SameState state = SameState.NONE;
        for (Piece piece : pieces) {
            if (piece.type == type && !piece.cell.compare(moveTo)) {
                for (Cell move : piece.moves) {
                    if (moveTo.compare(move)) state = getSameState(state, piece.cell);
                }
            }
        }
        return state;
    }

    private SameState getSameState(SameState state, Cell conflictCell) {
        if (state == SameState.NONE && oldCell.col != conflictCell.col) {
            return SameState.LETTER;
        }
        else if (state == SameState.NONE && oldCell.row != conflictCell.row) {
            return SameState.NUM;
        }
        else if (state == SameState.LETTER && oldCell.row != conflictCell.row) {
            return SameState.BOTH;
        }
        else if (state == SameState.NUM && oldCell.col != conflictCell.col) {
            return SameState.BOTH;
        }
        return SameState.NONE;
    }

    private boolean getExtra(SpecialNotation extra) {
        for (SpecialNotation n : extraNotationList) {
            if (n == extra) return true;
        }
        return false;
    }

}
