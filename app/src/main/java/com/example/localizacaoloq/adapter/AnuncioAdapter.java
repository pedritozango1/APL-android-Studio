package com.example.localizacaoloq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.model.Anuncio;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder> {

    private List<Anuncio> listaAnuncios;

    public AnuncioAdapter(List<Anuncio> listaAnuncios) {
        this.listaAnuncios = listaAnuncios;
    }

    @NonNull
    @Override
    public AnuncioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anuncio, parent, false);
        return new AnuncioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioViewHolder holder, int position) {
        Anuncio anuncio = listaAnuncios.get(position);
        holder.bind(anuncio);
    }

    @Override
    public int getItemCount() {
        return listaAnuncios != null ? listaAnuncios.size() : 0;
    }

    public void updateLista(List<Anuncio> novaLista) {
        this.listaAnuncios = novaLista;
        notifyDataSetChanged();
    }

    static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventIcon;
        private TextView eventTitle;
        private TextView eventTag;
        private TextView eventDesc;
        private TextView metaLoc;
        private TextView metaDate;

        public AnuncioViewHolder(@NonNull View itemView) {
            super(itemView);
            eventIcon = itemView.findViewById(R.id.event_icon);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventTag = itemView.findViewById(R.id.event_tag);
            eventDesc = itemView.findViewById(R.id.event_desc);
            metaLoc = itemView.findViewById(R.id.meta_loc);
            metaDate = itemView.findViewById(R.id.meta_date);
        }

        public void bind(Anuncio anuncio) {
            // Título
            eventTitle.setText(anuncio.getTitulo());

            // Descrição/Mensagem
            eventDesc.setText(anuncio.getMensagem());

            // Tag (Modo de Entrega)
            eventTag.setText(anuncio.getModoEntrega());

            // Local
            if (anuncio.getLocal() != null) {
                metaLoc.setText("📍 " + anuncio.getLocal().getNome());
            } else {
                metaLoc.setText("📍 Local não definido");
            }

            // Data de início
            if (anuncio.getInicio() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                metaDate.setText("📅 " + sdf.format(anuncio.getInicio()));
            } else {
                metaDate.setText("📅 Data não definida");
            }

            // Ícone baseado na política
            if ("whitelist".equalsIgnoreCase(anuncio.getPolitica())) {
                eventIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            } else {
                eventIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }
    }
}