package com.dat.floatingsearchviewdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by DAT on 13-Jun-16.
 */
public class MySearchView extends FrameLayout {

    private boolean isSearchViewOpen;
    private FrameLayout rootView;
    private EditText searchEditText;
    private LinearLayout searchBar;
    private ImageButton back;
    private ImageButton clear;
    private View backgroundView;
    private LinearLayout container;
    private RecyclerView suggestions;
    private SuggestionsAdapter suggestionsAdapter;

    private SearchViewListener searchViewListener;

    public MySearchView(Context context) {
        super(context);
        init();
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initSearchView();
    }

    private void init() {
        // Inflate view
        LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        // Get items
        rootView = (FrameLayout) findViewById(R.id.search_layout);
        searchBar = (LinearLayout) findViewById(R.id.search_bar);
        back = (ImageButton) findViewById(R.id.action_back);
        searchEditText = (EditText) findViewById(R.id.et_search);
        clear = (ImageButton) findViewById(R.id.action_clear);
        backgroundView = findViewById(R.id.transparent_view);
        suggestions = (RecyclerView) findViewById(R.id.suggestion_list);
        suggestions.setLayoutManager(new LinearLayoutManager(suggestions.getContext()));
        suggestionsAdapter = new SuggestionsAdapter(getContext());
        suggestions.setAdapter(suggestionsAdapter);
        suggestions.clearOnScrollListeners();
        container = (LinearLayout) findViewById(R.id.container);
        LayoutTransition layoutTransition = container.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        setEvents();
    }

    private void setEvents() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });
        backgroundView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });
    }

    private void initSearchView() {

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence keyword, int start, int before, int count) {
                suggestionsAdapter.filterSuggestions(keyword);
                MySearchView.this.onTextChanged(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setSearchViewListener(SearchViewListener searchViewListener) {
        this.searchViewListener = searchViewListener;
    }

    /**
     * Displays the SearchView.
     */
    public void openSearch() {
        // If search is already open, just return.
        if (isSearchViewOpen) {
            return;
        }

        // Get focus
        searchEditText.setText("");
        searchEditText.requestFocus();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootView.setVisibility(View.VISIBLE);
            AnimationUtils.circleRevealView(searchBar);
        } else {
            AnimationUtils.fadeInView(rootView);
        }

        // Call listener if we have one
        if (searchViewListener != null) {
            searchViewListener.onSearchViewOpen();
        }

        isSearchViewOpen = true;
    }

    public void closeSearch() {
        // If we're already closed, just return.
        if (!isSearchViewOpen) {
            return;
        }

        // Clear text, values, and focus.
        searchEditText.setText("");
        clearFocus();

        final View v = rootView;

        AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // After the animation is done. Hide the root view.
                v.setVisibility(View.GONE);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimationUtils.circleHideView(searchBar, listenerAdapter);
        } else {
            AnimationUtils.fadeOutView(rootView);
        }

        if (searchViewListener != null) {
            searchViewListener.onSearchViewClosed();
        }
        isSearchViewOpen = false;
    }

    private void onTextChanged(CharSequence newText) {

        // If the text is not empty, show the empty button and hide the voice button
        if (!TextUtils.isEmpty(searchEditText.getText())) {
            displayClearButton(true);
        } else {
            displayClearButton(false);
        }
    }

    private void displayClearButton(boolean display) {
        clear.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    interface SearchViewListener {
        void onSearchViewOpen();

        void onSearchViewClosed();
    }
}
