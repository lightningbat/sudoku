package com.smartphonecoder.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GameSettingsPage extends Activity {

    private SharedPreferences settings_data;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_settings_page);

        settings_data = getSharedPreferences("SudokuGameSettings", MODE_PRIVATE);
        editor = settings_data.edit();

        String[] key = {"SOUND_EFFECTS","MISTAKES_LIMIT","HIGHLIGHT_AREAS","HIGHLIGHT_IDENTICAL_NUMBERS","AUTO_REMOVE_NOTES"};
        for (int i=0; i<5; i++){
            String key_value = settings_data.getString(key[i], "file doesn't exists");
            int res_tv_id = getResources().getIdentifier(key[i].toLowerCase(), "id", getPackageName());
            TextView button = (TextView) findViewById(res_tv_id);

            if (key_value.equals("ON")){

                button.setText("ON");
                button.setTextColor(Color.parseColor("#5899f7"));
                GradientDrawable btn_background = (GradientDrawable)button.getBackground();
                btn_background.setStroke(2, Color.parseColor("#5899f7"));
            }
            else if (key_value.equals("OFF")){
                button.setText("OFF");
                button.setTextColor(Color.parseColor("#7c7c7c"));
                GradientDrawable btn_background = (GradientDrawable)button.getBackground();
                btn_background.setStroke(2, Color.parseColor("#7c7c7c"));
            }
            else{
                Toast.makeText(getApplicationContext(), "Please Restart the app", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent last_page;
        String from = getIntent().getStringExtra("FROM");
        if (from.equals("MA")){
            last_page = new Intent(this, MainActivity.class);
        } else {
            last_page = new Intent(this, CustomisationPage.class);
        }
        startActivity(last_page);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        //finishAffinity();
        finishAfterTransition();
    }


    public void change(View view ){
        String tv_id = String.valueOf(view.getTag());
        String key_value = settings_data.getString(tv_id.toUpperCase(), "file doesn't exists");
        int res_tv_id = getResources().getIdentifier(tv_id, "id", getPackageName());
        TextView button = (TextView) findViewById(res_tv_id);

        if (key_value.equals("ON")){

            editor.putString(tv_id.toUpperCase(),"OFF");
            editor.apply();

            button.setText("OFF");
            button.setTextColor(Color.parseColor("#7c7c7c"));
            GradientDrawable btn_background = (GradientDrawable)button.getBackground();
            btn_background.setStroke(2, Color.parseColor("#7c7c7c"));
        }
        else if (key_value.equals("OFF")){

            editor.putString(tv_id.toUpperCase(),"ON");
            editor.apply();

            button.setText("ON");
            button.setTextColor(Color.parseColor("#5899f7"));
            GradientDrawable btn_background = (GradientDrawable)button.getBackground();
            btn_background.setStroke(2, Color.parseColor("#5899f7"));
        }
        else{
            Toast.makeText(getApplicationContext(), "Please Restart the app", Toast.LENGTH_LONG).show();
        }

        String sound_effects = settings_data.getString("SOUND_EFFECTS", "OFF");
        SoundManager.playSound(11, sound_effects);
    }
}