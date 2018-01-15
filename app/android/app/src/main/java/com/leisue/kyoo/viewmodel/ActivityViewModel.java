package com.leisue.kyoo.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * View model.
 */

public class ActivityViewModel extends ViewModel {
    private boolean isSigningIn;

    public ActivityViewModel() {
        isSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public void setIsSigningIn(boolean isSigningIn) {
        this.isSigningIn = isSigningIn;
    }

}
