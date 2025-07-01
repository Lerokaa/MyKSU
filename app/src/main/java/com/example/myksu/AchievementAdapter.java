package com.example.myksu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private final List<Achievement> achievements;

    public AchievementAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.name.setText(achievement.getName());
        holder.description.setText(achievement.getDescription());
        holder.icon.setImageResource(achievement.getImageResId());
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
            icon = itemView.findViewById(R.id.achievement_image);
        }
    }
}