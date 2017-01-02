package com.dat.floatingsearchviewdemo.SearchView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.dat.floatingsearchviewdemo.R;

/**
 * Created by DAT on 13-Jun-16.
 */
public class MySearchView extends FrameLayout {

    private boolean isSearchViewOpen;
    private EditText searchEditText;
    private CardView searchBar;
    private ImageButton back;
    private ImageButton clear;
    private View backgroundView;
    private LinearLayout container;
    private ListView suggestions;
    private SuggestionsAdapter suggestionsAdapter;

    private SearchViewListener searchViewListener;

    private boolean collapsingSuggestions = false;

    public MySearchView(Context context) {
        super(context);
        initDrawables();
        init();
        initSearchView();
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawables();
        init();
        initSearchView();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        searchBar = (CardView) findViewById(R.id.search_bar);
        back = (ImageButton) findViewById(R.id.action_back);
        searchEditText = (EditText) findViewById(R.id.et_search);
        clear = (ImageButton) findViewById(R.id.action_clear);
        backgroundView = findViewById(R.id.transparent_view);
        backgroundView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenuBtnDrawable.animateDrawable(MenuArrowDrawable.ARROW_TO_HAMBURGER);
                closeSearchView();
            }
        });
        backgroundView.setVisibility(View.GONE);
        suggestions = (ListView) findViewById(R.id.suggestion_list);
        suggestionsAdapter = new SuggestionsAdapter(getContext());
        suggestions.setAdapter(suggestionsAdapter);
        container = (LinearLayout) findViewById(R.id.container);
        LayoutTransition layoutTransition = container.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view,
                int transitionType) {
                //Ignore
            }

            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view,
                int transitionType) {
                if (collapsingSuggestions) {
                    Log.d("collapsingSuggestions", "collapsingSuggestions");
                    closeSearchBar();
                    collapsingSuggestions = false;
                }
            }
        });

        setEvents();
    }

    private void setEvents() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchView();
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
                closeSearchView();
            }
        });
        suggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Clicked", (String) parent.getItemAtPosition(position));
            }
        });
    }

    private MenuArrowDrawable mMenuBtnDrawable;

    private void initDrawables() {
        mMenuBtnDrawable = new MenuArrowDrawable(getContext());
        mMenuBtnDrawable.setColor(getContext().getResources().getColor(R.color.black));
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

    public void openSearchView() {
        // If search is already open, just return.
        if (isSearchViewOpen) {
            return;
        }

        searchEditText.requestFocus();

        AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                backgroundView.setVisibility(VISIBLE);
                //After SearchBar is revealed if keyword is not empty then open suggestions section
                if (!searchEditText.getText().toString().isEmpty()) {
                    suggestionsAdapter.filterSuggestions(searchEditText.getText());
                    MySearchView.this.onTextChanged(searchEditText.getText());
                }
            }
        };
        final View rootView = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootView.setVisibility(View.VISIBLE);
            AnimationUtils.circleRevealView(searchBar, listenerAdapter);
        } else {
            AnimationUtils.fadeInView(rootView);
        }

        // Call listener if we have one
        if (searchViewListener != null) {
            searchViewListener.onSearchViewOpen();
        }

        isSearchViewOpen = true;
    }

    public void closeSearchView() {
        // If we're already closed, just return.
        if (!isSearchViewOpen) {
            return;
        }

        if (suggestionsAdapter.getCount() == 0) {
            // Suggestions section is empty(collapsed)
            // just close the SearchBar
            closeSearchBar();
        } else {
            // Suggestions section is expanded
            // Clear text, values, and focus.
            // closeSearchBar should be called after Suggestions section is collapsed
            suggestionsAdapter.clearData();
            collapsingSuggestions = true;
        }
    }

    private void closeSearchBar() {

        final View v = this;

        AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // After the animation is done. Hide the root view.
                v.setVisibility(View.GONE);
                backgroundView.setVisibility(View.GONE);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimationUtils.circleHideView(searchBar, listenerAdapter);
        } else {
            AnimationUtils.fadeOutView(searchBar);
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

    public interface SearchViewListener {
        void onSearchViewOpen();

        void onSearchViewClosed();
    }
}
