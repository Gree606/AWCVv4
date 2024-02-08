package android.example.awcvv4;

import android.animation.ValueAnimator;
import android.util.Log;

public class AnimatorXYS {

    float start_x=0;
    float start_y=0;
    public ValueAnimator animateX(int time, float jumpDist,  MapWC wcLoc)
    {
        ValueAnimator xAnimator = ValueAnimator.ofFloat(start_x, start_x+jumpDist); // Animate X position
        start_x=start_x+jumpDist;
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


    public ValueAnimator animateY(int time, float jumpDist,  MapWC wcLoc){
        ValueAnimator yAnimator = ValueAnimator.ofFloat(start_y, start_y+jumpDist); // Animate Y position
        start_y=start_y+jumpDist;
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

    public ValueAnimator animateXY(int time,float newX, float newY, MapWC wcLoc,float density){
        ValueAnimator xyAnimator = ValueAnimator.ofFloat(newX, newY);
        xyAnimator.setDuration(time);
        // Initial Y position of the view
        final float startX = wcLoc.getX(); // Initial X position of the view
        final float startY = wcLoc.getY(); // Initial Y position of the view

        Log.e("start x and y:",startX+"n"+startY);

        final float targetX = newX; // Target X position
        final float targetY = newY; // Target Y position

        xyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float currentX = startX + (targetX - startX)*fraction;
                float currentY = startY + (targetY - startY)*fraction;
                wcLoc.setX(currentX);
                wcLoc.setY(currentY);

                if(targetX>startX){
                    wcLoc.changeImage(90);
                }
                if(targetX<startX){
                    wcLoc.changeImage(-90);
                }
                if(targetY>startY){
                    wcLoc.changeImage(0);
                }
                if(targetY<startY){
                    wcLoc.changeImage(180);
                }
            }
        });
        return xyAnimator;
    }
}
