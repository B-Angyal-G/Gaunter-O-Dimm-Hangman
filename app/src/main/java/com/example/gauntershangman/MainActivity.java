package com.example.gauntershangman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Számválasztó
    NumberPicker inputLength;

    // Nehézségi szint spinner
    Spinner difficultySpinner;

    String[] levelsHU = {"Könnyű", "Közepes", "Nehéz", "Lehetetlen"};
    String[] levelsEN = {"Easy", "Medium", "Hard", "Impossible"};

    List<String> currentLevels = new ArrayList<>(Arrays.asList(levelsHU));
    ArrayAdapter<String> adapter;


    // Nyelvválasztó
    String language = "HU";
    RadioGroup langGroup;
    RadioButton enRadio, huRadio;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Számválasztó beállítása
        inputLength = findViewById(R.id.lengthNumberPicker);
        inputLength.setMinValue(2);
        inputLength.setMaxValue(20);

        // Nehézségi szint
        difficultySpinner = findViewById(R.id.difficultySpinner);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, levelsHu);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        difficultySpinner.setAdapter(adapter);

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("Lehetetlen")) {
                        Toast.makeText(MainActivity.this, "Tényleg lehetetlen!", Toast.LENGTH_SHORT).show();
                } else if (selectedItem.equals("Impossible")) {
                    Toast.makeText(MainActivity.this, "It's truly impossible!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Nyelvválasztás figyelése
        langGroup = findViewById(R.id.langGroup);
        huRadio = findViewById(R.id.huLang);
        enRadio = findViewById(R.id.enLang);

        TextView langText = findViewById(R.id.langText);
        TextView lengthText = findViewById(R.id.textLength);
        TextView difficultyText = findViewById(R.id.textDifficulty);
        Button startButton = findViewById(R.id.start);

        langGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                currentLevels.clear();

                if (checkedId == huRadio.getId())
                {
                    language = "HU";
                    langText.setText("Nyelv");
                    huRadio.setText("Magyar");
                    enRadio.setText("Angol");
                    lengthText.setText("Milyen hosszú szóra gondoljak?");
                    difficultyText.setText("Nehézségi szint");
                    currentLevels.addAll(Arrays.asList(levelsHU));
                    startButton.setText("Indítás");
                }
                else
                {
                    language = "EN";
                    langText.setText("Language");
                    huRadio.setText("Hungarian");
                    enRadio.setText("English");
                    lengthText.setText("How long word shall I think of?");
                    difficultyText.setText("Difficulty level");
                    currentLevels.addAll(Arrays.asList(levelsEN));
                    startButton.setText("Start");
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void startGame(View view) {
        int length = 4;
        int difficultyValue = 1;

        // Szóhossz lekérdezése
        length = inputLength.getValue();

        // Szóhossz átadása a játéktérnek
        Intent intent = new Intent(MainActivity.this, ArenaActivity.class);
        intent.putExtra("WORD_LENGTH", length);

        // Nehézségi szint
        String selected = difficultySpinner.getSelectedItem().toString();
        if (language.equals("HU")) {
            switch (selected) {
                case "Könnyű":
                    difficultyValue = 0;
                    break;
                case "Közepes":
                    difficultyValue = 1;
                    break;
                case "Nehéz":
                    difficultyValue = 2;
                    break;
                case "Lehetetlen":
                    difficultyValue = 3;
                    break;
            }
        }
        else
        {
            switch (selected) {
                case "Easy":     difficultyValue = 0; break;
                case "Medium":    difficultyValue = 1; break;
                case "Hard":      difficultyValue = 2; break;
                case "Impossible": difficultyValue = 3; break;
            }
        }

        intent.putExtra("DIFFICULTY", difficultyValue);
        intent.putExtra("LANGUAGE", language);
        startActivity(intent);
    }
}