package com.dat.floatingsearchviewdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by DAT on 13-Jun-16.
 */
public class MyFragment extends Fragment implements MySearchView.SearchViewListener {
    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.search_view)
    protected MySearchView searchView;
    private View view;
    private MenuItem searchItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_one, container, false);
        ButterKnife.bind(this, view);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        searchItem = menu.findItem(R.id.search);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                searchView.setSearchViewListener(MyFragment.this);
                searchView.openSearch();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSearchViewOpen() {
        if (searchItem != null) {
            AnimationUtils.fadeOutView(toolbar);
        }
    }

    @Override
    public void onSearchViewClosed() {
        if (searchItem != null) {
            AnimationUtils.fadeInView(toolbar);
        }
    }
}
