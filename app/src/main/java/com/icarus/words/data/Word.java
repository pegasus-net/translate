package com.icarus.words.data;

import org.litepal.crud.LitePalSupport;

public class Word extends LitePalSupport {
    public String en;
    public String ch;
    public int level;
    public int state;

    public Word(String en, String ch, int level) {
        this.en = en == null ? "" : en;
        this.ch = ch == null ? "" : ch;
        this.level = level;
    }
}
