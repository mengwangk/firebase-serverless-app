package com.leisue.kyoo.ui.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * View model.
 */

public class BaseActivityViewModel extends ViewModel {
    private boolean isSigningIn;

    public BaseActivityViewModel() {
        isSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.isSigningIn = mIsSigningIn;
    }

}
