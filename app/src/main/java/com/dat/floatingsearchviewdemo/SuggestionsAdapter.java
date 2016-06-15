package com.dat.floatingsearchviewdemo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Nguyen on 6/14/2016.
 */
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

    private final static int MAX_NUMBER_SUGGESTIONS = 5;
    private List<String> mValues;

    public SuggestionsAdapter(List<String> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.suggestion_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues.get(position) != null) {
            final String currentData = mValues.get(position);
            holder.mTextView.setText(currentData);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Clicked", currentData);
                }
            });
        }
    }

    public void setData(List<String> data) {
        mValues.clear();
        mValues.addAll(data);
        notifyDataSetChanged();
    }

    public String getValueAt(int position) {
        return mValues.get(position);
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size() > MAX_NUMBER_SUGGESTIONS ? MAX_NUMBER_SUGGESTIONS
                : mValues.size();
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