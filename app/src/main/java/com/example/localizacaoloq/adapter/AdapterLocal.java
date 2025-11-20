package com.example.localizacaoloq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.LocalGPS;
import com.example.localizacaoloq.model.LocalWifi;

import java.util.List;

public class AdapterLocal extends RecyclerView.Adapter<AdapterLocal.ViewHolder> {

    private List<Local> locais;
    private OnLocalDeleteListener deleteListener;
    public interface OnLocalDeleteListener {
        void onLocalDeleted(Local local, int position);
    }

    public AdapterLocal(List<Local> locais,OnLocalDeleteListener listener) {
        this.locais = locais;
        this.deleteListener = listener;
    }
    @Override
    public int getItemCount() {
        return locais.size();
    }

    public void updateLocais(List<Local> newLocais) {
        this.locais = newLocais;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_local, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Local local = locais.get(position);
        holder.bind(local);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja apagar o local '" + local.getNome() + "'?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onLocalDeleted(local, holder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });


    }
    public void removeItem(int position) {
        locais.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, locais.size());
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNome, tvTipo, tvDetalhes;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Local local) {

            tvNome.setText(local.getNome());
            tvTipo.setText(local.getTipo());

            if (local instanceof LocalGPS) {
                LocalGPS gps = (LocalGPS) local;
                tvDetalhes.setText(String.format("%.4f, %.4f • Raio: %.0fm", gps.getLatitude(), gps.getLongitude(), gps.getRaio()));
                ivIcon.setImageResource(R.drawable.ic_menu_mylocation);
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primary_red), android.graphics.PorterDuff.Mode.SRC_IN); // Ajusta cor
            } else if (local instanceof LocalWifi) {
                LocalWifi wifi = (LocalWifi) local;
                tvDetalhes.setText(String.format("%d rede(s): %s", wifi.getWifiIds().size(), String.join(", ", wifi.getWifiIds().subList(0, Math.min(2, wifi.getWifiIds().size()))))); // Mostra até 2 IDs
                ivIcon.setImageResource(R.drawable.ic_dialog_map);
                ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN); // Ajusta cor
            }
        }
    }
}