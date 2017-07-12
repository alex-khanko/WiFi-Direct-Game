package com.chelsenok.game.preferences;

import android.content.Context;
import android.content.SharedPreferences;

class SettingsSharedPreferences {

    private static final String TABLE_NAME = "settings";
    private static final String NAME = "name";
    private final SharedPreferences mPreferences;

    public SettingsSharedPreferences(final Context context) {
        mPreferences = context.getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE);
    }

    public void setName(final String s) {
        mPreferences.edit().putString(NAME, s).apply();
    }

    public String getName(final String defaultValue) {
        return mPreferences.getString(NAME, defaultValue);
    }
}
