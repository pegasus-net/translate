package com.icarus.words.engine.entity;

import com.icarus.words.data.Word;

import java.util.ArrayList;

public class Words {
    public ArrayList<ArrayList<String>> data;

    public ArrayList<Word> getWords() {
        ArrayList<Word> list = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            for (ArrayList<String> word : data) {
                if (word != null && word.size() >= 3) {
                    list.add(new Word(word.get(0), word.get(1), Integer.parseInt(word.get(2))));
                }
            }
        }
        return list;
    }
}
