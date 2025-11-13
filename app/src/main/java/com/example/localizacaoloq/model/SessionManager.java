package com.example.localizacaoloq.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LocalizacaoLoqPrefs";
    private static final String KEY_SESSION_ID = "sessionId";
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public void saveSession(String sessionId) {
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.apply();
    }
    public String getSessionId() {
        return preferences.getString(KEY_SESSION_ID, null);
    }
    public void clearSession() {
        editor.remove(KEY_SESSION_ID);
        editor.apply();
    }
}
