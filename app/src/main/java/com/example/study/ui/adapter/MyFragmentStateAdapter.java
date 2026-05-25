package com.example.study.ui.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.study.fragment.activityFragment.BlankFragment;

/**
 * Created by sunnyDay on 2019/12/14 10:39
 *
 * @author 16555
 */
public class MyFragmentStateAdapter extends FragmentStateAdapter {

    public String[] blankFragmentNames() {
        return blankFragmentNames;
    }

    private String[] blankFragmentNames;

    private FragmentManager mFragmentManager;

    public MyFragmentStateAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, String[] blankFragmentNames) {
        super(fragmentManager, lifecycle);
        this.blankFragmentNames = blankFragmentNames;
        mFragmentManager = fragmentManager;
    }


    public void addFragment(String fragmentName) {
        addFragmentNames(fragmentName);
        notifyItemChanged(blankFragmentNames.length - 1);
    }

    public void removeFragment() {

        if (blankFragmentNames.length > 3) {
            notifyItemRemoved(blankFragmentNames.length - 1);
            removeFragmentNames();
        }

        /*if (blankFragmentNames.length > 3) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.getFragments()
                    .get(blankFragmentNames.length - 1)).commitNow();

        }*/
    }

    private void removeFragmentNames() {
        String[] newBlankFragmentNames = new String[blankFragmentNames.length - 1];
        System.arraycopy(blankFragmentNames, 0, newBlankFragmentNames, 0, blankFragmentNames.length - 1);
        blankFragmentNames = newBlankFragmentNames;
    }

    public void addFragmentNames(String fragmentName) {
        String[] newBlankFragmentNames = new String[blankFragmentNames.length + 1];
        System.arraycopy(blankFragmentNames, 0, newBlankFragmentNames, 0, blankFragmentNames.length);
        newBlankFragmentNames[newBlankFragmentNames.length - 1] = fragmentName;
        blankFragmentNames = newBlankFragmentNames;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("MyFragmentStateAdapter", "createFragment");

        return new BlankFragment(blankFragmentNames[position]);

    }

    @Override
    public int getItemCount() {
        return blankFragmentNames.length;
    }
}

