package io.github.kylelam.appleanddroid;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class game extends ActionBarActivity {
    final int FPS = 60;
    final int TOTAL_WIDTH = 4;
    final int TOTAL_HEIGHT = 5;
    final int TOTAL_TIME = FPS * 30;

    int shellOnBoard = TOTAL_WIDTH * TOTAL_HEIGHT;

    int nextAddTime = 0;

    //shell up time in seconds
    double upTime = 2;

    //up random shell at rate
    //double rate = 0.5;
    double rate = upTime*0.75;//1.0/(#shellToCatch/catchUpTime+1/upTime);

    ImageView [] imageViews = new ImageView[TOTAL_WIDTH*TOTAL_HEIGHT];
    Shell [] shells = new Shell[TOTAL_WIDTH*TOTAL_HEIGHT];

    int oldOrientation;
    int currentTime=0;

    private Handler gameLoopHandler = new Handler();

    boolean shouldResume = false;
    Random randomNumberGenerator = new Random();

    @Override
    protected void onPause() {
        super.onPause();
        gameLoopHandler.removeCallbacks(gameLoop);
        shouldResume = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldResume) {
            gameLoopHandler.postDelayed(gameLoop, 1000 / FPS);
            shouldResume = false;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameLoopHandler.removeCallbacks(gameLoop);
        setRequestedOrientation(oldOrientation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game);


        Log.v(Double.toString(rate),"debug");

        //no rotate
        oldOrientation = getRequestedOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);


        View.OnClickListener imageViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View imageView) {
                int index = java.util.Arrays.asList(imageViews).indexOf(imageView);
                /*Toast.makeText(game.this,
                        "This is index: " + Integer.toString(index),   //+Integer.toString(countI)+" j:"+Integer.toString(countJ),
                        Toast.LENGTH_SHORT).show();*/
                shells[index].clicked();
            }
        };

        LinearLayout gameLayout = new LinearLayout(this);
        gameLayout.setOrientation(LinearLayout.VERTICAL);

        for (int countJ = 0; countJ < TOTAL_HEIGHT; countJ++) {

            //LinearLayOut Setup
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);


            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

            for (int countI = 0; countI < TOTAL_WIDTH; countI++) {


                //ImageView Setup
                ImageView imageView = new ImageView(this);
                //setting image resource
                imageView.setImageResource(R.drawable.android);
                imageView.setClickable(true);
                imageView.setOnClickListener(imageViewListener);
                //setting image position
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

                //adding view to layout
                linearLayout.addView(imageView);

                //adding to view holder imageViews
                imageViews[countJ * TOTAL_WIDTH + countI] = imageView;
                shells[countJ * TOTAL_WIDTH + countI] = new Shell(imageView, false);

            }

            gameLayout.addView(linearLayout);
        }
        //make visible to program
        setContentView(gameLayout);
        startGameLoop();
    }

    private void startGameLoop(){
        //to start the gameLoop
        currentTime=0;
        gameLoopHandler.postDelayed(gameLoop, 1000/FPS);
    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {

            //state update
            for (int i = 0; i < TOTAL_WIDTH*TOTAL_HEIGHT; i++ ){
                shells[i].update();
            }

            //add random
            if ((nextAddTime<=currentTime)&&(shellOnBoard < TOTAL_WIDTH * TOTAL_HEIGHT) ) {

                Log.v(Integer.toString(shellOnBoard),"debug: shellOnBoard: ");
                int randomNumber = randomNumberGenerator.nextInt(TOTAL_WIDTH*TOTAL_HEIGHT);



                int numberTired =0;
                while ((!shells[randomNumber].isBlank) && (numberTired < 60)){
                    //Log.v(Integer.toString(randomNumber),"debug: randomNumber: ");
                    randomNumber = randomNumberGenerator.nextInt(TOTAL_WIDTH*TOTAL_HEIGHT);
                    numberTired++;
                }
                shells[randomNumber].startAnime(true);

                nextAddTime=currentTime+(int)(rate*FPS+0.5);
            }



            if ((currentTime%(FPS*15))==0) {
                Toast.makeText(game.this,
                        "hello from timer",
                        Toast.LENGTH_SHORT).show();
            }
            /* and here comes the "trick" */
            currentTime++;
            gameLoopHandler.postDelayed(this, 1000/FPS);
        }
    };


    public void updateRate(){
        int targetShellNum = (int)(((TOTAL_HEIGHT*TOTAL_WIDTH-1)*0.3)*(currentTime*1.0/TOTAL_TIME));
        int shellToCatch = targetShellNum - shellOnBoard;
        /*double catchUpTime = TOTAL_TIME*0.1;
        double newRate = catchUpTime/shellToCatch + upTime;//1.0/(shellToCatch/catchUpTime+1/upTime);
        Log.v(Double.toString(shellToCatch),"debug: shellToCatch");

        Log.v(Double.toString(newRate),"debug: newRate");*/
        double newRate = upTime/(targetShellNum+1);

        double percent = 0.6;

        rate = rate*(1-percent) + newRate*(percent);

        if(rate>2){
            rate=2;
        }
        if (rate<0.1)
            rate = 0.1;
    }

    public void updateUpTime(int deta){
        double percent = 0.4;
        upTime = upTime*(1-percent) + deta*0.9*(percent);
        if (upTime<0.3){
            upTime = 0.3;
        }
        Log.v(Double.toString(upTime),"debug: upTime");
        updateRate();
    }



    class Shell{
        boolean isBlank = true;
        ImageView imageView;
        int scheduledResetTime = 0;
        int startTime = 0;

        public Shell(ImageView imageView, boolean isBlank){
            this.isBlank = isBlank;
            this.imageView = imageView;
        }

        public void setClickable(boolean clickable){
            imageView.setClickable(clickable);
        }

        public void setImageResource(int drawable){
            imageView.setImageResource(drawable);   //R.drawable.android
        }

        public void startAnime(boolean up){
            if (up){
                startTime = currentTime;
                isBlank = false;
                setClickable(true);
                scheduledResetTime = (int)(FPS * upTime + 0.5) + currentTime;
                shellOnBoard = shellOnBoard + 1;

                Log.v(Integer.toString(shellOnBoard),"debug: shellOnBoard: +1");
                //do real anime works
                imageView.setVisibility(View.VISIBLE);
            } else {
                //do real anime works
                imageView.setVisibility(View.INVISIBLE);

                //on real anime finished
                //should happen when the image is completely gone
                setClickable(false);
                scheduledResetTime = Integer.MAX_VALUE;
                isBlank = true;
                shellOnBoard = shellOnBoard - 1;
                Log.v(Integer.toString(shellOnBoard),"debug: shellOnBoard: -1");
            }
        }


        public void clicked(){
            setClickable(false);
            startAnime(false);
            int endTime = currentTime;
            updateUpTime(startTime - endTime);

        }

        public void update(){
            if (scheduledResetTime <= currentTime){     //catch auto down
                startAnime(false);
                int detaWithPen = (int)((scheduledResetTime - startTime)*1.5);
                if (detaWithPen>4){
                    detaWithPen = 4;
                }
                updateUpTime(detaWithPen);
            }
        }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
