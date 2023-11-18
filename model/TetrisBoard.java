// TetrisBoard.java
package model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.lang.Math;

/** Represents a Board class for Tetris.
 * Based on the Tetris assignment in the Nifty Assignments Database, authored by Nick Parlante
 */
public class TetrisBoard implements Serializable{
    private int width; //board height and width
    private int height;
    protected boolean[][] tetrisGrid; //board grid
    boolean committed; //indicates if the board is in a 'committed' state, meaning can't undo!

    //In your implementation, you'll want to keep counts of filled grid positions in each column.
    //A completely filled column means the game is over!
    private int colCounts[];
    //You will also want to keep counts by row.
    //A completely filled row can be cleared from the board (and points are awarded)!
    private int rowCounts[];

    //In addition, you'll need to allocate some space to back up your grid data.
    //This will be important when you implement "undo".
    private boolean[][] backupGrid; //to back up your grid
    private int backupColCounts[]; //to back up your row counts
    private int backupRowCounts[]; //to back up your column counts

    //error types (to be returned by the place function)
    public static final int ADD_OK = 0;
    public static final int ADD_ROW_FILLED = 1;
    public static final int ADD_OUT_BOUNDS = 2;
    public static final int ADD_BAD = 3;

    /**
     * Constructor for an empty board of the given width and height measured in blocks.
     *
     * @param aWidth    width
     * @param aHeight    height
     */
    public TetrisBoard(int aWidth, int aHeight) {
        width = aWidth;
        height = aHeight;
        tetrisGrid = new boolean[width][height];

        colCounts = new int[width];
        rowCounts = new int[height];

        //init backup storage, for undo
        backupGrid = new boolean[width][height];
        backupColCounts = new int[width];
        backupRowCounts = new int[height];
    }

    /**
     * Helper to fill new game grid with empty values
     */
    public void newGame() {
        for (int x = 0; x < tetrisGrid.length; x++) {
            for (int y = 0; y < tetrisGrid[x].length; y++) {
                tetrisGrid[x][y] = false;
            }
        }
        Arrays.fill(colCounts, 0);
        Arrays.fill(rowCounts, 0);
        committed = true;
    }

    /**
     * Getter for board width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Getter for board height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the max column height present in the board.
     * For an empty board this is 0.
     *
     * @return the y position of the last filled square in the tallest column
     */
    public int getMaxHeight() {
        return Arrays.stream(colCounts).max().getAsInt();
    }

    /**
     * Returns the height of the given column -- i.e. the y value of the highest block + 1.
     * The height is 0 if the column contains no blocks.
     *
     * @param x grid column, x
     *
     * @return the height of the given column, x
     */
    public int getColumnHeight(int x) {
        return colCounts[x];
    }

    /**
     * Returns the number of filled blocks in the given row.
     *
     * @param y grid row, y
     *
     * @return the number of filled blocks in row y
     */
    public int getRowWidth(int y) {
        return rowCounts[y];
    }

