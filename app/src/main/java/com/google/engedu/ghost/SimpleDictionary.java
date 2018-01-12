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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class SimpleDictionary implements GhostDictionary {
    private ArrayList<String> words;

    public SimpleDictionary(InputStream wordListStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(wordListStream));
        words = new ArrayList<>();
        String line = null;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() >= MIN_WORD_LENGTH)
              if (word.length() >= 4) words.add(line.trim());
        }
    }

    @Override
    public boolean isWord(String word) {
        return words.contains(word.toLowerCase());
    }

    @Override
    public String getAnyWordStartingWith(String prefix) throws NoSuchElementException {
        ArrayList<String> potential = prefixWords(prefix);
        if (potential.size() == 0) return null;
        return potential.get(0);
    }

    private ArrayList<String> prefixWords(String prefix) {
        ArrayList<String> results = new ArrayList();
        if (words.size() == 0) return results;
        if (prefix.length() == 0) return words;

        int lo = 0;
        int mid = words.size();
        int hi = mid - 1;

        int currentLow = -1;

        // Find the first occurence of the prefix
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            mid = (lo + hi) / 2;
            String midWord = words.get(mid);
            int comparison = prefix.compareTo(midWord.substring(0, Math.min(prefix.length(), midWord.length())));

            if (comparison == 0) {
                currentLow = mid;
                hi = mid - 1;
            } else if (comparison < 0) {
                hi = mid - 1;
            } else if (comparison > 0) {
                lo = mid + 1;
                mid = lo;
            }
        }
        if (currentLow == -1) return results;

        while (prefix.compareTo(words.get(currentLow).substring(0, Math.min(prefix.length(), words.get(currentLow).length()))) == 0) {
            results.add(words.get(currentLow));
            currentLow += 1;
        }
        return results;
    }


    @Override
    public String getGoodWordStartingWith(String prefix) {
        ArrayList<String> potential = prefixWords(prefix);
        if (potential.size() == 0) return null;
        return potential.get(potential.size() - 1);
    }
}
