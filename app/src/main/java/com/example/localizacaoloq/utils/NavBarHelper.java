package com.example.localizacaoloq.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.localizacaoloq.activities.FormHome;
import com.example.localizacaoloq.activities.LocalForm;
import com.example.localizacaoloq.activities.FormAviso;
import com.example.localizacaoloq.R;
import com.example.localizacaoloq.activities.FormPerfil;

public class NavBarHelper {
    private static final int ACTIVE_COLOR = Color.RED; // Ou defina como Color.parseColor("#F44336") para primary_red
    private static final int INACTIVE_COLOR = Color.parseColor("#666666");
    public static void setup(final Activity activity) {
        LinearLayout navHome = activity.findViewById(R.id.nav_home);
        LinearLayout navLocais = activity.findViewById(R.id.nav_locais);
        LinearLayout navAvisos = activity.findViewById(R.id.nav_avisos);
        LinearLayout navPerfil = activity.findViewById(R.id.nav_perfil);

        navHome.setOnClickListener(v -> open(activity, FormHome.class));
        navLocais.setOnClickListener(v -> open(activity, LocalForm.class));
        navAvisos.setOnClickListener(v -> open(activity, FormAviso.class));
        navPerfil.setOnClickListener(v -> open(activity, FormPerfil.class));

        resetAll(activity);
        highlightCurrent(activity);
    }

    private static void open(Activity current, Class<?> target) {
        if (!current.getClass().equals(target)) {
            Intent intent = new Intent(current, target);
            current.startActivity(intent);
            current.overridePendingTransition(0, 0); // sem animação
            current.finish();
        }
    }
    private static void resetAll(Activity activity) {
        // Resetar Home
        setNavItemColors(activity, R.id.nav_home_icon, R.id.nav_home_text, INACTIVE_COLOR);
        // Resetar Locais
        setNavItemColors(activity, R.id.nav_locais_icon, R.id.nav_locais_text, INACTIVE_COLOR);
        // Resetar Avisos
        setNavItemColors(activity, R.id.nav_avisos_icon, R.id.nav_avisos_text, INACTIVE_COLOR);
        // Resetar Perfil
        setNavItemColors(activity, R.id.nav_perfil_icon, R.id.nav_perfil_text, INACTIVE_COLOR);
    }

    private static void highlightCurrent(Activity activity) {
        Class<?> currentClass = activity.getClass();
        if (currentClass.equals(FormHome.class)) {
            setNavItemColors(activity, R.id.nav_home_icon, R.id.nav_home_text, ACTIVE_COLOR);
        } else if (currentClass.equals(LocalForm.class)) {
            setNavItemColors(activity, R.id.nav_locais_icon, R.id.nav_locais_text, ACTIVE_COLOR);
        } else if (currentClass.equals(FormAviso.class)) {
            setNavItemColors(activity, R.id.nav_avisos_icon, R.id.nav_avisos_text, ACTIVE_COLOR);
        } else if (currentClass.equals(FormPerfil.class)) {
            setNavItemColors(activity, R.id.nav_perfil_icon, R.id.nav_perfil_text, ACTIVE_COLOR);
        }
    }

    private static void setNavItemColors(Activity activity, int iconId, int textId, int color) {
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);

        if (icon != null) {
            icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        if (text != null) {
            text.setTextColor(color);
        }
    }
}
