package game;

public class Cell {

    public char col;
    public int row;

    public Cell(char col, int row) {
        this.col = col;
        this.row = row;
    }

    public boolean compare(Cell compCell) {
        return (this.col == compCell.col && this.row == compCell.row);
    }

    public int getX() {
        return Function.charLetterToInt(col) * 60;
    }

    public int getY() {
        return (9 - row) * 60;
    }
}
