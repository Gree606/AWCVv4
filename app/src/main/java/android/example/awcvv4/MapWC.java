package android.example.awcvv4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MapWC extends View {

    private int x, y;
    private float radius;
    private Paint paint;
    Bitmap imageBitmap;
    private float rotationAngle;
    Matrix matrix;
    float scaleFactor;
    int ontouchX, ontouchY;

    public MapWC(Context context) {
        super(context);
    }
        public MapWC(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public void init() {
        float density = getResources().getDisplayMetrics().density;
        ontouchX = 0; // Initial x position of the blob
        ontouchY = 0; // Initial y position of the blob
        paint = new Paint();
        matrix = new Matrix();
        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wc_icon);
        rotationAngle = 0.0f;
        scaleFactor=1;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        int desiredWidth =(int) (imageBitmap.getWidth()/10);
        int desiredHeight = (int)(imageBitmap.getHeight()/10);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, desiredWidth, desiredHeight, false);

        matrix.setScale(scaleFactor, scaleFactor);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(scaledBitmap, ontouchX, ontouchY, null);

    }

    public void changeImage(float angle){
        rotationAngle=angle;
        if(angle==90){
            imageBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.wc_right);
        }
        else if(angle==-90){
            imageBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.wc_left);
        }
        else if(angle==0){
            imageBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.wc_down);
        }
        else if(angle==180){
            imageBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.wc_icon);
        }
        invalidate();
    }
}

