package com.example.rhythmtapgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeaderboardActivity extends AppCompatActivity {
    private LinearLayout leaderboardList;
    private ActivityResultLauncher<Intent> leaderboardLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK) {
                        Log.e("Leaderboard", "ActivityResultContracts: " + RESULT_OK);
                    }
                });

        FrameLayout leaderboardButton = findViewById(R.id.leaderboardButton);
        FrameLayout menuButton = findViewById(R.id.playButton);
        FrameLayout storeButton = findViewById(R.id.shopButton);

        leaderboardButton.setOnClickListener(view -> {
            Intent intent = new Intent(LeaderboardActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(LeaderboardActivity.this, MainActivity.class);
            startActivity(intent);
        });

        storeButton.setOnClickListener(view -> {
            // Handle shop button click
        });

        leaderboardList = findViewById(R.id.leaderboard_list);


        fetchLeaderboardData();
        showLeaderboardUI();
    }

    public void fetchLeaderboardData() {
        PlayGames.getLeaderboardsClient(this)
                .loadTopScores(getString(R.string.leaderboard_id),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC,
                        25)
                .addOnSuccessListener(data -> {
                    LeaderboardScoreBuffer scoreBuffer = Objects.requireNonNull(data.get()).getScores();
                    List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

                    for (LeaderboardScore score : scoreBuffer) {
                        leaderboardEntries.add(new LeaderboardEntry(
                                (int) score.getRank(),
                                score.getScoreHolderDisplayName(),
                                score.getRawScore()
                        ));
                    }
                    populateLeaderboard(leaderboardEntries);
                })
                .addOnFailureListener(e -> {
                    Log.e("Leaderboard", "Error loading leaderboard: " + e.getMessage());
                    Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateLeaderboard(List<LeaderboardEntry> leaderboardEntries) {
        for (LeaderboardEntry entry : leaderboardEntries) {
            if (entry.getRank() > 3) {

                View entryView = getLayoutInflater().inflate(R.layout.leaderboard_entry, leaderboardList, false);

                TextView rankView = entryView.findViewById(R.id.rank);
                TextView nameView = entryView.findViewById(R.id.player_name);
                TextView scoreView = entryView.findViewById(R.id.player_score);

                rankView.setText(String.valueOf(entry.getRank()));
                nameView.setText(entry.getPlayerName());
                scoreView.setText(String.valueOf(entry.getScore()));

                leaderboardList.addView(entryView);
            } else {

                populateTopThree(entry);
            }
        }
    }

    private void populateTopThree(LeaderboardEntry entry) {
        switch (entry.getRank()) {
            case 1:
                populateTopThreePlace(entry, R.id.first_place, R.id.shadow1_first_place, R.id.shadow2_first_place, R.id.first_place_score, R.id.shadow1_first_place_score, R.id.shadow2_first_place_score);
                break;
            case 2:
                populateTopThreePlace(entry, R.id.second_place, R.id.shadow1_second_place, R.id.shadow2_second_place, R.id.second_place_score, R.id.shadow1_second_place_score, R.id.shadow2_second_place_score);
                break;
            case 3:
                populateTopThreePlace(entry, R.id.third_place, R.id.shadow1_third_place, R.id.shadow2_third_place, R.id.third_place_score, R.id.shadow1_third_place_score, R.id.shadow2_third_place_score);
                break;
        }
    }

    private void populateTopThreePlace(LeaderboardEntry entry, int placeViewId, int shadow1ViewId, int shadow2ViewId, int scoreViewId, int shadow1ScoreViewId, int shadow2ScoreViewId) {
        TextView placeView = findViewById(placeViewId);
        TextView shadow1View = findViewById(shadow1ViewId);
        TextView shadow2View = findViewById(shadow2ViewId);
        TextView scoreView = findViewById(scoreViewId);
        TextView shadow1ScoreView = findViewById(shadow1ScoreViewId);
        TextView shadow2ScoreView = findViewById(shadow2ScoreViewId);

        placeView.setText(entry.getPlayerName());
        shadow1View.setText(entry.getPlayerName());
        shadow2View.setText(entry.getPlayerName());

        scoreView.setText(String.valueOf(entry.getScore()));
        shadow1ScoreView.setText(String.valueOf(entry.getScore()));
        shadow2ScoreView.setText(String.valueOf(entry.getScore()));
    }


    private void showLeaderboardUI() {
        PlayGames.getLeaderboardsClient(this)
                .getLeaderboardIntent(getString(R.string.leaderboard_id))
                .addOnSuccessListener(intent -> leaderboardLauncher.launch(intent))
                .addOnFailureListener(e -> Log.e("Leaderboard", "Error showing leaderboard: " + e.getMessage()));
    }
}
