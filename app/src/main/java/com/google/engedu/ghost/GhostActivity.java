/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.ghost;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class GhostActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";

    String alphabet = "abcdefghijklmnopqrstuvwxyz";

    private GhostDictionary dictionary;

    private boolean userTurn = false;
    private String fragment = "";
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);
        AssetManager assetManager = getAssets();
        try {
            dictionary = new SimpleDictionary(assetManager.open("words.txt"));
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, R.string.dictionay_load_fail, Toast.LENGTH_LONG);
            toast.show();
        }

        String test = getString(R.string.dictionay_load_fail);

        onStart(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String fragment = ((TextView)findViewById(R.id.ghostText)).getText().toString();
        outState.putString("fragment", fragment);
        outState.putBoolean("userTurn", userTurn);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            fragment = savedInstanceState.getString("fragment", fragment);
            userTurn = savedInstanceState.getBoolean("userTurn", userTurn);
        }

        onStart(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    public boolean onStart(View view) {
        userTurn = random.nextBoolean();
        TextView text = (TextView) findViewById(R.id.ghostText);
        text.setText(fragment);
        TextView label = (TextView) findViewById(R.id.gameStatus);
        if (userTurn) {
            label.setText(USER_TURN);
        } else {
            label.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void forceUserRestart(String message) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("New game?", this);
        dlg.show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        onStart(null);
    }

    private void computerTurn() {
        TextView label = (TextView) findViewById(R.id.gameStatus);
        TextView ghostText = (TextView) findViewById(R.id.ghostText);
        if (onChallenge(null)) return;
        String fragment = ghostText.getText().toString();

        if (fragment.length() == 0) {
            // The computer got the first turn
            ghostText.setText(String.valueOf(alphabet.charAt(random.nextInt(26))));
        } else {
            String prefixed = dictionary.getAnyWordStartingWith(fragment);
            String newFragment = prefixed.substring(0, fragment.length() + 1);
            ghostText.setText(newFragment);
        }

        userTurn = true;
        label.setText(USER_TURN);
    }

    public boolean onChallenge(View view) {
        String fragment = ((TextView) findViewById(R.id.ghostText)).getText().toString();
        String prefixed = dictionary.getAnyWordStartingWith(fragment);
        if (userTurn) {
            if (dictionary.isWord(fragment)) {
                forceUserRestart("The computer wrote a word! You win!");
                return true;
            } else if (prefixed == null) {
                // This will never happen...
                forceUserRestart("The computer's entry isn't a word prefix! You win!");
                return true;
            } else {
                forceUserRestart("Bad challenge! You lose. Ha. Ha. Ha. Ha.");
                return true;
            }
        } else {
            if (dictionary.isWord(fragment)) {
                forceUserRestart("That's a word! You lose. Ha. Ha. Ha. Ha.");
                return true;
            } else if (prefixed == null) {
                forceUserRestart("That isn't a word prefix! You lose. Ha. Ha. Ha. Ha.");
                return true;
            }
        }
        return false;
    }

    /**
     * Handler for user key presses.
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (Character.isLetter(event.getUnicodeChar())) {
            TextView ghostText = (TextView) findViewById(R.id.ghostText);

            String fragment = ghostText.getText().toString() +
                    Character.toString(event.getDisplayLabel());

            ghostText.setText(fragment.toLowerCase());

            userTurn = false;
            computerTurn();
        } else {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }
}
