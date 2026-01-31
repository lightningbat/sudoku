package com.smartphonecoder.sudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;;

public class MainActivity extends Activity {
    String sound_effects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SoundManager.getInstance();
        SoundManager.initSounds(this);
        SoundManager.loadSounds();

        SharedPreferences settings_data = getSharedPreferences("SudokuGameSettings", MODE_PRIVATE);
        sound_effects = settings_data.getString("SOUND_EFFECTS", "file doesn't exists");

        if(sound_effects.equals("file doesn't exists")){
            SharedPreferences.Editor editor = settings_data.edit();
            editor.putString("SOUND_EFFECTS","ON");
            editor.putString("MISTAKES_LIMIT","ON");
            editor.putString("HIGHLIGHT_AREAS","ON");
            editor.putString("HIGHLIGHT_IDENTICAL_NUMBERS","ON");
            editor.putString("AUTO_REMOVE_NOTES","ON");
            editor.apply();
        }
        sound_effects = settings_data.getString("SOUND_EFFECTS", "OFF");

        SharedPreferences paused_game = getSharedPreferences("GameDataOnPause", MODE_PRIVATE);
        boolean paused_game_data = paused_game.getBoolean("CONTAINS_DATA", false);
        if (paused_game_data){
            SharedPreferences game_data = getSharedPreferences("GameToFinish", MODE_PRIVATE);
            SharedPreferences.Editor editor = game_data.edit();

            editor.putBoolean("CONTAINS_DATA", true);

            for (int i = 1; i <= 81; i++) {
                String s = "CELL_" + i;
                String notes_data = paused_game.getString(s, "");
                editor.putString(s, notes_data);
            }

            int clues = paused_game.getInt("CLUES", 0);
            editor.putInt("CLUES", clues);

            String mistakes = paused_game.getString("MISTAKES", null);
            editor.putString("MISTAKES", mistakes);

            String time_limit = paused_game.getString("TIME_LIMIT", null);
            editor.putString("TIME_LIMIT", time_limit);
            String time = paused_game.getString("TIME", null);
            editor.putString("TIME", time);

            String filled_sudoku = paused_game.getString("FILLED_SUDOKU", null);
            editor.putString("FILLED_SUDOKU", filled_sudoku);

            String sudoku_puzzle = paused_game.getString("SUDOKU_PUZZLE", null);
            editor.putString("SUDOKU_PUZZLE", sudoku_puzzle);

            String users_result = paused_game.getString("USER_RESULT", null);
            editor.putString("USER_RESULT", users_result);

            String hints = paused_game.getString("HINTS", null);
            editor.putString("HINTS", hints);

            editor.apply();

            SharedPreferences.Editor editor1 = paused_game.edit();
            editor1.putBoolean("CONTAINS_DATA", false);
            editor1.apply();
        }

        SharedPreferences game_to_finish = getSharedPreferences("GameToFinish", MODE_PRIVATE);
        boolean game_data = game_to_finish.getBoolean("CONTAINS_DATA", false);
        if (game_data){
            findViewById(R.id.cont_btn).setVisibility(View.VISIBLE);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        ImageView sudoku_icon = (ImageView) findViewById(R.id.sudoku_icon);
        Button new_game_btn = (Button) findViewById(R.id.new_game);
        Button cont_btn = (Button) findViewById(R.id.cont_btn);

        LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams)sudoku_icon.getLayoutParams();
        param1.topMargin = (int) height / 100 * 10;
        sudoku_icon.setLayoutParams(param1);

        LinearLayout.LayoutParams param2 = (LinearLayout.LayoutParams)new_game_btn.getLayoutParams();
        param2.topMargin = (int) height / 100 * 30;
        new_game_btn.setLayoutParams(param2);

        LinearLayout.LayoutParams param3 = (LinearLayout.LayoutParams)cont_btn.getLayoutParams();
        param3.topMargin = (int) height / 100 * 3;
        cont_btn.setLayoutParams(param3);

    }

    public void game_settings_page(View view){
        SoundManager.playSound(7, sound_effects);
        Intent game_settings_page = new Intent(this, GameSettingsPage.class);
        game_settings_page.putExtra("FROM", "MA");
        startActivity(game_settings_page);
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        finishAfterTransition();
    }

    public void new_game(View view){
        SoundManager.playSound(7, sound_effects);
        Button new_game_btn = (Button) findViewById(R.id.new_game);
        new_game_btn.setTextColor(Color.WHITE);
        new Handler().postDelayed(() -> {
            new_game_btn.setTextColor(Color.parseColor("#7daefd"));
            Intent settings_page = new Intent(this, CustomisationPage.class);
            startActivity(settings_page);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finishAfterTransition();
        }, 300);
    }

    public void load_left_game(View view){
        SoundManager.playSound(7, sound_effects);
        Button cont_btn = (Button) findViewById(R.id.cont_btn);
        cont_btn.setTextColor(Color.WHITE);
        new Handler().postDelayed(() -> {
            cont_btn.setTextColor(Color.parseColor("#7daefd"));
            Intent settings_page = new Intent(this, Unfinished_Game.class);
            startActivity(settings_page);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finishAfterTransition();
        }, 300);
    }



    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}