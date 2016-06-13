package com.dat.floatingsearchviewdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class MaterialSearchView extends CoordinatorLayout {
    //-- Class Properties --//

    /**
     * The freaking log tag. Used for logs, duh.
     */
    private static final String LOG_TAG = MaterialSearchView.class.getSimpleName();

    /**
     * The maximum number of results we want to return from the voice recognition.
     */
    private static final int MAX_RESULTS = 1;

    /**
     * The identifier for the voice request intent. (Guess why it's 42).
     */
    public static final int REQUEST_VOICE = 42;

    /**
     * Whether or not the search view is open right now.
     */
    private boolean mOpen;

    /**
     * The Context that this view appears in.
     */
    private Context mContext;

    /**
     * Whether or not the MaterialSearchView will animate into view or just appear.
     */
    private boolean mShouldAnimate;

    /**
     * Flag for whether or not we are clearing focus.
     */
    private boolean mClearingFocus;

    //-- UI Elements --//

    /**
     * The tint that appears over the search view.
     */
    private View mTintView;

    /**
     * The root of the search view.
     */
    private CoordinatorLayout mRoot;

    /**
     * The bar at the top of the SearchView containing the EditText and ImageButtons.
     */
    private LinearLayout mSearchBar;

    /**
     * The EditText for entering a search.
     */
    private EditText mSearchEditText;

    /**
     * The ImageButton for navigating back.
     */
    private ImageButton mBack;

    /**
     * The ImageButton for clearing the search text.
     */
    private ImageButton mClear;

    /**
     * The ListView for displaying suggestions based on the search.
     */
    private ListView mSuggestionsListView;

    /**
     * Adapter for displaying suggestions.
     */
    private ListAdapter mAdapter;

    //-- Query properties --//

    /**
     * The previous query text.
     */
    private CharSequence mOldQuery;

    /**
     * The current query text.
     */
    private CharSequence mCurrentQuery;

    //-- Listeners --//

    /**
     * Listener for when the query text is submitted or changed.
     */
    private OnQueryTextListener mOnQueryTextListener;

    /**
     * Listener for when the search view opens and closes.
     */
    private SearchViewListener mSearchViewListener;

    //-- Constructors --//

    public MaterialSearchView(Context context) {
        this(context, null);
    }

    public MaterialSearchView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);

        init();
    }

    public MaterialSearchView(Context context, AttributeSet attributeSet, int defStyleAttributes) {
        super(context, attributeSet);

        // Set variables
        this.mContext = context;
        this.mShouldAnimate = true;

        // Initialize view
        init();

        // Initialize style
        // TODO - Improve this to the next release.
    }

    // ----- Setters ----- //

    public void setOnQueryTextListener(OnQueryTextListener mOnQueryTextListener) {
        this.mOnQueryTextListener = mOnQueryTextListener;
    }

    public void setSearchViewListener(SearchViewListener mSearchViewListener) {
        this.mSearchViewListener = mSearchViewListener;
    }

    /**
     * Sets an OnItemClickListener to the suggestion list.
     *
     * @param listener - The ItemClickListener.
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSuggestionsListView.setOnItemClickListener(listener);
    }

    public void setShouldAnimate(boolean mShouldAnimate) {
        this.mShouldAnimate = mShouldAnimate;
    }

    //-- Initializers --//

    /**
     * Preforms any required initializations for the search view.
     */
    private void init() {
        // Inflate view
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true);

        // Get items
        mRoot = (CoordinatorLayout) findViewById(R.id.search_layout);
        mSearchBar = (LinearLayout) mRoot.findViewById(R.id.search_bar);
        mBack = (ImageButton) mRoot.findViewById(R.id.action_back);
        mSearchEditText = (EditText) mRoot.findViewById(R.id.et_search);
        mClear = (ImageButton) mRoot.findViewById(R.id.action_clear);

        // Set click listeners
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });

        mClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText("");
            }
        });

        mTintView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });

        // Initialize the search view.
        initSearchView();

        mSuggestionsListView.setAdapter(mAdapter);
        mSuggestionsListView.setTextFilterEnabled(true);
    }

    /**
     * Preforms necessary initializations on the SearchView.
     */
    private void initSearchView() {
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // When an edit occurs, submit the query.
                return true;
            }
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When the text changes, filter
                ((CursorAdapter) mAdapter).getFilter().filter(s.toString());
                ((CursorAdapter) mAdapter).notifyDataSetChanged();
                MaterialSearchView.this.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If we gain focus, show keyboard and show suggestions.
                if (hasFocus) {
                    showKeyboard(mSearchEditText);
                    showSuggestions();
                }
            }
        });
    }

    //-- Show methods --//

    /**
     * Displays the keyboard with a focus on the Search EditText.
     *
     * @param view The view to attach the keyboard to.
     */
    private void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus();
        }

        view.requestFocus();

        if (!isHardKeyboardAvailable()) {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    /**
     * Method that checks if there's a physical keyboard on the phone.
     *
     * @return true if there's a physical keyboard connected, false otherwise.
     */
    private boolean isHardKeyboardAvailable() {
        return mContext.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
    }

    /**
     * Changes the visibility of the clear button to VISIBLE or GONE.
     *
     * @param display True to display the clear button, false to hide it.
     */
    private void displayClearButton(boolean display) {
        mClear.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * Displays the available suggestions, if any.
     */
    private void showSuggestions() {
        mSuggestionsListView.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the SearchView.
     */
    public void openSearch() {
        // If search is already open, just return.
        if (mOpen) {
            return;
        }

        // Get focus
        mSearchEditText.setText("");
        mSearchEditText.requestFocus();

        if (mShouldAnimate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRoot.setVisibility(View.VISIBLE);
                AnimationUtils.circleRevealView(mSearchBar);
            } else {
                AnimationUtils.fadeInView(mRoot);
            }
        } else {
            mRoot.setVisibility(View.VISIBLE);
            if (mSearchViewListener != null) {
                mSearchViewListener.onSearchViewOpened();
            }
        }

        mOpen = true;
    }

    //-- Hide methods --//

    /**
     * Hides the suggestion list.
     */
    private void dismissSuggestions() {
        mSuggestionsListView.setVisibility(View.GONE);
    }

    /**
     * Hides the keyboard displayed for the SearchEditText.
     *
     * @param view The view to detach the keyboard from.
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =
            (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Closes the search view if necessary.
     */
    public void closeSearch() {
        // If we're already closed, just return.
        if (!mOpen) {
            return;
        }

        // Clear text, values, and focus.
        mSearchEditText.setText("");
        dismissSuggestions();
        clearFocus();

        if (mShouldAnimate) {
            final View v = mRoot;

            AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // After the animation is done. Hide the root view.
                    v.setVisibility(View.GONE);
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AnimationUtils.circleHideView(mSearchBar, listenerAdapter);
            } else {
                AnimationUtils.fadeOutView(mRoot);
            }
        } else {
            // Just hide the view.
            mRoot.setVisibility(View.GONE);
        }

        // Call listener if we have one
        if (mSearchViewListener != null) {
            mSearchViewListener.onSearchViewClosed();
        }

        mOpen = false;
    }

    //-- Interface methods --//

    /**
     * Filters and updates the buttons when text is changed.
     *
     * @param newText The new text.
     */
    private void onTextChanged(CharSequence newText) {
        // Get current query
        mCurrentQuery = mSearchEditText.getText();

        // If the text is not empty, show the empty button and hide the voice button
        if (!TextUtils.isEmpty(mCurrentQuery)) {
            displayClearButton(true);
        } else {
            displayClearButton(false);
        }

        // TODO - #5
        // If we have a query listener and the text has changed, call it.
        if (mOnQueryTextListener != null) {
            mOnQueryTextListener.onQueryTextChange(newText.toString());
        }

        mOldQuery = mCurrentQuery;
    }

    //-- Mutators --//

    /**
     * Sets the background of the SearchView.
     *
     * @param background The drawable to use as a background.
     */
    @Override
    public void setBackground(Drawable background) {
        // Method changed in jelly bean for setting background.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchBar.setBackground(background);
        } else {
            //noinspection deprecation
            mSearchBar.setBackgroundDrawable(background);
        }
    }

    /**
     * Sets the background color of the SearchView.
     *
     * @param color The color to use for the background.
     */
    @Override
    public void setBackgroundColor(int color) {
        // Set background color of search bar.
        mSearchBar.setBackgroundColor(color);
    }

    /**
     * Change the color of the background tint.
     *
     * @param color The new color.
     */
    public void setTintColor(int color) {
        mTintView.setBackgroundColor(color);
    }

    /**
     * Sets the alpha value of the background tint.
     *
     * @param alpha The alpha value, from 0 to 255.
     */
    public void setTintAlpha(int alpha) {
        if (alpha < 0 || alpha > 255) return;

        Drawable d = mTintView.getBackground();

        if (d instanceof ColorDrawable) {
            ColorDrawable cd = (ColorDrawable) d;
            int color = cd.getColor();
            int newColor =
                Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));

            setTintColor(newColor);
        }
    }

    /**
     * Adjust the background tint alpha, based on a percentage.
     *
     * @param factor The factor of the alpha, from 0% to 100%.
     */
    public void adjustTintAlpha(float factor) {
        if (factor < 0 || factor > 1.0) return;

        Drawable d = mTintView.getBackground();

        if (d instanceof ColorDrawable) {
            ColorDrawable cd = (ColorDrawable) d;
            int color = cd.getColor();

            color = adjustAlpha(color, factor);

            mTintView.setBackgroundColor(color);
        }
    }

    /**
     * Adjust the alpha of a color based on a percent factor.
     *
     * @param color - The color you want to change the alpha value.
     * @param factor - The factor of the alpha, from 0% to 100%.
     * @return The color with the adjusted alpha value.
     */
    private int adjustAlpha(int color, float factor) {
        if (factor < 0) return color;

        int alpha = Math.round(Color.alpha(color) * factor);

        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Sets the text color of the EditText.
     *
     * @param color The color to use for the EditText.
     */
    public void setTextColor(int color) {
        mSearchEditText.setTextColor(color);
    }

    /**
     * Sets the text color of the search hint.
     *
     * @param color The color to be used for the hint text.
     */
    public void setHintTextColor(int color) {
        mSearchEditText.setHintTextColor(color);
    }

    /**
     * Sets the hint to be used for the search EditText.
     *
     * @param hint The hint to be displayed in the search EditText.
     */
    public void setHint(CharSequence hint) {
        mSearchEditText.setHint(hint);
    }

    /**
     * Sets the icon for the clear action.
     *
     * @param drawable The drawable to represent the clear action.
     */
    public void setClearIcon(Drawable drawable) {
        mClear.setImageDrawable(drawable);
    }

    /**
     * Sets the icon for the back action.
     *
     * @param drawable The drawable to represent the back action.
     */
    public void setBackIcon(Drawable drawable) {
        mBack.setImageDrawable(drawable);
    }

    /**
     * Sets the background of the suggestions ListView.
     *
     * @param drawable The drawable to use as a background for the suggestions listview.
     */
    public void setSuggestionBackground(Drawable drawable) {
        // Method change in jelly bean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSuggestionsListView.setBackground(getBackground());
        } else {
            //noinspection deprecation
            mSuggestionsListView.setBackgroundDrawable(drawable);
        }
    }

    //-- Accessors --//

    /**
     * Determines if the search view is opened or closed.
     *
     * @return True if the search view is open, false if it is closed.
     */
    public boolean isOpen() {
        return mOpen;
    }

    /**
     * Determines if the user's voice is available
     *
     * @return True if we can collect the user's voice, false otherwise.
     */
    private boolean isVoiceAvailable() {
        // Get package manager
        PackageManager packageManager = mContext.getPackageManager();

        // Gets a list of activities that can handle this intent.
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        // Returns true if we have at least one activity.
        return activities.size() > 0;
    }

    //-- View methods --//

    /**
     * Handles any cleanup when focus is cleared from the view.
     */
    @Override
    public void clearFocus() {
        this.mClearingFocus = true;
        hideKeyboard(this);
        super.clearFocus();
        mSearchEditText.clearFocus();
        this.mClearingFocus = false;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Don't accept if we are clearing focus, or if the view isn't focusable.
        return !(mClearingFocus || !isFocusable()) && mSearchEditText.requestFocus(direction,
            previouslyFocusedRect);
    }

    //-- Interfaces --//

    /**
     * Interface that handles the submission and change of search queries.
     */
    public interface OnQueryTextListener {
        /**
         * Called when a search query is submitted.
         *
         * @param query The text that will be searched.
         * @return True when the query is handled by the listener, false to let the SearchView
         * handle the default case.
         */
        boolean onQueryTextSubmit(String query);

        /**
         * Called when a search query is changed.
         *
         * @param newText The new text of the search query.
         * @return True when the query is handled by the listener, false to let the SearchView
         * handle the default case.
         */
        boolean onQueryTextChange(String newText);
    }

    /**
     * Interface that handles the opening and closing of the SearchView.
     */
    public interface SearchViewListener {
        /**
         * Called when the searchview is opened.
         */
        void onSearchViewOpened();

        /**
         * Called when the search view closes.
         */
        void onSearchViewClosed();
    }
}