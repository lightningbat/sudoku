package com.smartphonecoder.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class CustomisationPage extends Activity {

    String clues_range = "";
    int height;
    int time_limit = 5;
    String sound_effects;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customisation_page);

        SharedPreferences settings_data = getSharedPreferences("SudokuGameSettings", MODE_PRIVATE);
        sound_effects = settings_data.getString("SOUND_EFFECTS", "OFF");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;

        TextView text_above_cluesSelector = (TextView) findViewById(R.id.text_above_CluesSelector);
        ImageView play_button = (ImageView) findViewById(R.id.play_button);

        LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams)text_above_cluesSelector.getLayoutParams();
        param1.topMargin = (int) height / 100 * 12;
        text_above_cluesSelector.setLayoutParams(param1);

        LinearLayout.LayoutParams param2 = (LinearLayout.LayoutParams)play_button.getLayoutParams();
        param2.topMargin = (int) height / 100 * 8;
        play_button.setLayoutParams(param2);

        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        TextView seekbar_result = (TextView) findViewById(R.id.seekBar_result);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                seekbar_result.setText(progressValue + " min");
                time_limit = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //pass
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //pass
            }

        });

    }

    @Override
    public void onBackPressed(){
        Intent first_page = new Intent(this, MainActivity.class);
        startActivity(first_page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAfterTransition();
    }

    public void game_settings_page(View view){
        SoundManager.playSound(7, sound_effects);
        Intent game_settings_page = new Intent(this, GameSettingsPage.class);
        game_settings_page.putExtra("FROM", "CP");
        startActivity(game_settings_page);
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        finishAfterTransition();
    }

    public void clues(View view){
        SoundManager.playSound(11, sound_effects);
        if(clues_range != ""){
            int res_tv_id = getResources().getIdentifier(("R_"+clues_range), "id", getPackageName());
            TextView textview = (TextView) findViewById(res_tv_id);
            textview.setTextColor(Color.parseColor("#8a8990"));
        }
        clues_range = String.valueOf(view.getTag());
        int res_tv_id = getResources().getIdentifier(("R_" + clues_range), "id", getPackageName());
        TextView textview = (TextView) findViewById(res_tv_id);
        textview.setTextColor(Color.parseColor("#7daefd"));

    }

    public void time_limit(View view){
        SoundManager.playSound(11, sound_effects);
        LinearLayout time_limit_setter = (LinearLayout) findViewById(R.id.time_limit_setter);
        Switch aSwitch = (Switch) findViewById(R.id.a_switch);

        if(aSwitch.isChecked()){
            time_limit_setter.setVisibility(View.VISIBLE);
        } else {
            time_limit_setter.setVisibility(View.INVISIBLE);
        }
    }
    public void play(View view){
        if (clues_range != ""){
            ImageView play_button = (ImageView) findViewById(R.id.play_button);
            LinearLayout.LayoutParams btn_param = new LinearLayout.LayoutParams(play_button.getMeasuredWidth()-20,play_button.getMeasuredHeight()-20);
            btn_param.gravity = Gravity.CENTER_HORIZONTAL;
            btn_param.topMargin = (int) height / 100 * 8;
            play_button.setLayoutParams(btn_param);

            new Handler().postDelayed(() -> {
                SoundManager.playSound(7, sound_effects);
                LinearLayout.LayoutParams btn_param_1 = new LinearLayout.LayoutParams(play_button.getMeasuredWidth()+20,play_button.getMeasuredHeight()+20);
                btn_param_1.gravity = Gravity.CENTER_HORIZONTAL;
                btn_param_1.topMargin = (int) height / 100 * 8;
                play_button.setLayoutParams(btn_param_1);

                Switch aSwitch = (Switch) findViewById(R.id.a_switch);

                Intent playing_page = new Intent(this, PlayingPage.class);
                playing_page.putExtra("CLUES_RANGE",clues_range);
                if (aSwitch.isChecked()){
                    playing_page.putExtra("TIME_LIMIT",String.valueOf(time_limit));
                } else {
                    playing_page.putExtra("TIME_LIMIT","disabled");
                }
                startActivity(playing_page);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finishAfterTransition();

            },100);
        } else{
            Toast.makeText(getApplicationContext(),"Please select Number of Clues first",Toast.LENGTH_LONG).show();
        }
    }
}