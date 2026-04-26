package com.example.gauntershangman;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArenaActivity extends AppCompatActivity {
    int LENGTH;
    int DIFFICULTY;
    String LANGUAGE;

    // Akasztófa szükségletei
    ImageView Gallow;
    int GallowNum = 1;
    List<String> WordPool;
    TextView DispWord;
    StringBuilder ActWordPattern;
    String LastPattern;

    Map<String, List<String>> WordsByPatter = new HashMap<>();


    // Játéktérhez szükségesek
    char[] Alphabet;
    Drawable OrigBackground;
    char SelectedLetter = ' ';
    Button SelectedButton = null;
    Button LetterInput,  SubmitGuess;
    EditText GuessField;
    TextInputLayout GuessFieldFrame;



    @SuppressLint("MissingInflatedId")
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        // Intentek
        LENGTH = getIntent().getIntExtra("WORD_LENGTH", 2);
        DIFFICULTY = getIntent().getIntExtra("DIFFICULTY", 0);
        LANGUAGE = getIntent().getStringExtra("LANGUAGE");
        setAppLocale(LANGUAGE);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_arena);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





        // Gombok beállítása
        LetterInput = findViewById(R.id.letterInputBtn);
        SubmitGuess = findViewById(R.id.guessButton);
        GuessFieldFrame = findViewById(R.id.outlinedTextField);

        LetterInput.setText(getString(R.string.input_choose));
        SubmitGuess.setText(getString(R.string.submit_guess));
        GuessFieldFrame.setHint(getString(R.string.guess_field));

        // Szavak beolvasása a megfelelő file-ból
        WordPool = readWordsFromFile();


        // Akasztófa
        Gallow = findViewById(R.id.gallowTree);
        printGallow();

        DispWord = findViewById(R.id.wordText);
        ActWordPattern = new StringBuilder();

        // Feltöltjük alulhúzásokkal a szó hossza alapján
        for (int i = 0; i < LENGTH; i++) {
            ActWordPattern.append("_");
        }

        // Megjelenítés
        printWord();

        // Betűrács létrehozatala
        GridLayout grid = findViewById(R.id.alphabetGrid);

        if (LANGUAGE.equals("HU")) {
            Alphabet = new char[] {'A', 'Á', 'B', 'C', 'D', 'E', 'É', 'F', 'G', 'H', 'I', 'Í', 'J',
                    'K', 'L', 'M', 'N', 'O', 'Ó', 'Ö', 'Ő',
                    'P', 'Q', 'R', 'S', 'T', 'U', 'Ú', 'Ü', 'Ű', 'V', 'W', 'X', 'Y', 'Z'};
        }
        else
        {
            Alphabet = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J','K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        }


        for (char letter : Alphabet) {
            Button b = new Button(this);
            b.setText(String.valueOf(letter));

            if (OrigBackground == null) {
                OrigBackground = b.getBackground();
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            params.setMargins(5, 5, 5, 5);

            b.setLayoutParams(params);

            // Gombnyomás megadása -> Listener
            b.setOnClickListener(v -> {
                // Ha volt korábban kiválasztott gomb, azt visszaállítjuk alap színre
                if (SelectedButton != null) {
                    SelectedButton.setBackground(OrigBackground.getConstantState().newDrawable());
                    SelectedButton.setScaleX(1.0f);
                    SelectedButton.setScaleY(1.0f);
                    SelectedButton.setZ(0);
                }

                // Kijelölés
                b.setBackgroundColor(Color.YELLOW);
                b.setScaleX(1.1f);
                b.setScaleY(1.1f);
                b.setZ(10);

                // Kijelölt betű és a gomb elmentése
                SelectedLetter = letter;
                SelectedButton = b;
            });

            grid.addView(b);
        }

        // Betűkérés gomb funkciója -> Listener
        LetterInput.setOnClickListener(v -> {
            if (SelectedLetter != ' ') {
                // Akasztófa funkciók
                // Szótár feltöltése
                fillMap(SelectedLetter);

                // Gép választása
                GauntersChoice();


                // Ha az utolsónak tippelt betű kiadja a "keresett" szót, akkor nyert a játékos
                if (WordPool.size() == 1 && ActWordPattern.toString().toUpperCase().equals( WordPool.get(0).toUpperCase() )) {
                    gameOver(true);
                    return;
                }

                // Akasztófa frissítése
                if (LastPattern.equals("_".repeat(LENGTH))) {
                    ++GallowNum;
                    printGallow();
                }
                else {
                    printWord();
                }

                // Ha elfogyott a játékos élete, akkor veszített
                if (GallowNum == 11) {
                    gameOver(false);
                    return;
                }

                // Kiválasztott gomb megváltoztatása
                SelectedButton.setEnabled(false);
                SelectedButton.setBackgroundColor(Color.LTGRAY);
                SelectedButton.setScaleX(0.75f);
                SelectedButton.setScaleY(0.75f);
                SelectedButton.setZ(0);

                SelectedButton = null;
                SelectedLetter = ' ';
            } else {
                // Ha nincs kiválasztott betű
                Toast.makeText(this, getString(R.string.toast_choose), Toast.LENGTH_SHORT).show();
            }
        });


        // Tipp gomb funkciója -> Listener
        GuessField = findViewById(R.id.guessText);
        SubmitGuess.setOnClickListener(v -> {
            String Guess = GuessField.getText().toString();

            if (Guess.equals("")) {
                Toast.makeText(this, getString(R.string.toast_guess_isempty), Toast.LENGTH_SHORT).show();
                return;
            }

            if (Guess.length() != LENGTH) {
                Toast.makeText(this, getString(R.string.toast_guess_length), Toast.LENGTH_SHORT).show();
                return;
            }

            // Legalább 2 elem van még a WordPool-ban -> Vereség
            if (WordPool.size() > 1) {
                Collections.shuffle(WordPool);
                if (Guess.toUpperCase().equals( WordPool.get(0).toUpperCase() )){ gameOver(false, 1, 0); }
                else { gameOver(false, 0,0); }
            }
            // Győzelem
            else if (WordPool.size() == 1 && Guess.toUpperCase().equals( WordPool.get(0).toUpperCase() )) {
                DispWord.setText( Guess.replace("", " ").trim() );
                gameOver(true);
            }
            // Ismeretlen szóra tippelt
            else
            {
                gameOver(false, 0, 1);
            }
        });

    }

    // <--- FÜGGVÉNYEK --->
    // File beolvasása
    private List<String> readWordsFromFile () {
        List<String> words = new ArrayList<>();
        String path;
        if (LANGUAGE.equals("HU")) {
            path = "magyar_szavak/magyar" + LENGTH + ".txt";
        } else {
            path = "angol_szavak/english" + LENGTH + ".txt";
        }

        try {
            InputStream is = getAssets().open(path);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    words.add(trimmedLine.toLowerCase()); // Mindent kisbetűsítünk a könnyebb összehasonlításhoz
                }
            }

            reader.close();
            is.close();

        } catch (IOException e) {
            // Ha nem található a file
            e.printStackTrace();
        }

        return words;
    }


    // Akasztófa függvények
    void printWord() {
        String printing = ActWordPattern.toString().replace("", " ").trim();
        DispWord.setText(printing);
    }

    void printGallow() {
        switch (GallowNum) {
            case 1: Gallow.setImageResource(R.drawable.hangman_1); break;
            case 2: Gallow.setImageResource(R.drawable.hangman_2); break;
            case 3: Gallow.setImageResource(R.drawable.hangman_3); break;
            case 4: Gallow.setImageResource(R.drawable.hangman_4); break;
            case 5: Gallow.setImageResource(R.drawable.hangman_5); break;
            case 6: Gallow.setImageResource(R.drawable.hangman_6); break;
            case 7: Gallow.setImageResource(R.drawable.hangman_7); break;
            case 8: Gallow.setImageResource(R.drawable.hangman_8); break;
            case 9: Gallow.setImageResource(R.drawable.hangman_9); break;
            case 10: Gallow.setImageResource(R.drawable.hangman_10); break;
            default: Gallow.setImageResource(R.drawable.hangman_11); break;
        }
    }

    void fillMap(char Letter) {
        for (String word : WordPool) {
            // Generálunk egy mintát a szóra a tippelt betű alapján
            StringBuilder PatternBuilder = new StringBuilder();
            for (char c : word.toUpperCase().toCharArray()) {
                if (c == Character.toUpperCase(Letter)) {
                    PatternBuilder.append(Letter);
                } else {
                    PatternBuilder.append('_');
                }
            }
            String Pattern = PatternBuilder.toString();

            // Berakjuk a szót a megfelelő csoportba
            if (!WordsByPatter.containsKey(Pattern)) {
                WordsByPatter.put(Pattern, new ArrayList<>());
            }
            WordsByPatter.get(Pattern).add(word);
        }
    }

    void GauntersChoice() {
        if (DIFFICULTY == 3) {
            // MAXIMUM KIVÁLASZTÁS -> Lehetetlen
            Map.Entry<String, List<String>> MaxEntry = null;
            for (Map.Entry<String, List<String>> entry : WordsByPatter.entrySet()) {
                if (MaxEntry == null || entry.getValue().size() > MaxEntry.getValue().size()) {
                    MaxEntry = entry;
                }
            }

            mergePatterns(MaxEntry.getKey());
            WordPool = MaxEntry.getValue();
            LastPattern = MaxEntry.getKey();
            WordsByPatter.clear();
        }
        else {
            int RandomNum = (int)(Math.random() * 10);
            int CurrentSize;
            Map.Entry<String, List<String>> FirstMaxEntry = null;
            Map.Entry<String, List<String>> SecondMaxEntry = null;
            Map.Entry<String, List<String>> ChoosenEntry = null;

            // A legnagyobb és második legnagyobb elemszámú listával rendelekező minták kiválasztása
            for (Map.Entry<String, List<String>> entry : WordsByPatter.entrySet()) {
                CurrentSize = entry.getValue().size();

                // 1. Ha az elem nagyobb, mint az eddigi első
                if (FirstMaxEntry == null || CurrentSize > FirstMaxEntry.getValue().size()) {
                    SecondMaxEntry = FirstMaxEntry;
                    FirstMaxEntry = entry;
                }
                // 2. Ha nem nagyobb az elsőnél, de nagyobb, mint az eddigi második
                else if (SecondMaxEntry == null || CurrentSize > SecondMaxEntry.getValue().size()) {
                    SecondMaxEntry = entry;
                }
            }

            switch (DIFFICULTY) {
                case 0: {
                    if (RandomNum < 8 && SecondMaxEntry != null) {
                        ChoosenEntry = SecondMaxEntry;
                    }
                    else {
                        ChoosenEntry = FirstMaxEntry;
                    }
                    break;
                }
                case 1: {
                    if (RandomNum < 6 && SecondMaxEntry != null) {
                        ChoosenEntry = SecondMaxEntry;
                    }
                    else {
                        ChoosenEntry = FirstMaxEntry;
                    }
                    break;
                }
                case 2: {
                    if (RandomNum < 3 && SecondMaxEntry != null) {
                        ChoosenEntry = SecondMaxEntry;
                    }
                    else {
                        ChoosenEntry = FirstMaxEntry;
                    }
                    break;
                }
            }

            mergePatterns(ChoosenEntry.getKey());
            WordPool = ChoosenEntry.getValue();
            LastPattern = ChoosenEntry.getKey();
            WordsByPatter.clear();
        }
    }

    void mergePatterns(String NewPattern) {
        for (int c = 0; c < LENGTH; ++c) {
            if (NewPattern.charAt(c) != '_') {
                ActWordPattern.setCharAt(c, NewPattern.charAt(c));
            }
        }
    }

    void gameOver(boolean IfWin) {
        gameOver(IfWin, 0, 0);
    }
    void gameOver(boolean IfWin, int index, int SWITCH) {
        LetterInput.setEnabled(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        GuessField.setText("");
        switch (SWITCH) {
            case 0: {
                if (IfWin) {
                    builder.setTitle(getString(R.string.win_title));
                    builder.setMessage(getString(R.string.win_message));
                } else {
                    builder.setTitle(getString(R.string.lose_title));
                    builder.setMessage(getString(R.string.lose_message, WordPool.get(index)));
                }
                break;
            }
            case 1: {
                builder.setTitle(getString(R.string.neutral_title));
                builder.setMessage(getString(R.string.neutral_message, WordPool.get(index)));
                break;
            }
        }

        // Új játék gomb
        builder.setPositiveButton(getString(R.string.new_game), (dialog, which) -> {
            finish();
        });

        // Kilépés gomb
        builder.setNegativeButton(getString(R.string.exit), (dialog, which) -> {
            finishAffinity();
        });

        // Ne lehessen mellékattintással bezárni a döntés előtt
        builder.setCancelable(false);

        builder.show();
    }

    // Nyelvi file beállítása
    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = res.getConfiguration();

        config.setLocale(locale);

        createConfigurationContext(config);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

}