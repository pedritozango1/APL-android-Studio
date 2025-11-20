package com.example.localizacaoloq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.model.Anuncio;

import java.util.List;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.ViewHolder> {

    private List<Anuncio> anuncios;

    public AnuncioAdapter(List<Anuncio> anuncios) {
        this.anuncios = anuncios;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, desc, local, data, tag;

        public ViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.event_title);
            desc = itemView.findViewById(R.id.event_desc);
            local = itemView.findViewById(R.id.meta_loc);
            data = itemView.findViewById(R.id.meta_date);
            tag = itemView.findViewById(R.id.event_tag);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Anuncio anuncio = anuncios.get(position);
        holder.titulo.setText(anuncio.titulo);
        holder.desc.setText(anuncio.descricao);
        holder.local.setText(anuncio.local);
        holder.data.setText(anuncio.data);
        holder.tag.setText(anuncio.tag);
    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }
}

