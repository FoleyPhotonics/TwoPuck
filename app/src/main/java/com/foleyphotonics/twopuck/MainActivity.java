package com.foleyphotonics.twopuck;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.view.GestureDetectorCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;


import java.util.concurrent.ThreadLocalRandom;

import static android.widget.RelativeLayout.*;

public class MainActivity extends AppCompatActivity{
    Puck puck1;
    Field field;
    private GestureDetectorCompat mDetector;


    public class Field{
        private double leftBuffer;
        private double rightBuffer;
        private double topBuffer;
        private double bottomBuffer;
        private double width;
        private double height;
        private View fieldView;
        double centreX;
        double centreY;

        Field(int leftBuffer,int rightBuffer, int topBuffer, int bottomBuffer){
            DisplayMetrics metrics = new DisplayMetrics();
            //getWindowManager().getDefaultDisplay().getMetrics(metrics);
            fieldView = findViewById(R.id.field);
            int screenHeight = 800;
            int screenWidth = 1280;
            this.leftBuffer = leftBuffer;
            this.rightBuffer = rightBuffer;
            this.topBuffer = topBuffer;
            this.bottomBuffer = bottomBuffer;
            width = screenWidth - this.rightBuffer - this.leftBuffer;
            height = screenHeight - this.bottomBuffer - this.topBuffer;
            this.centreX = width/2;
            this.centreY = height/2;

        }
        double fieldPositionX(double screenPositionX){
            return screenPositionX+this.leftBuffer;
        }
        double fieldPositionY(double screenPositionY){
            return screenPositionY+this.topBuffer;
        }
        double screenPositionX(double fieldPositionX){
            return fieldPositionX-this.leftBuffer;
        }
        double screenPositionY(double fieldPositionY){
            return fieldPositionY-this.topBuffer;
        }
        void updateView(){
            Constraints.LayoutParams lp = new Constraints.LayoutParams(Constraints.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) leftBuffer,(int) topBuffer,(int) rightBuffer,(int) bottomBuffer);

            fieldView.setLayoutParams(lp);
        }
    }

    public class Puck{
        private double centreXoffset;
        private double centreYoffset;
        double puckRadius;
        View puckView;

        public Puck(float centreXoffset,float centreYoffset,float puckRadius){
            this.centreXoffset = centreXoffset;
            this.centreYoffset = centreYoffset;
            this.puckRadius = puckRadius;
            this.puckView = findViewById(R.id.puck);
        }

        double X2centreScreen(double X){
            return X+this.centreXoffset;
        }

        double Y2centreScreen(double Y){
            return Y+this.centreYoffset;
        }

        double centreScreen2X(double centreScreenX){
            return centreScreenX-this.centreXoffset;
        }

        double centreScreen2Y(double centreScreenY){
            return centreScreenY-this.centreYoffset;
        }

        public double centreFieldPositionX(){
            return field.fieldPositionX(X2centreScreen(puckView.getX()));
        }
        public double centreFieldPositionY(){
            return field.fieldPositionY(Y2centreScreen(puckView.getY()));
        }
        void placeOnField(double centreFieldPositionX, double centreFieldPositionY){

            puckView.setX((float) (this.centreScreen2X(centreFieldPositionX)+field.leftBuffer));
            puckView.setY((float) (this.centreScreen2Y(centreFieldPositionY)+field.topBuffer));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //go fullscreen
        hideSystemUI();
        //initiate field
        field = new Field(200,10,30,10);
        //initiate puck
        puck1 = new Puck(250/2,250/2,62.5f);
        //puck1.place(0,0);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        puck1.puckView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        field.updateView();
        puck1.placeOnField(field.centreX,field.centreY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        //return super.onTouchEvent(event);
        return true;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            Log.d(DEBUG_TAG,"down coords: "+String.format("x=%.0f, y=%.0f",event.getRawX(),event.getRawY()));
            double startDistance = Math.sqrt(Math.pow(event.getRawX()-puck1.centreFieldPositionX(),2)+Math.sqrt(Math.pow(event.getRawY()-puck1.centreFieldPositionY(),2)));
            Log.d(DEBUG_TAG,"x: "+String.format("%.0f",event.getRawX()-puck1.centreFieldPositionX()));
            Log.d(DEBUG_TAG,"y: "+String.format("%.0f",event.getRawY()-puck1.centreFieldPositionY()));
            Log.d(DEBUG_TAG,"distance: "+String.format("%.0f",startDistance));
            return false;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            //event1.
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());

            Log.d(DEBUG_TAG,"down coords: "+String.format("x=%.0f, y=%.0f",event1.getRawX(),event1.getRawY()));
            Log.d(DEBUG_TAG, "ball centre: "+String.format("x=%.0f, y=%.0f",puck1.centreFieldPositionX(),puck1.centreFieldPositionY()));
            double startDistance = Math.sqrt(Math.pow(event1.getRawX()-puck1.centreFieldPositionX(),2)+Math.sqrt(Math.pow(event1.getRawY()-puck1.centreFieldPositionY(),2)));
            Log.d(DEBUG_TAG,"distance: "+String.format("%.0f",startDistance));
            if (startDistance <= puck1.puckRadius) {
                float tableFriction = 0.2f;
                FlingAnimation flingX = new FlingAnimation(puck1.puckView, DynamicAnimation.X);
                flingX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                        double fieldPositionX = field.fieldPositionX(puck1.X2centreScreen(value));
                        boolean tooHigh = fieldPositionX + puck1.puckRadius > field.width;
                        boolean tooLow = fieldPositionX - puck1.puckRadius < 0;
                        if (tooHigh || tooLow){
                            Log.d("bouncing",String.format("fieldPositionX=%.0f radius=%.0f field.width=%.0f",fieldPositionX,puck1.puckRadius,field.width));
                            animation.setStartVelocity(-velocity)
                                    .setStartValue(value);
                        }
                    }
                });
                flingX.setStartVelocity(velocityX)
                        .setFriction(tableFriction)
                        .start();

                FlingAnimation flingY = new FlingAnimation(puck1.puckView, DynamicAnimation.Y);
                flingY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                        double fieldPositionY = field.fieldPositionY(puck1.Y2centreScreen(value));
                        boolean tooHigh = fieldPositionY + puck1.puckRadius > field.height;
                        boolean tooLow = fieldPositionY - puck1.puckRadius < 0;
                        if (tooHigh || tooLow){
                            Log.d("bouncing",String.format("fieldPositionY=%.0f radius=%.0f field.height=%.0f",fieldPositionY,puck1.puckRadius,field.height));
                            animation.setStartVelocity(-velocity)
                                    .setStartValue(value);
                        }
                    }
                });
                flingY.setStartVelocity(velocityY)
                        .setFriction(tableFriction)
                        .start();
            }
            return true;
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ballClicked(View view){
        double randomNum = ThreadLocalRandom.current().nextDouble(0,1);
        randomNum = randomNum*2*Math.PI;

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
