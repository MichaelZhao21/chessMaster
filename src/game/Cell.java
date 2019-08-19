package game;

public class Cell {

    public char col;
    public int row;

    public Cell() {
        this.col = 'j';
        this.row = 0;
    }

    public Cell(char col, int row) {
        this.col = col;
        this.row = row;
    }

    public boolean compare(Cell compCell) {
        return (this.col == compCell.col && this.row == compCell.row);
    }

    public int getX() {
        return Game.charLetterToInt(col) * 60;
    }

    public int getY() {
        return (9 - row) * 60;
    }

    public char getAddedColChar(int change) {
        return Game.getCharForNumber(Game.charLetterToInt(col) + change);
    }

    public void print() {
        System.out.println(Character.toString(col) + row);
    }
}
