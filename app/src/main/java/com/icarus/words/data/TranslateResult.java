package com.icarus.words.data;

import org.litepal.crud.LitePalSupport;

public class TranslateResult  extends LitePalSupport {
    public int type;
    public String src;
    public String dst;

    public TranslateResult(int type, String src, String dst) {
        this.type = type;
        this.src = src;
        this.dst = dst;
    }
}
