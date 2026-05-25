package com.example.study.ui.study;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author 16555
 */
public class MyViewModel extends ViewModel {

    private MyMutableLiveData<String> contact = new MyMutableLiveData<String>("null");

    public MyMutableLiveData<String> getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact.postValue(contact);
    }
}
