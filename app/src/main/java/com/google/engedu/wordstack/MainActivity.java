package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static int wordLength = 3;
    private final int MAX_WORD_LENGTH = 6;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Set wordsSet = new HashSet();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<View> placedTiles = new Stack<>();

    private LinearLayout word1LinearLayout;
    private LinearLayout word2LinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() >= wordLength) {
                    words.add(word);
                    wordsSet.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        word1LinearLayout = (LinearLayout) findViewById(R.id.word1LinearLayout);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        word2LinearLayout = (LinearLayout) findViewById(R.id.word2LinearLayout);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    if (isAWinner(word1, word2)) {
                        messageBox.setText("YOU WIN");
                    } else {
                        messageBox.setText("LOSER, the correct answers are " + word1 + " " + word2);
                    }
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);

                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);

                        if (isAWinner(word1, word2)) {
                            messageBox.setText(R.string.winner);
                            if (wordLength <= MAX_WORD_LENGTH) {
                                wordLength++;
                            }
                        } else {
                            messageBox.setText(getString(R.string.loser) + word1 + " " + word2);
                        }
                    }
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        do {
            word1 = words.get(random.nextInt(words.size() + 1));
        } while (word1.length() != wordLength);

        do {
            word2 = words.get(random.nextInt(words.size() + 1));
        } while (word1.equals(word2) || word2.length() != wordLength);

        Log.d("MainActivity", word1);
        Log.d("MainActivity", word2);

        int word1Counter = 0;
        int word2Counter = 0;
        String scrambledWord = "";

        while (word1Counter < word1.length() && word2Counter < word2.length()) {
            boolean wordNumber = random.nextBoolean(); //word1 = true, word2 = false
            if (wordNumber) {
                scrambledWord = scrambledWord + word1.charAt(word1Counter++);
            } else {
                scrambledWord = scrambledWord + word2.charAt(word2Counter++);
            }
        }

        if (word1Counter < word1.length()) {
            scrambledWord += word1.substring(word1Counter);
        } else {
            scrambledWord += word2.substring(word2Counter);
        }

        for (char letter : new StringBuilder(scrambledWord).reverse().toString().toCharArray()) {
            stackedLayout.push(new LetterTile(this, letter));
        }

        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            LetterTile poppedTile = (LetterTile) placedTiles.pop();
            poppedTile.moveToViewGroup(stackedLayout);
        }
        return true;
    }

    public boolean isAWinner(String correctWord1, String correctWord2) {
        boolean word1IsCorrect = false;
        boolean word2IsCorrect = false;

        String word1 = getPlacedTiles(word1LinearLayout);
        String word2 = getPlacedTiles(word2LinearLayout);

        word1IsCorrect = (word1.equals(correctWord1) || word1.equals(correctWord2) || wordsSet.contains(word1));
        word2IsCorrect = (word2.equals(correctWord1) || word2.equals(correctWord2) || wordsSet.contains(word2));

        return (word1IsCorrect && word2IsCorrect);
    }

    public String getPlacedTiles(LinearLayout wordLinearLayout) {
        String word = "";
        for (int i = 0 ; i < wordLinearLayout.getChildCount(); i++) {
            LetterTile tile = (LetterTile) wordLinearLayout.getChildAt(i);
            word += tile.getText();
        }
        return word;
    }
}
