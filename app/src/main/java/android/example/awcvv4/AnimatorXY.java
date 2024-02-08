package android.example.awcvv4;

import android.animation.ValueAnimator;

public class AnimatorXY {

    public ValueAnimator animateX(int time, float start_x, float end_x, MapWC wcLoc)
    {
        ValueAnimator xAnimator = ValueAnimator.ofFloat(start_x, end_x); // Animate X position
        xAnimator.setDuration(time); // Duration of X animation in milliseconds
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newX = (float) animation.getAnimatedValue();
                wcLoc.setX(newX);
                wcLoc.invalidate();
            }
        });
        return xAnimator;

    }


    public ValueAnimator animateY(int time, float start_y, float end_y, MapWC wcLoc){
        ValueAnimator yAnimator = ValueAnimator.ofFloat(start_y, end_y); // Animate Y position
        yAnimator.setDuration(time); // Duration of Y animation in milliseconds

        yAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newY = (float) animation.getAnimatedValue();
                wcLoc.setY(newY);
                wcLoc.invalidate();
            }
        });
        return yAnimator;
    }
}
