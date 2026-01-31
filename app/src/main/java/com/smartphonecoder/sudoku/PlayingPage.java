package com.smartphonecoder.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;

public class PlayingPage extends Activity {

    String clicked_cell_id, filled_sudoku, time_limit;
    StringBuffer sudoku_puzzle, users_result;
    String sound_effects, mistakes_limit, highlight_areas, highlight_identical_numbers, auto_remove_notes;
    boolean editable;
    boolean restarted = false;
    boolean game_completed = false;
    boolean game_over = false;
    String notes_mode = "OFF";
    String game_state = "resumed";
    int clues, min, sec;
    List<Integer> mistakes = new ArrayList<>(Arrays.asList(0,0));
    List<Integer> hints = new ArrayList<>(Arrays.asList(0,0));
    boolean back_pressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playing_page);

        SharedPreferences settings_data = getSharedPreferences("SudokuGameSettings", MODE_PRIVATE);
        sound_effects = settings_data.getString("SOUND_EFFECTS", "OFF");
        mistakes_limit = settings_data.getString("MISTAKES_LIMIT",null);
        highlight_areas = settings_data.getString("HIGHLIGHT_AREAS",null);
        highlight_identical_numbers = settings_data.getString("HIGHLIGHT_IDENTICAL_NUMBERS",null);
        auto_remove_notes = settings_data.getString("AUTO_REMOVE_NOTES",null);

        filled_sudoku = getsolvedsudoku();
        sudoku_puzzle = new StringBuffer(filled_sudoku);

        new Handler().postDelayed(() -> {

            ViewStub stub = (ViewStub) findViewById(R.id.stub);
            stub.inflate();

            if (mistakes_limit.equals("OFF")){
                TextView mistake_viewer = (TextView) findViewById(R.id.mistakes_viewer);
                mistake_viewer.setVisibility(View.INVISIBLE);
            }
            time_limit = getIntent().getStringExtra("TIME_LIMIT");
            TextView time_viewer = (TextView) findViewById(R.id.time_viewer);
            if (time_limit.equals("disabled")){
                time_viewer.setText("00:00");
                min = 0;
                sec = 0;
            } else {
                if (time_limit == "0"){
                    time_limit = "1";
                }
                min = Integer.parseInt(time_limit);
                sec = 0;
                time_viewer.setText(checktime(Integer.parseInt(time_limit)) + ":00");
            }

            String clues_range = getIntent().getStringExtra("CLUES_RANGE");
            if (clues_range.equals("any_random")){
                clues = (int)Math.floor(Math.random() * 50) + 21;
                mistakes.set(1,((int)Math.floor(Math.random() * 5) + 6));
                hints.set(1,((int)Math.floor(Math.random() * 5) + 3));
            } else {
                int range = Integer.parseInt(clues_range.split("_")[0]);
                clues = (int)Math.floor(Math.random() * 11) + range;

                switch(range){
                    case 60:
                        mistakes.set(1,6); // setting total chances (mistakes)
                        hints.set(1,3); // setting number of hints
                        break;
                    case 50:
                        mistakes.set(1,7);
                        hints.set(1,4);
                        break;
                    case 40:
                        mistakes.set(1,8);
                        hints.set(1,5);
                        break;
                    case 30:
                        mistakes.set(1,9);
                        hints.set(1,6);
                        break;
                    case 20:
                        mistakes.set(1,10);
                        hints.set(1,7);
                        break;
                }
            }
            hints.set(0,hints.get(1));
            TextView clues_viewer = (TextView) findViewById(R.id.clues_viewer);
            TextView mistakes_viewer = (TextView) findViewById(R.id.mistakes_viewer);
            TextView hints_viewer = (TextView) findViewById(R.id.hints_left);
            clues_viewer.setText("Clues: "+String.valueOf(clues));
            mistakes_viewer.setText("Mistakes: 0/"+String.valueOf(mistakes.get(1)));
            hints_viewer.setText(String.valueOf(hints.get(0)));

            LinearLayout sudoku_grid = (LinearLayout) findViewById(R.id.sudoku_grid);
            sudoku_grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    if (Build.VERSION.SDK_INT > 16) {
                        sudoku_grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        sudoku_grid.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    FrameLayout main_page = (FrameLayout) findViewById(R.id.main_playing_page);
                    LinearLayout loading_screen = (LinearLayout) findViewById(R.id.loading_screen);
                    main_page.removeView(loading_screen);

                    int sudoku_grid_width = sudoku_grid.getMeasuredWidth();
                    LinearLayout.LayoutParams grid_param = new LinearLayout.LayoutParams(sudoku_grid_width,sudoku_grid_width);
                    grid_param.setMargins(20,0,0,20);
                    sudoku_grid.setLayoutParams(grid_param); // parent of this layout should also be of same type

                    grid(null);
                }
            });

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;

            LinearLayout layout_above_grid = (LinearLayout) findViewById(R.id.layout_above_grid);
            LinearLayout tool_buttons = (LinearLayout) findViewById(R.id.tool_buttons);
            LinearLayout num_buttons = (LinearLayout) findViewById(R.id.num_buttons);

            LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams)layout_above_grid.getLayoutParams();
            param1.topMargin = (int) height / 100 * 3;
            layout_above_grid.setLayoutParams(param1);

            LinearLayout.LayoutParams param2 = (LinearLayout.LayoutParams)tool_buttons.getLayoutParams();
            param2.topMargin = (int) height / 100 * 12;
            tool_buttons.setLayoutParams(param2);

            LinearLayout.LayoutParams param3 = (LinearLayout.LayoutParams)num_buttons.getLayoutParams();
            param3.topMargin = (int) height / 100 * 5;
            num_buttons.setLayoutParams(param3);

        },100 );

    }

    @Override
    public void onBackPressed(){
        save_game_data("GameToFinish");
        if (sec <= 5 && min == 0){
            SoundManager.stopSound(8);
        }
        game_state = "paused";
        back_pressed = true;
        Intent first_page = new Intent(this, MainActivity.class);
        startActivity(first_page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAfterTransition();
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (!game_completed && !game_over && !back_pressed) {
            pause(null);
        }
        save_game_data("GameDataOnPause");
    }
    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences game_data = getSharedPreferences("GameDataOnPause", MODE_PRIVATE);
        SharedPreferences.Editor editor = game_data.edit();
        editor.putBoolean("CONTAINS_DATA", false);
        editor.apply();
    }

    public void save_game_data(String file_name){
        if (!game_completed){
            SharedPreferences game_data = getSharedPreferences(file_name, MODE_PRIVATE);
            SharedPreferences.Editor editor = game_data.edit();

            if (!game_over) {

                editor.putBoolean("CONTAINS_DATA", true);

                for (int i = 1; i <= 81; i++) {
                    String s = "CELL_" + i;
                    String notes_data = "";
                    for (int j = 1; j <= 9; j++) {
                        String str_notes_tv_id = "cell_" + i + "_cell_" + j;
                        int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                        notes_data += ((TextView) findViewById(res_notes_tv_id)).getText().toString();
                    }
                    editor.putString(s, notes_data);
                }

                editor.putInt("CLUES", clues);
                if (mistakes_limit.equals("ON")) {
                    editor.putString("MISTAKES", mistakes.get(0) + "/" + mistakes.get(1));
                } else {
                    editor.putString("MISTAKES", "OFF");
                }

                editor.putString("TIME_LIMIT", time_limit);
                String result_time = ((TextView) findViewById(R.id.time_viewer)).getText().toString();
                editor.putString("TIME", result_time);

                editor.putString("FILLED_SUDOKU", filled_sudoku);
                editor.putString("SUDOKU_PUZZLE", String.valueOf(sudoku_puzzle));
                editor.putString("USER_RESULT", String.valueOf(users_result));

                editor.putString("HINTS", hints.get(0) + "/" + hints.get(1));

                editor.apply();


            } else {

                editor.putBoolean("CONTAINS_DATA", true);

                for (int i = 1; i <= 81; i++) {
                    String s = "CELL_" + i;
                    editor.putString(s, "");
                }

                editor.putInt("CLUES", clues);
                if (mistakes_limit.equals("ON")) {
                    editor.putString("MISTAKES", "0" + "/" + mistakes.get(1));
                } else {
                    editor.putString("MISTAKES", "OFF");
                }

                editor.putString("TIME_LIMIT", time_limit);
                if (!time_limit.equals("disabled")){
                    editor.putString("TIME", checktime(Integer.parseInt(time_limit))+":00");
                } else {
                    editor.putString("TIME", "00:00");
                }

                editor.putString("FILLED_SUDOKU", filled_sudoku);
                editor.putString("SUDOKU_PUZZLE", String.valueOf(sudoku_puzzle));
                editor.putString("USER_RESULT", String.valueOf(sudoku_puzzle));

                editor.putString("HINTS", hints.get(1) + "/" + hints.get(1));

                editor.apply();
            }
        }
    }

    public void stopwatch(View view){
        TextView time_viewer = (TextView) findViewById(R.id.time_viewer);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                if (game_state.equals("resumed")){
                    if(sec == 60){
                        sec = 0;
                        min++;
                    }
                    time_viewer.setText(checktime(min)+":"+checktime(sec));
                    sec++;
                    handler.postDelayed(this, 1000);
                }
            }
        }, 0);
    }

    public void timer(View view){
        TextView time_viewer = (TextView) findViewById(R.id.time_viewer);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                if (game_state.equals("resumed")){
                    if (sec == 5 && min == 0){
                        SoundManager.playSound(8, sound_effects);
                    }
                    if(sec == -1){
                        if(min == 0){
                            game_state = "paused";
                            game_over = true;
                            add_popup_screen(null, "times_up");
                        }else{
                            min--;
                            sec = 59;
                        }
                    }
                    time_viewer.setText(checktime(min)+":"+checktime(sec));
                    sec--;
                    handler.postDelayed(this, 1000);
                }
            }
        }, 0);
    }

    public String checktime(int time){
        if(time == -1){ // -1 bec it shows -1 when time's out
            return "00";
        }else if(time < 10){
            return "0" + String.valueOf(time);
        }else{
            return String.valueOf(time);
        }
    }

    public void pause(View view){
        game_state = "paused";
        SoundManager.playSound(7, sound_effects);
        ImageView button = (ImageView) findViewById(R.id.pause_and_play_btn);
        button.setImageResource(R.drawable.resume_image_icon);
        add_popup_screen(null, "paused");

        for(int i=1; i<=81; i++){

            int res_fl_id = getResources().getIdentifier(("cell_"+i), "id", getPackageName());
            FrameLayout all_cells = (FrameLayout) findViewById(res_fl_id);
            all_cells.setBackgroundColor(Color.WHITE);
            int res_tv_id = getResources().getIdentifier(("cell_"+i+"_textview"), "id", getPackageName());
            TextView all_textview = (TextView) findViewById(res_tv_id);
            all_textview.setTextColor(Color.WHITE);

            for (int j=1; j<=9; j++){
                int res_notes_tv_id = getResources().getIdentifier(("cell_"+i+"_cell_"+j), "id", getPackageName());
                TextView all_notes_textview = (TextView) findViewById(res_notes_tv_id);
                all_notes_textview.setTextColor(Color.WHITE);
            }
        }
    }

    public void resume(View view){
        game_state = "resumed";
        SoundManager.playSound(7, sound_effects);
        ImageView button = (ImageView) findViewById(R.id.pause_and_play_btn);
        button.setImageResource(R.drawable.pause_image_icon);
        if (clicked_cell_id != null){
            highlight(null, clicked_cell_id);
        }

        for(int i=1; i<=81; i++){

            int res_tv_id = getResources().getIdentifier(("cell_"+i+"_textview"), "id", getPackageName());
            TextView all_textview = (TextView) findViewById(res_tv_id);

            String tv_data = all_textview.getText().toString();

            if(tv_data != ""){
                if (sudoku_puzzle.charAt(i-1) == '0'){
                    if (String.valueOf(filled_sudoku.charAt(i-1)).equals(tv_data)){
                        all_textview.setTextColor(Color.parseColor("#406aff"));
                    } else {
                        all_textview.setTextColor(Color.RED);
                    }
                } else {
                    all_textview.setTextColor(Color.BLACK);
                }
            }


            for (int j=1; j<=9; j++){
                int res_notes_tv_id = getResources().getIdentifier(("cell_"+i+"_cell_"+j), "id", getPackageName());
                TextView all_notes_textview = (TextView) findViewById(res_notes_tv_id);
                all_notes_textview.setTextColor(Color.BLACK);
            }
        }

        remove_popup_screen(null, "paused");


        new Handler().postDelayed(new Runnable() {
            public void run(){
                if (time_limit.equals("disabled")){
                    stopwatch(null);
                } else {
                    timer(null);
                }
            }
        }, 1000);

    }

    public void restart(View view){

        ((LinearLayout) findViewById(R.id.transparent_screen)).setVisibility(View.VISIBLE);
        SoundManager.playSound(7, sound_effects);
        mistakes.set(0,0);
        hints.set(0,hints.get(1));
        TextView mistakes_viewer = (TextView) findViewById(R.id.mistakes_viewer);
        TextView hints_viewer = (TextView) findViewById(R.id.hints_left);
        mistakes_viewer.setText("Mistakes: 0/"+String.valueOf(mistakes.get(1)));
        hints_viewer.setText(String.valueOf(hints.get(0)));

        game_state = "resumed";
        TextView time_viewer = (TextView) findViewById(R.id.time_viewer);
        if (time_limit.equals("disabled")){
            time_viewer.setText("00:00");
            min = 0;
            sec = 0;
        } else {
            if (time_limit == "0"){
                time_limit = "1";
            }
            min = Integer.parseInt(time_limit);
            sec = 0;
            time_viewer.setText(checktime(Integer.parseInt(time_limit)) + ":00");
        }

        for(int i=1; i<=81; i++){

            int res_fl_id = getResources().getIdentifier(("cell_"+i), "id", getPackageName());
            ((FrameLayout) findViewById(res_fl_id)).setBackgroundColor(Color.WHITE);

            int res_tv_id = getResources().getIdentifier(("cell_"+i+"_textview"), "id", getPackageName());
            TextView all_textview = (TextView) findViewById(res_tv_id);
            all_textview.setText("");

            for (int j=1; j<=9; j++){
                int res_notes_tv_id = getResources().getIdentifier(("cell_"+i+"_cell_"+j), "id", getPackageName());
                TextView all_notes_textview = (TextView) findViewById(res_notes_tv_id);
                all_notes_textview.setText("");
                all_notes_textview.setTextColor(Color.BLACK);
            }
        }

        for (int i=1; i<=9; i++){
            String str_popup_notes_tv_id = "notes_cell_"+ String.valueOf(i);
            int res_popup_notes_tv_id = getResources().getIdentifier(str_popup_notes_tv_id, "id", getPackageName());
            TextView popup_notes_tv = (TextView) findViewById(res_popup_notes_tv_id);
            popup_notes_tv.setText("");
            popup_notes_tv.setBackgroundColor(Color.WHITE);
        }

        game_over = false;
        clicked_cell_id = null;
        editable = false;
        notes_mode = "OFF";

        ((TextView) findViewById(R.id.notes_symbol)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.notes_text)).setTextColor(Color.BLACK);

        ((LinearLayout) findViewById(R.id.notes_data_eraser)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.paused)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.times_up)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.game_over)).setVisibility(View.GONE);

        ((FrameLayout) findViewById(R.id.popup_screen)).setVisibility(View.GONE);

        ((ImageView) findViewById(R.id.pause_and_play_btn)).setImageResource(R.drawable.pause_image_icon);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        restarted = true;

        grid(null);

    }

    public void new_game(View view){
        SoundManager.playSound(7, sound_effects);
        Intent first_page = new Intent(this, CustomisationPage.class);
        startActivity(first_page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finishAfterTransition();
    }

    int cell_id_num = 0;
    public void grid(View view){

        if (restarted == false){
            for (int i=1; i<=(81-clues); ){
                int random_index = (int)Math.floor(Math.random() * 81);
                if (sudoku_puzzle.charAt(random_index) != '0'){
                    sudoku_puzzle.setCharAt(random_index, '0');
                    i++;
                }
            }
        }

        users_result = new StringBuffer(sudoku_puzzle);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                for(int i=0; i<9; i++){
                    cell_id_num++;
                    if(cell_id_num <= 81){
                        String str_cell_id = "cell_"+cell_id_num;
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#d4dae6"));
                    }
                }

                if ((cell_id_num-17) > 0 && cell_id_num < 99){
                    for(int i=0; i<9; i++){
                        String str_cell_id = "cell_"+((cell_id_num - 17) + i);
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#b3bfd7"));
                    }
                }

                if ((cell_id_num-26) > 0 && cell_id_num < 108){
                    for(int i=0; i<9; i++){
                        String str_tv_id = "cell_"+((cell_id_num - 26) + i)+"_textview"; // tv = textview
                        int res_tv_id = getResources().getIdentifier(str_tv_id, "id", getPackageName());
                        TextView textview = (TextView) findViewById(res_tv_id);
                        if(sudoku_puzzle.charAt(((cell_id_num-26)+i)-1) != '0'){
                            textview.setText(String.valueOf(sudoku_puzzle.charAt(((cell_id_num-26)+i)-1)));
                            textview.setTextColor(Color.BLACK);
                        }
                        if(highlight_identical_numbers.equals("ON")){
                            final int num = (cell_id_num-26)+i;
                            textview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String cell_id = "cell_"+num;
                                    highlight(null, cell_id);
                                }
                            });
                        } else {
                            if(sudoku_puzzle.charAt(((cell_id_num-26)+i)-1) == '0'){
                                final int num = (cell_id_num-26)+i;
                                textview.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String cell_id = "cell_"+num;
                                        highlight(null, cell_id);
                                    }
                                });
                            }
                        }
                    }
                }

                if ((cell_id_num-35) > 0 && cell_id_num < 117){
                    for(int i=0; i<9; i++){
                        String str_cell_id = "cell_"+((cell_id_num - 35) + i);
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#c5cedf"));
                    }
                }

                if ((cell_id_num-44) > 0 && cell_id_num < 126){
                    for(int i=0; i<9; i++){
                        String str_cell_id = "cell_"+((cell_id_num - 44) + i);
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#cbd5e1"));
                    }
                }

                if ((cell_id_num-53) > 0 && cell_id_num < 135){
                    for(int i=0; i<9; i++){
                        String str_cell_id = "cell_"+((cell_id_num - 53) + i);
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#dbe2ea"));
                    }
                }

                if ((cell_id_num-71) > 0){
                    for(int i=0; i<9; i++){
                        String str_cell_id = "cell_"+((cell_id_num - 71) + i);
                        int res_cell_id = getResources().getIdentifier(str_cell_id, "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_cell_id);
                        cell.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
                if(cell_id_num < 144){
                    handler.postDelayed(this, 80);
                }
                else if(cell_id_num >= 144) {
                    LinearLayout transparen_screen = (LinearLayout) findViewById(R.id.transparent_screen);
                    transparen_screen.setVisibility(View.GONE);
                    cell_id_num *= 0;

                    restarted = false;

                    if (time_limit.equals("disabled")){
                        stopwatch(null);
                    } else {
                        timer(null);
                    }
                }
            }
        }, 500);

    }

    public String getsolvedsudoku(){
        final String sudoku1 = "483921657967345821251876493548132976729564138136798245372689514814253769695417382";
        final String sudoku2 = "123765849456289713789413256972346185361852497548197362634571928215938674897624531";
        final String sudoku3 = "156729834839564721742318965921643578485172396673895142568437219394281657217956483";
        final String sudoku4 = "291384576854167392763952814629473185135698247478215963982546731347821659516739428";
        final String sudoku5 = "162397854859146732473852916624781593738925461591463278286539147947618325315274689";
        final String sudoku6 = "469187253123459876857632941315974682248563719796821435682715394534298167971346528";

        HashMap<String, String> all_arrays = new HashMap<>();
        all_arrays.put("sudoku1", sudoku1);
        all_arrays.put("sudoku2", sudoku2);
        all_arrays.put("sudoku3", sudoku3);
        all_arrays.put("sudoku4", sudoku4);
        all_arrays.put("sudoku5", sudoku5);
        all_arrays.put("sudoku6", sudoku6);

        Object[] all_arrays_Keys = all_arrays.keySet().toArray();
        Object random_key = all_arrays_Keys[new Random().nextInt(all_arrays_Keys.length)];

        String random_sudoku = all_arrays.get(random_key);
        System.out.println(random_sudoku);

        ArrayList<String> row_wise = new ArrayList();

        for(int i=0; i<81; i+=9){
            row_wise.add(random_sudoku.substring(i,i+9));
        }

        int number_of_rotation = (int)Math.floor(Math.random() * 4); // rotating sudoku

        if (number_of_rotation > 0){
            for(int i=0; i<number_of_rotation; i++){
                row_wise = rotate(row_wise);
            }
        }

        return TextUtils.join("",row_wise);
    }
    public static ArrayList rotate(ArrayList<String> row_wise){
        ArrayList<String> rotated_array = new ArrayList();

        for(int i=0; i<9; i++){
            String each_row = "";
            for(int j=8; j>-1; j--){
                each_row += String.valueOf(row_wise.get(j).charAt(i));
            }
            rotated_array.add(each_row);
        }
        return rotated_array;
    }

    public void highlight(View view, String cell_id){

        if (sudoku_puzzle.charAt(Integer.parseInt(cell_id.split("_")[1])-1) == '0'){
            editable = true;
        } else {
            editable = false;
        }
        clicked_cell_id = cell_id;



        for(int i=1; i<=81; i++){ // resetting all cells background color to white
            int res_fl_id = getResources().getIdentifier(("cell_"+i), "id", getPackageName()); // fl = framelayout
            FrameLayout all_cells = (FrameLayout) findViewById(res_fl_id);
            all_cells.setBackgroundColor(Color.WHITE);
        }

        if (highlight_areas.equals("ON") && editable == true){
            DataStorage cell_group_data = new DataStorage();
            HashMap<String, String> cell_groups = cell_group_data.getCellGroup();

            String[] clicked_cell_groups = cell_groups.get(cell_id).split(" ");

            for(Map.Entry mapElement: cell_groups.entrySet()){
                String key = (String)mapElement.getKey();
                String value = (String)mapElement.getValue();

                if(value.contains(clicked_cell_groups[0]) || value.contains(clicked_cell_groups[1]) || value.contains(clicked_cell_groups[2])){
                    int res_fl_id = getResources().getIdentifier(key, "id", getPackageName()); // fl = framelayout
                    FrameLayout group_cells = (FrameLayout) findViewById(res_fl_id);
                    group_cells.setBackgroundColor(Color.parseColor("#e0eaf4"));
                }
            }
        }
        if (highlight_identical_numbers.equals("ON")){
            int res_tv_id = getResources().getIdentifier((cell_id+"_textview"), "id", getPackageName());
            TextView selected_tv = (TextView) findViewById(res_tv_id);
            String selected_tv_value = selected_tv.getText().toString();

            if(!selected_tv_value.equals("")){
                for (int i=1; i<=81; i++){
                    int res_tv_id_1 = getResources().getIdentifier(("cell_"+i+"_textview"), "id", getPackageName());
                    TextView all_textView = (TextView) findViewById(res_tv_id_1);
                    if (all_textView.getText().toString().equals(selected_tv_value)){
                        int res_fl_id = getResources().getIdentifier(("cell_"+i), "id", getPackageName());
                        FrameLayout cell = (FrameLayout) findViewById(res_fl_id);
                        cell.setBackgroundColor(Color.parseColor("#fff6b3"));
                    }
                }
            }
        }

        if (editable == true){
            int res_fl_id_1 = getResources().getIdentifier(clicked_cell_id, "id", getPackageName()); // fl = framelayout
            FrameLayout selected_cell = (FrameLayout) findViewById(res_fl_id_1);
            selected_cell.setBackgroundColor(Color.parseColor("#fff6b3"));
        }
    }

    public void change_notes_mode(View view){
        TextView symbol = (TextView) findViewById(R.id.notes_symbol);
        TextView text = (TextView) findViewById(R.id.notes_text);
        SoundManager.playSound(3, sound_effects);
        if (notes_mode == "OFF"){
            notes_mode = "ON";
            symbol.setTextColor(Color.parseColor("#406aff"));
            text.setTextColor(Color.parseColor("#406aff"));
        }
        else if (notes_mode == "ON"){
            notes_mode = "OFF";
            symbol.setTextColor(Color.BLACK);
            text.setTextColor(Color.BLACK);
        }
    }

    public void erase(View view){
        if (clicked_cell_id != null){

            String notes_data = "";
            for (int i=1; i<=9; i++){
                String str_notes_tv_id = clicked_cell_id+"_cell_"+i;
                int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                notes_data += notes_tv.getText().toString();
            }
            if (notes_data != ""){
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                ((LinearLayout) findViewById(R.id.notes_data_eraser)).setVisibility(View.VISIBLE);

                for(int i=0; i<notes_data.length(); i++){
                    String str_popup_notes_tv_id = "notes_cell_"+ String.valueOf(notes_data.charAt(i));
                    int res_popup_notes_tv_id = getResources().getIdentifier(str_popup_notes_tv_id, "id", getPackageName());
                    TextView popup_notes_tv = (TextView) findViewById(res_popup_notes_tv_id);
                    popup_notes_tv.setText(String.valueOf(notes_data.charAt(i)));
                }

                ArrayList<String> cells = new ArrayList<>();

                for (int i=1; i<=9; i++){
                    String str_popup_notes_tv_id = "notes_cell_"+ String.valueOf(i);
                    int res_popup_notes_tv_id = getResources().getIdentifier(str_popup_notes_tv_id, "id", getPackageName());
                    TextView popup_notes_tv = (TextView) findViewById(res_popup_notes_tv_id);
                    popup_notes_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String tag = String.valueOf(view.getTag());
                            if (!cells.contains(tag)){
                                cells.add(tag);
                                view.setBackgroundColor(Color.parseColor("#fff6b3"));
                            } else {
                                cells.remove(new String(tag));
                                view.setBackgroundColor(Color.WHITE);
                            }
                        }
                    });
                }
                ImageView checked_box = (ImageView) findViewById(R.id.checked_box);
                checked_box.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cells.size() > 0){
                            SoundManager.playSound(5, sound_effects);
                        }
                        for (int i=0; i<cells.size(); i++){
                            String str_notes_tv_id = clicked_cell_id + "_" + cells.get(i);
                            int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                            TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                            notes_tv.setText("");
                        }
                        for (int i=1; i<=9; i++){
                            String str_popup_notes_tv_id = "notes_cell_"+ String.valueOf(i);
                            int res_popup_notes_tv_id = getResources().getIdentifier(str_popup_notes_tv_id, "id", getPackageName());
                            TextView popup_notes_tv = (TextView) findViewById(res_popup_notes_tv_id);
                            popup_notes_tv.setText("");
                            popup_notes_tv.setBackgroundColor(Color.WHITE);
                        }
                        getWindow().setStatusBarColor(Color.WHITE);
                        getWindow().setNavigationBarColor(Color.WHITE);
                        ((LinearLayout) findViewById(R.id.notes_data_eraser)).setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    public void delete_cell_data(View view){
        if(editable == true && clicked_cell_id != null){
            SoundManager.playSound(6, sound_effects);
            for (int i=1; i<=9; i++){
                String str_notes_tv_id = clicked_cell_id+"_cell_"+i;
                int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                notes_tv.setText("");
            }
            int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
            TextView selected_cell_tv = (TextView) findViewById(res_tv_id);
            selected_cell_tv.setText("");
            int index = Integer.parseInt(clicked_cell_id.split("_")[1]);
            users_result.setCharAt(index-1, '0');
            highlight(null, clicked_cell_id);
        }
    }

    public void provide_hint(View view){
        if(clicked_cell_id != null && hints.get(0) > 0){

            int index = Integer.parseInt(clicked_cell_id.split("_")[1]);
            int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
            TextView selected_cell_tv = (TextView) findViewById(res_tv_id);

            if (!selected_cell_tv.getText().toString().equals(String.valueOf(filled_sudoku.charAt(index-1)))){

                for (int i=1; i<=9; i++){
                    String str_notes_tv_id = clicked_cell_id+"_cell_"+i;
                    int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                    TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                    notes_tv.setText("");
                }

                hints.set(0,(hints.get(0)-1));
                selected_cell_tv.setText(String.valueOf(filled_sudoku.charAt(index-1)));
                selected_cell_tv.setTextColor(Color.parseColor("#406aff"));
                TextView hints_left = (TextView) findViewById(R.id.hints_left);
                hints_left.setText(String.valueOf(hints.get(0)));

                users_result.setCharAt(index-1, filled_sudoku.charAt(index-1));
                if(String.valueOf(users_result).equals(filled_sudoku)){
                    game_completed = true;
                    game_finished(null);
                } else {
                    SoundManager.playSound(1, sound_effects);
                    highlight(null,clicked_cell_id);
                }
            }
        }
    }

    public void add_digit(View view){
        if(editable == true && clicked_cell_id != null){
            String num_to_add = String.valueOf(view.getTag());
            if (notes_mode.equals("OFF")){
                if (auto_remove_notes.equals("ON")){
                    for (int i=1; i<=9; i++){
                        String str_notes_tv_id = clicked_cell_id+"_cell_"+i;
                        int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                        TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                        notes_tv.setText("");
                    }
                    add_num(null,num_to_add);

                } else {
                    String notes_data = "";
                    for (int i=1; i<=9; i++){
                        String str_notes_tv_id = clicked_cell_id+"_cell_"+i;
                        int res_notes_tv_id = getResources().getIdentifier(str_notes_tv_id, "id", getPackageName());
                        TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                        notes_data += notes_tv.getText().toString();
                    }
                    if (notes_data == ""){
                        add_num(null,num_to_add);
                    } else {
                        Toast.makeText(getApplicationContext(),"Please remove notes first",Toast.LENGTH_LONG).show();
                    }
                }

            } else {
                SoundManager.playSound(4, sound_effects);
                int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
                TextView selected_cell_tv = (TextView) findViewById(res_tv_id);
                selected_cell_tv.setText("");
                int index = Integer.parseInt(clicked_cell_id.split("_")[1]);
                users_result.setCharAt(index-1, '0');
                int res_notes_tv_id = getResources().getIdentifier((clicked_cell_id+"_cell_"+num_to_add), "id", getPackageName());
                TextView notes_tv = (TextView) findViewById(res_notes_tv_id);
                notes_tv.setText(num_to_add);
                highlight(null, clicked_cell_id);
            }
        }
    }
    public void add_num(View view, String num_to_add){
        int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
        TextView selected_cell_tv = findViewById(res_tv_id);
        int index = Integer.parseInt(clicked_cell_id.split("_")[1]);
        if (!selected_cell_tv.getText().toString().equals(num_to_add)) {
            selected_cell_tv.setText(num_to_add);
            users_result.setCharAt(index - 1, num_to_add.charAt(0));
            if (String.valueOf(users_result).equals(filled_sudoku)) {
                game_completed = true;
                game_finished(null);
            } else {
                check_user_input(null, num_to_add);
                highlight(null, clicked_cell_id);
            }
        }
    }


    public void check_user_input(View view, String num_to_add){

        int index = Integer.parseInt(clicked_cell_id.split("_")[1]);
        int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
        TextView selected_cell_tv = findViewById(res_tv_id);

        if (String.valueOf(filled_sudoku.charAt(index-1)).equals(num_to_add)){
            SoundManager.playSound(1, sound_effects);
            selected_cell_tv.setTextColor(Color.parseColor("#406aff"));
        } else {
            SoundManager.playSound(2, sound_effects);
            selected_cell_tv.setTextColor(Color.RED);
            if (mistakes_limit.equals("ON")){
                mistakes.set(0,(mistakes.get(0)+1));
                TextView mistake_viewer = (TextView) findViewById(R.id.mistakes_viewer);
                mistake_viewer.setText("Mistakes: "+mistakes.get(0)+"/"+mistakes.get(1));
                if (mistakes.get(0) >= mistakes.get(1)){
                    game_state = "paused";
                    game_over = true;
                    add_popup_screen(null, "game_over");
                }
            }
        }
    }

    public void add_popup_screen(View view, String popup_box_id){

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        if (popup_box_id.equals("game_over") || popup_box_id.equals("times_up")){
            SoundManager.playSound(10, sound_effects);
        }

        FrameLayout popup_screen = (FrameLayout) findViewById(R.id.popup_screen);
        popup_screen.setVisibility(View.VISIBLE);

        int res_ll_id = getResources().getIdentifier(popup_box_id, "id", getPackageName()); // ll = LinearLayout
        LinearLayout popup_box = (LinearLayout) findViewById(res_ll_id);
        popup_box.setVisibility(View.VISIBLE);

    }
    public void remove_popup_screen(View view, String popup_box_id){

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        int res_ll_id = getResources().getIdentifier(popup_box_id, "id", getPackageName()); // ll = LinearLayout
        LinearLayout popup_box = (LinearLayout) findViewById(res_ll_id);
        popup_box.setVisibility(View.GONE);

        FrameLayout popup_screen = (FrameLayout) findViewById(R.id.popup_screen);
        popup_screen.setVisibility(View.GONE);

    }

    int i = 0; // i = index
    public void game_finished(View view){
        findViewById(R.id.transparent_screen).setVisibility(View.VISIBLE);
        game_state = "paused";

        int res_tv_id = getResources().getIdentifier(clicked_cell_id+"_textview", "id", getPackageName());
        ((TextView) findViewById(res_tv_id)).setTextColor(Color.parseColor("#406aff"));

        SoundManager.playSound(9, sound_effects);

        for (int i=1; i<=81; i++){
            int res_fl_id = getResources().getIdentifier(("cell_"+i), "id", getPackageName());
            findViewById(res_fl_id).setBackgroundColor(Color.WHITE);
        }

        int[][] for_animation = {{41}, {31, 32, 33, 40, 42, 49, 50, 51}, {21, 22, 23, 24, 25, 30, 34, 39, 43, 48, 52, 57, 58, 59, 60, 61}, {11, 12, 13, 14, 15, 16, 17, 20, 26, 29, 35, 38, 44, 47, 53, 56, 62, 65, 66, 67, 68, 69, 70, 71}, {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 18, 19, 27, 28, 36, 37, 45, 46, 54, 55, 63, 64, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81}};

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                if (i < 5){
                    for (int j = 0; j < for_animation[i].length; j++){
                        String cell_id = "cell_" + for_animation[i][j];
                        int res_tv_id = getResources().getIdentifier(cell_id, "id", getPackageName());
                        findViewById(res_tv_id).setBackgroundColor(Color.parseColor("#e7eaf2"));
                    }
                }
                if (i > 0 && i < 6){
                    for (int j = 0; j < for_animation[i-1].length; j++){
                        String cell_id = "cell_" + for_animation[i-1][j];
                        int res_tv_id = getResources().getIdentifier(cell_id, "id", getPackageName());
                        findViewById(res_tv_id).setBackgroundColor(Color.parseColor("#b5c2d9"));
                    }
                }
                if (i > 1 && i < 7){
                    for (int j = 0; j < for_animation[i-2].length; j++){
                        String cell_id = "cell_" + for_animation[i-2][j];
                        int res_tv_id = getResources().getIdentifier(cell_id, "id", getPackageName());
                        findViewById(res_tv_id).setBackgroundColor(Color.parseColor("#dfe2ed"));
                    }
                }
                if (i > 2 && i < 8){
                    for (int j = 0; j < for_animation[i-3].length; j++){
                        String cell_id = "cell_" + for_animation[i-3][j];
                        int res_tv_id = getResources().getIdentifier(cell_id, "id", getPackageName());
                        findViewById(res_tv_id).setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }

                if (i < 8){
                    i++;
                    handler.postDelayed(this, 100);
                } else {
                    Intent result_page = new Intent(getApplicationContext(), ResultPage.class);
                    result_page.putExtra("CLUES",clues);

                    if (mistakes_limit.equals("ON")) {
                        result_page.putExtra("MISTAKES", String.valueOf(mistakes.get(0) + "/" + mistakes.get(1)));
                    } else {
                        result_page.putExtra("MISTAKES", "disabled");
                    }
                    result_page.putExtra("HINTS",String.valueOf((hints.get(1) - hints.get(0))+"/"+hints.get(1)));
                    TextView result_time_tv = findViewById(R.id.time_viewer);
                    String result_time = result_time_tv.getText().toString();
                    if (time_limit.equals("disabled")){
                        result_page.putExtra("TIME",result_time);
                    } else {
                        int min1 = Integer.parseInt(result_time.split(":")[0]);
                        int sec1 = Integer.parseInt(result_time.split(":")[1]);
                        int min2 = (Integer.parseInt(time_limit) - 1) - min1;
                        String sec2 = checktime(60 - sec1);
                        result_page.putExtra("TIME",String.valueOf(min2+":"+sec2)+" / "+time_limit+":"+"00");
                    }
                    startActivity(result_page);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finishAfterTransition();
                }
            }
        }, 120);

    }

}