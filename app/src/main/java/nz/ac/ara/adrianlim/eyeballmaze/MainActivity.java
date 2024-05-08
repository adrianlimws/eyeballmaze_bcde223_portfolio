package nz.ac.ara.adrianlim.eyeballmaze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import nz.ac.ara.adrianlim.eyeballmaze.enums.Direction;
import nz.ac.ara.adrianlim.eyeballmaze.enums.Message;
import nz.ac.ara.adrianlim.eyeballmaze.models.Game;
import nz.ac.ara.adrianlim.eyeballmaze.models.Level;

public class MainActivity extends AppCompatActivity {
    private Game game;
    private Level level;
    private TextView levelNameTextView;
    private TextView dialogTextView;

    private TextView moveCountTextView;
    private TextView goalCountTextView;
    private int moveCount = 0;
    private int initialGoalCount;

    private MediaPlayer legalMoveSound;
    private MediaPlayer illegalMoveSound;
    private MediaPlayer goalReachedSound;
    private MediaPlayer gameOverSound;
    private boolean isSoundOn = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sound effects
        legalMoveSound = MediaPlayer.create(this, R.raw.legal_move_sound);
        illegalMoveSound = MediaPlayer.create(this, R.raw.illegal_move_sound);
        goalReachedSound = MediaPlayer.create(this, R.raw.goal_reached_sound);
        gameOverSound = MediaPlayer.create(this, R.raw.game_over_sound);

        // level data display
        levelNameTextView = findViewById(R.id.text_maze_level);
        moveCountTextView = findViewById(R.id.text_move_count);
        goalCountTextView = findViewById(R.id.text_goal_count);

        // rules dialog textview
        dialogTextView = findViewById(R.id.text_rule_dialog);
        dialogTextView.setText("Select a tile to make a move");

        // Define the layout of the game level using the numeric value found in GameGridAdapter.java (line 71)
        int[][] levelLayout = {
                {0, 0, 11, 0},
                {1, 12, 8, 2},
                {10, 15, 14, 8},
                {11, 9, 15, 10},
                {13, 7, 9, 5},
                {0, 5, 0, 6}
        };

        game = new Game(); // create new Game obj
        game.addLevel("Level 1",levelLayout); // add specific levelName, levelLayout
        game.addGoal(0, 2); // add position of the goal
        game.addEyeball(5, 1, Direction.UP); // add position of the eyeball and its facing direction

        // Store the initial goal count
        initialGoalCount = game.getGoalCount();
        goalCountTextView.setText("Goal: 0/" + initialGoalCount);
        // create a GameGridAdapter to populate the GridView with the game data
        GameGridAdapter gameGridAdapter = new GameGridAdapter(this, game);
        // Locate the GridView in layout/activity_main.xml
        GridView gridView = findViewById(R.id.grid_game_level);
        // set GameGridAdapter as the GridView adapter
        gridView.setAdapter(gameGridAdapter);

        updateLevelName();

        // Set onclick listener for GridView
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Calculate the row and column based on the tapped position by player
                int tappedRow = position / game.getLevelWidth();
                int tappedCol = position % game.getLevelWidth();

                // Check if tapping the same current eyeball position
                if (tappedRow == game.getEyeballRow() && tappedCol == game.getEyeballColumn()) {
                    dialogTextView.setText("You are already here");
                    if (isSoundOn) {
                        illegalMoveSound.start();
                    }
                    return;
                }

                // Check if the tapped position is a valid move
                if (game.canMoveTo(tappedRow, tappedCol)) {
                    // Move to the tapped position
                    game.moveTo(tappedRow, tappedCol);
                    // play move sound
                    if (isSoundOn) {
                        legalMoveSound.start();
                    }
                    // Increment the move count per legal move
                    moveCount++;
                    moveCountTextView.setText("Moves: " + moveCount);

                    // notifyDataSetChanged ()
                    // Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
                    // Refresh the grid
                    gameGridAdapter.notifyDataSetChanged();

                    Log.d("EyeballMaze", "Game Goal count: " + game.getGoalCount());
                    Log.d("EyeballMaze", "Completed Goal Count: " + game.getCompletedGoalCount());

                    int currentGoalCount = game.getGoalCount();

                    // if all goals are completed
                    if (game.getGoalCount() == 0) {
                        showGameOverDialog(true);
                        if (isSoundOn) {
                            goalReachedSound.start();
                        }
                    } else if (!game.hasLegalMoves()) {
                        showGameOverDialog(false);
                        if (isSoundOn) {
                            gameOverSound.start();
                        }
                    }

                    // Update the goal count TextView
                    goalCountTextView.setText("Goal: " + game.getCompletedGoalCount() + "/" + initialGoalCount);
                } else {
                    // Play the illegal move sound
                    if (isSoundOn) {
                        illegalMoveSound.start();
                    }
                    Message message = game.MessageIfMovingTo(tappedRow, tappedCol);
                    showInvalidMoveMessage(message);
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_sound) {
                    isSoundOn = !isSoundOn;

                    // Update the sound icon based on the state
                    if (isSoundOn) {
                        item.setIcon(R.drawable.icon_sound_on);
                    } else {
                        item.setIcon(R.drawable.icon_sound_off);
                    }
                    return true;
                } else if (itemId == R.id.action_undo) {
                    // Handle undo action
                    return true;
                } else if (itemId == R.id.action_pause) {
                    // Handle pause game action
                    return true;
                } else if (itemId == R.id.action_load_save) {
                    // Handle load/save game logic
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release sound resources
        legalMoveSound.release();
        illegalMoveSound.release();
        goalReachedSound.release();
        gameOverSound.release();
    }

    private void updateLevelName() {
        String levelName = game.getCurrentLevelName();
        levelNameTextView.setText(levelName);
    }

    private void showGameOverDialog(boolean isWin) {
        String title = isWin ? "Congratulations!" : "Game Over";
        String message = isWin ? "You have completed the level!" : "You lost as there are no legal moves to make.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Quit Game", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
                        confirmBuilder.setTitle("Confirm Quit")
                                .setMessage("Are you sure you want to quit the game?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface confirmDialog, int confirmId) {
                                        finish();
                                    }
                                })
                                .setNegativeButton("Restart Level", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface confirmDialog, int confirmId) {
                                        recreate();
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // restart level
                        recreate();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showInvalidMoveMessage(Message message) {
        String messageText = "";
        switch (message) {
            case MOVING_DIAGONALLY:
                messageText = "Cannot move diagonally";
                break;
            case BACKWARDS_MOVE:
                messageText = "Cannot move backwards";
                break;
            case MOVING_OVER_BLANK:
                messageText = "Cannot move over blank squares";
                break;
            case DIFFERENT_SHAPE_OR_COLOR:
                messageText = "Can only move to a square with the same color or shape";
                break;
        }
        dialogTextView.setText(messageText);
    }

}