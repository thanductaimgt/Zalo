package vng.zalo.tdtai.zalo.widget;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    public State curState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (curState != State.EXPANDED) {
                curState = State.EXPANDED;
                onStateChanged(State.EXPANDED);
            }
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (curState != State.COLLAPSED) {
                curState = State.COLLAPSED;
                onStateChanged(State.COLLAPSED);
            }
        } else {
            if (curState != State.IDLE) {
                curState = State.IDLE;
                onStateChanged(State.IDLE);
            }
        }
    }

    public abstract void onStateChanged(@NonNull State state);
}