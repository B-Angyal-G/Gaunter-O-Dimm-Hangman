package com.example.gauntershangman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Számválasztó
    NumberPicker inputLength;
    Spinner difficultySpinner;


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
        String[] levels = {"Könnyű", "Közepes", "Nehéz", "Lehetetlen"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("Lehetetlen")) {
                    Toast.makeText(MainActivity.this, "Tényleg lehetetlen!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        switch (selected) {
            case "Könnyű":     difficultyValue = 0; break;
            case "Közepes":    difficultyValue = 1; break;
            case "Nehéz":      difficultyValue = 2; break;
            case "Lehetetlen": difficultyValue = 3; break;
        }
        intent.putExtra("DIFFICULTY", difficultyValue);
        startActivity(intent);
    }
}