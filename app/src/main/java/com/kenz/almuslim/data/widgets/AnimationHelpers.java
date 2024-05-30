package com.kenz.almuslim.data.widgets;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.BounceInterpolator;

public class AnimationHelpers {
    public static void startAnimationTopOrBottom(View view, float value, int duration){
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", value, 0f);
        animY.setDuration(duration);//1sec
        animY.setInterpolator(new BounceInterpolator());
        animY.start();
    }

    public static void startAnimationRightOrLeft(View view, float value){
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationX", value, 0f);
        animY.setDuration(1000);//1sec
        animY.setInterpolator(new BounceInterpolator());
        animY.start();
    }

}
