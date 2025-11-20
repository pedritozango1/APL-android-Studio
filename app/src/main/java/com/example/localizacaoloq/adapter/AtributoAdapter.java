package com.example.localizacaoloq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.model.Atributo;

import java.util.List;

public class AtributoAdapter extends RecyclerView.Adapter<AtributoAdapter.ViewHolder> {

    private List<Atributo> lista;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(int position);
    }

    public AtributoAdapter(List<Atributo> lista, OnDeleteClickListener deleteListener) {
        this.lista = lista;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_atributo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Atributo atributo = lista.get(position);

        holder.textKey.setText(atributo.getKey());
        holder.textValue.setText(atributo.getValue());

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null)
                deleteListener.onDelete(position);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textKey, textValue;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textKey = itemView.findViewById(R.id.textKey);
            textValue = itemView.findViewById(R.id.textValue);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
