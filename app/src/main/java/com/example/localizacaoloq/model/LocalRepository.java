package com.example.localizacaoloq.model;



import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalWifi;

import java.util.ArrayList;
import java.util.List;
public class LocalRepository {
    private static LocalRepository instance;
    private List<Local> listaLocais = new ArrayList<>();

    private LocalRepository() {
        // Construtor privado
    }

    public static LocalRepository getInstance() {
        if (instance == null) {
            instance = new LocalRepository();
        }
        return instance;
    }

    public List<Local> getLocais() {
        return new ArrayList<>(listaLocais);
    }
    public void adicionarLocalGPS(LocalGPS localGps) {
        listaLocais.add(0, localGps); // Adiciona no início (mais recente primeiro)
    }

    public void adicionarLocalWifi(LocalWifi localWifi) {
        listaLocais.add(0, localWifi);
    }
}
