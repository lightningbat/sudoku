package com.smartphonecoder.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class ResultPage extends Activity {
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page);

        getWindow().setStatusBarColor(Color.parseColor("#fffcf3"));
        getWindow().setNavigationBarColor(Color.parseColor("#fffcf3"));

        int clues = getIntent().getIntExtra("CLUES", 0);
        ((TextView) findViewById(R.id.clues_viewer)).setText(String.valueOf(clues));

        String mistakes = getIntent().getStringExtra("MISTAKES");
        if (!mistakes.equals("disabled")) {
            ((TextView) findViewById(R.id.mistakes_viewer)).setText(mistakes);
        }
        String time = getIntent().getStringExtra("TIME");
        ((TextView) findViewById(R.id.time_viewer)).setText(time);

        String hints = getIntent().getStringExtra("HINTS");
        ((TextView) findViewById(R.id.hints_viewer)).setText(hints);

        String[] results = {"clues", "mistakes", "time", "hints", "horizontal_line", "new_game"};
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (results[index].equals("mistakes") && mistakes.equals("disabled")) {
                    findViewById(R.id.mistakes).setVisibility(View.GONE);
                } else {
                    int res_ll_id = getResources().getIdentifier(results[index], "id", getPackageName());
                    findViewById(res_ll_id).setVisibility(View.VISIBLE);
                }
                index++;
                if (index < 6){
                    handler.postDelayed(this, 300);
                }
            }
        }, 1500);
    }

    @Override
    public void onBackPressed(){
        Intent first_page = new Intent(this, MainActivity.class);
        startActivity(first_page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAfterTransition();
    }

    public void new_game(View view){
        MediaPlayer.create(this, R.raw.other_buttons).start();
        Intent first_page = new Intent(this, CustomisationPage.class);
        startActivity(first_page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAfterTransition();
    }
}