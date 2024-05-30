package com.kenz.almuslim.data.adapter;

import android.view.ViewGroup;

import com.kenz.almuslim.ui.category.CategoriesFragment;
import com.kenz.almuslim.ui.gif.GifFragment;
import com.kenz.almuslim.ui.home.HomeFragment;
import com.kenz.almuslim.ui.search.SearchFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kenz.almuslim.ui.favourite.FavouriteFragment;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Objects;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    public Hashtable<Integer, WeakReference<Fragment>> fragmentReferences = new Hashtable<>();


    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new CategoriesFragment();
                break;

            case 1:
                fragment = new SearchFragment();
                break;

            case 2:
                fragment = new HomeFragment();
                break;

            case 3:
                fragment = new GifFragment();
                break;

            case 4:
                fragment = new FavouriteFragment();
                break;

        }
        fragmentReferences.put(position, new WeakReference<>(fragment));
        return Objects.requireNonNull(fragment);
    }


    @Override
    public @NotNull Object instantiateItem(@NotNull ViewGroup container, int position) {
        //        // save the appropriate reference depending on position
//        switch (position) {
//            case 0:
//                m1stFragment = (CategoriesFragment) createdFragment;
//                break;
//            case 1:
//                m2ndFragment = (SearchFragment) createdFragment;
//                break;
//        }
        return (Fragment) super.instantiateItem(container, position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

    @Override
    public int getCount() {
        return 5;
    }
}