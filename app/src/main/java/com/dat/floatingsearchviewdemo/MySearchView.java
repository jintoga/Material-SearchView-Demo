package com.dat.floatingsearchviewdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.util.List;

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

    private final Interpolator SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR = new LinearInterpolator();
    private final int ATTRS_SUGGESTION_ANIM_DURATION_DEFAULT = 250;

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
            public void onTextChanged(final CharSequence keyword, int start, int before,
                int count) {
                suggestionsAdapter.filterSuggestions(keyword);
                suggestions.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Util.removeGlobalLayoutObserver(suggestions, this);
                            suggestionsListChanged();
                        }
                    });
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

    private void suggestionsListChanged() {
        final int cardTopBottomShadowPadding = Util.dpToPx(5);
        final int cardRadiusSize = Util.dpToPx(3);

        int visibleHeight = getVisibleItemsHeight(suggestionsAdapter.getSuggestions());
        int diff = container.getHeight() - visibleHeight;
        int addedTranslationYForShadowOffsets =
            diff <= cardTopBottomShadowPadding ? -(cardTopBottomShadowPadding - diff)
                : Math.max(cardRadiusSize - (diff - cardTopBottomShadowPadding), cardRadiusSize);
        final float newTranslationY = -container.getHeight()
            +
            getVisibleItemsHeight(suggestionsAdapter.getSuggestions())
            + addedTranslationYForShadowOffsets;

        final boolean animateAtEnd = newTranslationY >= container.getTranslationY();

        Log.d("searchBar:", 2 * searchBar.getHeight() + "");
        Log.d("newTranslationY:", newTranslationY + "");
        ViewCompat.animate(container).cancel();
        ViewCompat.animate(container)
            .setInterpolator(SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR)
            .setDuration(1000)
            .translationY(newTranslationY)
            .setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    if (!animateAtEnd) {
                        suggestions.smoothScrollToPosition(0);
                    }
                }

                @Override
                public void onAnimationEnd(View view) {
                    if (animateAtEnd) {
                        int lastPos = suggestions.getAdapter().getItemCount() - 1;
                        if (lastPos > -1) {
                            suggestions.smoothScrollToPosition(lastPos);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(View view) {
                    container.setTranslationY(newTranslationY);
                }
            })
            .start();
    }

    private void displayClearButton(boolean display) {
        clear.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    interface SearchViewListener {
        void onSearchViewOpen();

        void onSearchViewClosed();
    }

    private int getVisibleItemsHeight(List<String> data) {

        int visibleItemsHeight = 0;
        for (int i = 0; i < data.size() && i < suggestions.getChildCount(); i++) {
            visibleItemsHeight += suggestions.getChildAt(i).getHeight();

            if (visibleItemsHeight > container.getHeight()) {
                visibleItemsHeight = container.getHeight();
                break;
            }
        }

        Log.d("visibleItemsHeight:", visibleItemsHeight + "");
        return visibleItemsHeight;
    }
}
