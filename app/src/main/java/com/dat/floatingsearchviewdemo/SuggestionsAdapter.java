package com.dat.floatingsearchviewdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nguyen on 6/14/2016.
 */
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

    private final static int MAX_NUMBER_SUGGESTIONS = 5;

    private Context context;
    private List<String> suggestions;
    private List<String> suggestionsFromAssets;
    private String keyword;

    public SuggestionsAdapter(Context context) {
        this.context = context;
        suggestionsFromAssets =
            Arrays.asList(context.getResources().getStringArray(R.array.suggestions));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.suggestion_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (suggestions.get(position) != null) {
            final String currentData = suggestions.get(position);
            holder.mTextView.setText(getColoredKeywordSuggestion(currentData));
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Clicked", currentData);
                }
            });
        }
    }

    public void filterSuggestions(CharSequence charSequence) {
        List<String> results = new ArrayList<>();
        if (charSequence != null && !charSequence.toString().trim().isEmpty()) {
            this.keyword = charSequence.toString();
            for (String item : suggestionsFromAssets) {
                if (item.toLowerCase().startsWith(keyword)) {
                    results.add(item);
                }
            }
        }
        setData(results);
    }

    public void setData(List<String> data) {
        if (suggestions == null) {
            suggestions = new ArrayList<>();
        }
        suggestions.clear();
        suggestions.addAll(data);
        notifyDataSetChanged();
    }

    public String getValueAt(int position) {
        return suggestions.get(position);
    }

    private Spannable getColoredKeywordSuggestion(String suggestion) {
        Spannable result = new SpannableString(suggestion);
        result.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.gray_50)), 0,
            keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }

    @Override
    public int getItemCount() {
        if (suggestions != null) {
            return suggestions.size() > MAX_NUMBER_SUGGESTIONS ? MAX_NUMBER_SUGGESTIONS
                : suggestions.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTextView = (TextView) view.findViewById(R.id.suggestion);
        }
    }
}