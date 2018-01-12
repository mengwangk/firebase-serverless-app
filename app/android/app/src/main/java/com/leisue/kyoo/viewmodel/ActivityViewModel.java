package com.leisue.kyoo.viewmodel;

import android.arch.lifecycle.ViewModel;

/**
 * View model.
 */

public class ActivityViewModel extends ViewModel {
    private boolean mIsSigningIn;

    public ActivityViewModel() {
        mIsSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

}