    /**
     * Returns true if the given block is filled in the board. Blocks outside of the
     * valid width/height area always return true (as we can't place anything there).
     *
     * @param x grid position, x
     * @param y grid position, y
     *
     * @return true if the given block at x,y is filled, else false
     */
    public boolean getGrid(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0 || tetrisGrid[x][y])
            return true;
        return false;
    }

    /**
     * Given a piece and an x, returns the y value where the piece will come to rest
     * if it were dropped straight down at that x.
     *
     * Use getLowestYVals and the col heights (getColumnHeight) to compute this quickly!
     *
     * @param piece piece to place
     * @param x column of grid
     *
     * @return the y value where the piece will come to rest
     */
    public int placementHeight(TetrisPiece piece, int x) {
        // to do!!!

        int i = 0;
        int pos = 0;

        int[] tempYVals;
        tempYVals = piece.getLowestYVals();
        int hightLength;
        hightLength = piece.getLowestYVals().length;



        while (true) {
            if (i >= hightLength) {
                break;
            }
            if ((getColumnHeight(x + i) - tempYVals[i]) <= pos) {
            } else {
                pos = getColumnHeight(x + i) - tempYVals[i];
            }
            i += 1;
        }
        return pos;
    }



    /**
     * Attempts to add the body of a piece to the board. Copies the piece blocks into the board grid.
     * Returns ADD_OK for a regular placement, or ADD_ROW_FILLED
     * for a regular placement that causes at least one row to be filled.
     *
     * Error cases:
     * A placement may fail in two ways. First, if part of the piece may fall out
     * of bounds of the board, ADD_OUT_BOUNDS is returned.
     * Or the placement may collide with existing blocks in the grid
     * in which case ADD_BAD is returned.
     * In both error cases, the board may be left in an invalid
     * state. The client can use undo(), to recover the valid, pre-place state.
     *
     * @param piece piece to place
     * @param x placement position, x
     * @param y placement position, y
     *
     * @return static int that defines result of placement
     */
    public int placePiece(TetrisPiece piece, int x, int y) {

        committed = false;
        backupGrid();

        int fin = ADD_OK;
        int xVal;
        int yVal;

        TetrisPoint[] pieceBody;
        pieceBody = piece.getBody();
        int i = 0;
        do {
            if (!(i < pieceBody.length)) {
                break;
            } else {
                TetrisPoint point = pieceBody[i];
                yVal = y + point.y;
                xVal = x + point.x;


                if ((yVal < height) && (yVal >= 0) && (0 <= xVal) && (xVal < width)) {
                    if (!tetrisGrid[xVal][yVal]) {
                        tetrisGrid[xVal][yVal] = true;

                        if (getColumnHeight(xVal) >= (1 + yVal)) {
                        } else {
                            colCounts[xVal] = yVal + 1;
                        }
                        rowCounts[yVal] = 1 + rowCounts[yVal];

                        if (rowCounts[yVal] != width) {
                            i++;
                            continue;
                        }
                        fin = ADD_ROW_FILLED;
                    } else {
                        fin = ADD_BAD;
                        break;
                    }

                } else {
                    fin = ADD_OUT_BOUNDS;
                    break;
                }

                i += 1;
            }
        } while (true);
        return fin;

//        throw new UnsupportedOperationException(); //replace this!
    }


    /**
     * Deletes rows that are filled all the way across, moving
     * things above down. Returns the number of rows cleared.
     *
     * @return number of rows cleared (useful for scoring)
     */
    public int clearRows() {
        if (!committed) {
        } else {
            committed = false;
            backupGrid();
        }

        boolean completeRow;
        completeRow = false;
        int rowTo;
        int rowFrom;
        int rowsCleared;
        rowsCleared = 0;

        rowTo = 0;
        rowFrom = 1;
        while (true) {
            if (rowFrom >= getMaxHeight()) break;
            if (!(!completeRow && !(rowCounts[rowTo] != width))) {
                while (completeRow && rowFrom < getMaxHeight() && rowCounts[rowFrom] == width) {
                    rowsCleared++;
                    rowFrom++;
                }
            } else {
                rowsCleared += 1;
                completeRow = true;

                while (true) {
                    if (!completeRow || rowFrom >= getMaxHeight() || rowCounts[rowFrom] != width) {
                        break;
                    }
                    rowsCleared += 1;
                    rowFrom += 1;
                }
            }

            if (!completeRow) {
            } else {
                if (rowFrom >= getMaxHeight()) {
                    int i = 0;
                    while (i < width) {
                        rowCounts[rowTo] = 0;
                        tetrisGrid[i][rowTo] = false;
                        i++;
                    }
                } else {
                    int i = 0;
                    while (i < width) {
                        if (tetrisGrid[i][rowFrom]) tetrisGrid[i][rowTo] = true;
                        else tetrisGrid[i][rowTo] = false;
                        rowCounts[rowTo] = rowCounts[rowFrom];
                        i++;
                    }
                }
            }
            rowTo++;
            rowFrom++;
        }

        if (!completeRow) {
            return rowsCleared;
        }
        int i = rowTo;
        while (i < getMaxHeight()) {
            rowCounts[i] = 0;
            int j = width - 1;
            if (0 <= j) {
                do {
                    tetrisGrid[j][i] = false;
                    j--;
                } while (0 <= j);
            }
            i++;
        }
        return rowsCleared;
    }


    /**
     * Reverts the board to its state before up to one call to placePiece() and one to clearRows();
     * If the conditions for undo() are not met, such as calling undo() twice in a row, then the second undo() does nothing.
     * See the overview docs.
     */
    public void undo() {
        if (committed == true) return;  //a committed board cannot be undone!

        if (backupGrid == null) throw new RuntimeException("No source for backup!");  //a board with no backup source cannot be undone!

        //make a copy!!
        for (int i = 0; i < backupGrid.length; i++) {
            System.arraycopy(backupGrid[i], 0, tetrisGrid[i], 0, backupGrid[i].length);
        }

        //copy row and column tallies as well.
        System.arraycopy(backupRowCounts, 0, rowCounts, 0, backupRowCounts.length);
        System.arraycopy(backupColCounts, 0, colCounts, 0, backupColCounts.length);

        committed = true; //no going backwards now!
    }

    /**
     * Copy the backup grid into the grid that defines the board (to support undo)
     */
    private void backupGrid() {
        //make a copy!!
        for (int i = 0; i < tetrisGrid.length; i++) {
            System.arraycopy(tetrisGrid[i], 0, backupGrid[i], 0, tetrisGrid[i].length);
        }
        //copy row and column tallies as well.
        System.arraycopy(rowCounts, 0, backupRowCounts, 0, rowCounts.length);
        System.arraycopy(colCounts, 0, backupColCounts, 0, colCounts.length);
    }

    /**
     * Puts the board in the 'committed' state.
     */
    public void commit() {
        committed = true;
    }

    /**
     * Fills heightsOfCols[] and widthOfRows[].  Useful helper to support clearing rows and placing pieces.
     */
    private void makeHeightAndWidthArrays() {

        Arrays.fill(colCounts, 0);
        Arrays.fill(rowCounts, 0);

        for (int x = 0; x < tetrisGrid.length; x++) {
            for (int y = 0; y < tetrisGrid[x].length; y++) {
                if (tetrisGrid[x][y]) { //means is not an empty cell
                    colCounts[x] = y + 1; //these tallies can be useful when clearing rows or placing pieces
                    rowCounts[y]++;
                }
            }
        }
    }

    /**
     * Print the board
     *
     * @return a string representation of the board (useful for debugging)
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (int y = height-1; y>=0; y--) {
            buff.append('|');
            for (int x=0; x<width; x++) {
                if (getGrid(x,y)) buff.append('+');
                else buff.append(' ');
            }
            buff.append("|\n");
        }
        for (int x=0; x<width+2; x++) buff.append('-');
        return(buff.toString());
    }


}