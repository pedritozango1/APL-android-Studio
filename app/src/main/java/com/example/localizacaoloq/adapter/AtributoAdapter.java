package com.example.localizacaoloq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.model.Perfil;

import java.util.List;

public class AtributoAdapter extends RecyclerView.Adapter<AtributoAdapter.ViewHolder> {

    private List<Perfil> lista;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(Perfil perfil, int position);
    }

    public AtributoAdapter(List<Perfil> lista, OnDeleteClickListener deleteListener) {
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
        Perfil atributo = lista.get(position);

        holder.textKey.setText(atributo.getChave());
        holder.textValue.setText(atributo.getValor());

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja apagar o atributo '" + atributo.getChave() + "'?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onDelete(atributo, holder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void updatePerfis(List<Perfil> newPerfis) {
        this.lista = newPerfis;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        lista.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, lista.size());
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