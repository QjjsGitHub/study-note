package com.example.study.fragment.activityFragment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BlankViewModel extends ViewModel {

    private MutableLiveData<String> fragmentName;

    public MutableLiveData<String> getFragmentName() {
        if (fragmentName == null) {
            fragmentName = new MutableLiveData<>("null");
        }
        return fragmentName;
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName.postValue(fragmentName);
    }


}