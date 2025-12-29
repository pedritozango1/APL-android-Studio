package com.example.localizacaoloq.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConfigDBHelper extends SQLiteOpenHelper {

    // Informações do banco
    public static final String DATABASE_NAME = "configs.db";
    public static final int DATABASE_VERSION = 2; // Versão aumentada para adicionar user_id

    // Tabela
    public static final String TABLE_CONFIG = "configuracoes";

    // Colunas
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MODO_MULA = "modo_mula";
    public static final String COLUMN_WIFI_DIRECT = "wifi_direct";
    public static final String COLUMN_NOTIFICACOES = "notificacoes";

    // SQL para criar tabela (VERSÃO 2 com user_id)
    private static final String CREATE_TABLE_V2 =
            "CREATE TABLE " + TABLE_CONFIG + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT NOT NULL, " +
                    COLUMN_MODO_MULA + " INTEGER DEFAULT 0, " +
                    COLUMN_WIFI_DIRECT + " INTEGER DEFAULT 0, " +
                    COLUMN_NOTIFICACOES + " INTEGER DEFAULT 1, " +
                    "UNIQUE(" + COLUMN_USER_ID + "))";

    public ConfigDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Cria tabela versão 2
        db.execSQL(CREATE_TABLE_V2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migrar da versão 1 para 2
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIG);
            onCreate(db);
        }
    }

    // Método para verificar se tabela existe
    public static boolean tabelaExiste(SQLiteDatabase db, String tableName) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (android.database.sqlite.SQLiteStatement stmt = db.compileStatement(query)) {
            stmt.bindString(1, tableName);
            return stmt.simpleQueryForLong() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}