package com.example.showdown;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;

public class MapboxSearchAdapter extends RecyclerView.Adapter<MapboxSearchAdapter.ViewHolder> {

    private final List<SearchSuggestion> suggestions = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClicked(SearchSuggestion suggestion);
    }

    public MapboxSearchAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(List<SearchSuggestion> newSuggestions) {
        suggestions.clear();
        suggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchSuggestion s = suggestions.get(position);
        holder.tvName.setText(s.getName());
        holder.tvAddress.setText(s.getAddress() != null ? s.getAddress() : "");

        holder.itemView.setOnClickListener(v -> listener.onSuggestionClicked(s));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_suggestion_name);
            tvAddress = itemView.findViewById(R.id.tv_suggestion_address);
        }
    }
}