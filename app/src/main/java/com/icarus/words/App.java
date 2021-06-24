package com.icarus.words;

import com.icarus.words.engine.TranslateEngine;

import org.litepal.LitePal;

import a.icarus.component.MonitorApplication;

public class App extends MonitorApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        TranslateEngine.init(this, getString(R.string.TRANSLATE_ID), getString(R.string.TRANSLATE_KEY));
        TranslateEngine.setMode(TranslateEngine.ENGLISH, TranslateEngine.CHINESE);
    }
}
