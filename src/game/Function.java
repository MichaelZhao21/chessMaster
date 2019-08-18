package game;

public class Function {

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
