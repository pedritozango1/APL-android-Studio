package com.example.localizacaoloq.Repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.localizacaoloq.database.ConfigDBHelper;
import com.example.localizacaoloq.model.Config;

public class ConfigRepository {

    private ConfigDBHelper dbHelper;
    private static final String TAG = "ConfigRepository";

    public ConfigRepository(Context context) {
        dbHelper = new ConfigDBHelper(context);
    }

    // Fecha conexão
    public void close() {
        dbHelper.close();
    }

    // ============= MÉTODOS DE SELECT =============

    // SELECT para buscar configurações por usuário
    public Config carregarConfiguracoesPorUsuario(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + ConfigDBHelper.TABLE_CONFIG +
                    " WHERE " + ConfigDBHelper.COLUMN_USER_ID + " = ? LIMIT 1";
            cursor = db.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                return cursorParaConfig(cursor);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar configurações: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return null;
    }

    // SELECT específico para cada configuração por usuário
    public boolean getModoMula(String userId) {
        Config config = carregarConfiguracoesPorUsuario(userId);
        return config != null && config.isModoMula();
    }

    public boolean getWifiDirect(String userId) {
        Config config = carregarConfiguracoesPorUsuario(userId);
        return config != null && config.isWifiDirect();
    }

    public boolean getNotificacoes(String userId) {
        Config config = carregarConfiguracoesPorUsuario(userId);
        return config != null && config.isNotificacoes();
    }

    // ============= MÉTODOS DE UPDATE =============

    // UPDATE para atualizar Modo Mula por usuário
    public boolean atualizarModoMula(String userId, boolean ativo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ConfigDBHelper.COLUMN_MODO_MULA, ativo ? 1 : 0);

            int rows = db.update(
                    ConfigDBHelper.TABLE_CONFIG,
                    values,
                    ConfigDBHelper.COLUMN_USER_ID + " = ?",
                    new String[]{userId}
            );

            Log.d(TAG, "Modo Mula atualizado para usuário " + userId + ": " + ativo);
            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar Modo Mula: " + e.getMessage());
            return false;
        }
    }

    // UPDATE para atualizar WiFi-Direct por usuário
    public boolean atualizarWifiDirect(String userId, boolean ativo) {
        SQLiteDatabase db = dbHelper .getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ConfigDBHelper.COLUMN_WIFI_DIRECT, ativo ? 1 : 0);

            int rows = db.update(
                    ConfigDBHelper.TABLE_CONFIG,
                    values,
                    ConfigDBHelper.COLUMN_USER_ID + " = ?",
                    new String[]{userId}
            );

            Log.d(TAG, "WiFi-Direct atualizado para usuário " + userId + ": " + ativo);
            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar WiFi-Direct: " + e.getMessage());
            return false;
        }
    }

    // UPDATE para atualizar Notificações por usuário
    public boolean atualizarNotificacoes(String userId, boolean ativo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ConfigDBHelper.COLUMN_NOTIFICACOES, ativo ? 1 : 0);

            int rows = db.update(
                    ConfigDBHelper.TABLE_CONFIG,
                    values,
                    ConfigDBHelper.COLUMN_USER_ID + " = ?",
                    new String[]{userId}
            );

            Log.d(TAG, "Notificações atualizadas para usuário " + userId + ": " + ativo);
            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar Notificações: " + e.getMessage());
            return false;
        }
    }

    // ============= MÉTODO DE INSERT =============

    // INSERT para criar configurações padrão para um usuário
    public boolean criarConfiguracoesPadraoUsuario(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ConfigDBHelper.COLUMN_USER_ID, userId);
            values.put(ConfigDBHelper.COLUMN_MODO_MULA, 0); // false
            values.put(ConfigDBHelper.COLUMN_WIFI_DIRECT, 0); // false
            values.put(ConfigDBHelper.COLUMN_NOTIFICACOES, 1); // true

            long resultado = db.insertWithOnConflict(
                    ConfigDBHelper.TABLE_CONFIG,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );

            boolean sucesso = resultado != -1;
            Log.d(TAG, "Configurações padrão criadas para usuário " + userId + ": " + sucesso);
            return sucesso;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar configurações padrão: " + e.getMessage());
            return false;
        }
    }

    // ============= MÉTODOS AUXILIARES =============

    // Método para verificar se existem configurações para um usuário
    public boolean existeConfiguracoesParaUsuario(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT COUNT(*) FROM " + ConfigDBHelper.TABLE_CONFIG +
                    " WHERE " + ConfigDBHelper.COLUMN_USER_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar existência de configurações: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    // Método para converter Cursor em Config (SIMPLIFICADO)
    private Config cursorParaConfig(Cursor cursor) {
        Config config = new Config();

        try {
            // Usar índices fixos baseados na ordem das colunas
            // _id (0), user_id (1), modo_mula (2), wifi_direct (3), notificacoes (4)

            config.setId(cursor.getInt(0));
            config.setUserId(cursor.getString(1));
            config.setModoMula(cursor.getInt(2) == 1);
            config.setWifiDirect(cursor.getInt(3) == 1);
            config.setNotificacoes(cursor.getInt(4) == 1);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter cursor para Config: " + e.getMessage());
            // Configuração padrão em caso de erro
            config.setModoMula(false);
            config.setWifiDirect(false);
            config.setNotificacoes(true);
        }

        return config;
    }
}