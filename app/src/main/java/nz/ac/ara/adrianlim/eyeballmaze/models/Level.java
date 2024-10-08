package nz.ac.ara.adrianlim.eyeballmaze.models;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import nz.ac.ara.adrianlim.eyeballmaze.enums.Color;
import nz.ac.ara.adrianlim.eyeballmaze.enums.Direction;
import nz.ac.ara.adrianlim.eyeballmaze.enums.Message;
import nz.ac.ara.adrianlim.eyeballmaze.enums.Shape;

public class Level {

	private final int height;
    private final int width;
    private final Square[][] squares;
    private final int[][] levelLayout;
    private final Set<Position> goals = new HashSet<>();

    private int completedGoalCount;

    private final String levelName;

    // Constructor 
    // Created in portoflio version, extending the original Ass2 constructor
    public Level(String levelName, int[][] levelLayout) {
        this.levelLayout = levelLayout;
        this.height = levelLayout.length;
        this.width = levelLayout[0].length;
        this.levelName = levelName;

        // Initialise the squares array
        this.squares = new Square[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int squareValue = levelLayout[row][col];
                Square square = createSquareFromValue(squareValue);
                squares[row][col] = square;
            }
        }
    }

    // Create and return the appropriate Square object based on value (this matches GameGridAdapter.java Line 71 switch case)
    private Square createSquareFromValue(int value) {
        switch (value) {
            case 1:
                return new PlayableSquare(Color.BLUE, Shape.CROSS);
            case 2:
                return new PlayableSquare(Color.GREEN, Shape.CROSS);
            case 3:
                return new PlayableSquare(Color.RED, Shape.CROSS);
            case 4:
                return new PlayableSquare(Color.YELLOW, Shape.CROSS);
            case 5:
                return new PlayableSquare(Color.BLUE, Shape.DIAMOND);
            case 6:
                return new PlayableSquare(Color.GREEN, Shape.DIAMOND);
            case 7:
                return new PlayableSquare(Color.RED, Shape.DIAMOND);
            case 8:
                return new PlayableSquare(Color.YELLOW, Shape.DIAMOND);
            case 9:
                return new PlayableSquare(Color.BLUE, Shape.FLOWER);
            case 10:
                return new PlayableSquare(Color.GREEN, Shape.FLOWER);
            case 11:
                return new PlayableSquare(Color.RED, Shape.FLOWER);
            case 12:
                return new PlayableSquare(Color.YELLOW, Shape.FLOWER);
            case 13:
                return new PlayableSquare(Color.BLUE, Shape.STAR);
            case 14:
                return new PlayableSquare(Color.GREEN, Shape.STAR);
            case 15:
                return new PlayableSquare(Color.RED, Shape.STAR);
            case 16:
                return new PlayableSquare(Color.YELLOW, Shape.STAR);
            default:
                return new BlankSquare();
        }
    }

    public String getLevelName() {
        return levelName;
    }

    // Square-related methods

    public int getSquareAt(int row, int col) {
        return levelLayout[row][col];
    }

    public void addSquare(Square square, int row, int column) {
        squares[row][column] = square;
    }

    public Square getSquare(int row, int column) {
        return squares[row][column];
    }

    public Color getColorAt(int row, int column) {
        return getSquare(row, column).getColor();
    }

    public Shape getShapeAt(int row, int column) {
        return getSquare(row, column).getShape();
    }

    // Goal-related methods

    public void addGoal(int row, int column) {
        goals.add(new Position(row, column));
    }

    public boolean hasGoalAt(int row, int column) {
        return goals.stream().anyMatch(goal -> goal.getRow() == row && goal.getColumn() == column);
    }

    public int getGoalCount() {
        return goals.size();
    }

    public int getCompletedGoalCount() {
        return completedGoalCount;
    }

    // Direction/Movement methods

    public boolean isDirectionOK(int row, int column, Eyeball eyeball) {
        int currentRow = eyeball.getRow();
        int currentColumn = eyeball.getColumn();
        Direction currentDirection = eyeball.getDirection();

        // Check for diagonal move
        if (row != currentRow && column != currentColumn) {
            return false;
        }

        // Check if moving backwards based on the current direction
        switch (currentDirection) {
            case UP:
                return row <= currentRow;
            case DOWN:
                return row >= currentRow;
            case LEFT:
                return column <= currentColumn;
            case RIGHT:
                return column >= currentColumn;
            default:
                return false;
        }
    }

    public boolean hasBlankFreePathTo(int row, int column, Eyeball eyeball) {
        int currentRow = eyeball.getRow();
        int currentColumn = eyeball.getColumn();

        if (row == currentRow) {
            // Moving horizontally
            int start = Math.min(currentColumn, column);
            int end = Math.max(currentColumn, column);
            for (int col = start + 1; col < end; col++) {
            	if (getSquare(row, col) instanceof BlankSquare) {
            	    return false;
            	}
            }
        } else if (column == currentColumn) {
            // Moving vertically
            int start = Math.min(currentRow, row);
            int end = Math.max(currentRow, row);
            for (int r = start + 1; r < end; r++) {
                if (getSquare(r, column) instanceof BlankSquare) {
                    return false;
                }
            }
        }

        return true;
    }

    public Message checkDirectionMessage(int row, int column, Eyeball eyeball) {
    	if (!isDirectionOK(row, column, eyeball)) {
    		if (row != eyeball.getRow() && column != eyeball.getColumn()) {
    			return Message.MOVING_DIAGONALLY;
    		} else {
    			return Message.BACKWARDS_MOVE;
    		}
    	}
    	return Message.OK;
    }

    public Message checkMessageForBlankOnPathTo(int row, int column, Eyeball eyeball) {
        if (!hasBlankFreePathTo(row, column, eyeball)) {
            return Message.MOVING_OVER_BLANK;
        }
        return Message.OK;
    }

    public boolean canMoveTo(int row, int column, Eyeball eyeball, Game game) {
        if (!isDirectionOK(row, column, eyeball) || !hasBlankFreePathTo(row, column, eyeball)) {
            return false;
        }

        Square targetSquare = getSquare(row, column);
        Color currentColor = eyeball.getCurrentColor(game);
        Shape currentShape = eyeball.getCurrentShape(game);

        // Check if the target square has the same color or shape as the eyeball's current color or shape
        return targetSquare.getColor() == currentColor || targetSquare.getShape() == currentShape;

    }

    public Message MessageIfMovingTo(int row, int column, Eyeball eyeball, Game game) {
        if (!isDirectionOK(row, column, eyeball)) {
            return checkDirectionMessage(row, column, eyeball);
        }

        if (!hasBlankFreePathTo(row, column, eyeball)) {
            return checkMessageForBlankOnPathTo(row, column, eyeball);
        }

        if (!canMoveTo(row, column, eyeball, game)) {
            return Message.DIFFERENT_SHAPE_OR_COLOR;
        }

        return Message.OK;
    }

    public void moveTo(int row, int column, Eyeball eyeball) {

    	// Replace current square with a BlankSquare
        squares[eyeball.getRow()][eyeball.getColumn()] = new BlankSquare();

        Position currentPosition = new Position(eyeball.getRow(), eyeball.getColumn());
        Position targetPosition = new Position(row, column);

        // If current position is a goal, remove it from the goals set and replace it with BlankSquare
        if (hasGoalAt(eyeball.getRow(), eyeball.getColumn())) {
            squares[eyeball.getRow()][eyeball.getColumn()] = new BlankSquare();
            goals.remove(currentPosition);
        }

        // If the target position is a goal, remove it from goals set and increment the completedGoalCount
        if (hasGoalAt(row, column)) {
            goals.remove(targetPosition);
            completedGoalCount++;
        }

        // Calculate difference between the target row and the eyeball's current row
        int rowDiff = row - eyeball.getRow();
        int colDiff = column - eyeball.getColumn();

        // Update eyeball's direction based on the move direction
        eyeball.setDirection(Math.abs(rowDiff) > Math.abs(colDiff)
        	    ? (rowDiff < 0 ? Direction.UP : Direction.DOWN) // Vertical
        	    : (colDiff < 0 ? Direction.LEFT : Direction.RIGHT)); // Horizontal

        // Update eyeball's position
        eyeball.setPosition(row, column);
    }

    // Getters
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }


    // Created in portoflio version
    public void revertMove(int row, int column, Eyeball eyeball) {
        // Revert game logic that was affected by the move
        // if a goal was reached during the move, undo the goal count
        if (hasGoalAt(row, column)) {
            goals.add(new Position(row, column));
            completedGoalCount--;
        }

        // Restore square at the eyeball's current position
        Square currentSquare = createSquareFromValue(levelLayout[eyeball.getRow()][eyeball.getColumn()]);
        squares[eyeball.getRow()][eyeball.getColumn()] = currentSquare;

        // Reset the eyeball's previous position
        eyeball.setPosition(row, column);
    }
}