package io.github.kylelam.appleanddroid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class game extends ActionBarActivity {

    //settings
    final int FPS = 60;
    final int TOTAL_TIME_IN_SEC = 30;
    final int TOTAL_WIDTH = 4;      //gameboard size
    final int TOTAL_HEIGHT = 5;
    final int MAX_UP_TIME = 2;
    final int BAR_NORMAL_COLOR = 0xFF42C0FB;
    final int BAR_TIMEUP_COLOR = 0xFFFF3333;

    //debug
    private static final String TAG = "GameActivity";

    //global variables
    final int TOTAL_TIME = FPS * TOTAL_TIME_IN_SEC;    //in fps
    int total_score = 0;
    int combo = 0;
    int maxCombo = 0;
    int shellOnBoard = TOTAL_WIDTH * TOTAL_HEIGHT;
    int currentTime = 0;  //in fps
    boolean shouldResume = false;


    //game variables
        //how long to wait until next shell appear
    int nextAddTime = 0;

        //shell up time in seconds, how long does a shell stay on screen
    double upTime = MAX_UP_TIME;

        //up random shell at rate, how often does a shell appear
        //double rate = 0.5;
    double rate = upTime*0.75;      //1.0/(#shellToCatch/catchUpTime+1/upTime);


    //references
    Vibrator my_vibrator;
    ProgressBar progressBar;
    TextView scoreView;
    ImageView [] imageViews = new ImageView[TOTAL_WIDTH*TOTAL_HEIGHT];
    Shell [] shells = new Shell[TOTAL_WIDTH*TOTAL_HEIGHT];
    int oldOrientation;
    Random randomNumberGenerator = new Random();
    private Handler gameLoopHandler = new Handler();

    //Animation [] popUpArray = new Animation[TOTAL_WIDTH*TOTAL_HEIGHT];
    //Animation [] popDownArray = new Animation[TOTAL_WIDTH*TOTAL_HEIGHT];


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

        total_score = 0;

        //disable rotation
        oldOrientation = getRequestedOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        //the imageView Onclick listener for ALL images. will check which image view it comes from and
        // call the responding Shell object's clicked method.
        View.OnClickListener imageViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View imageView) {
                int index = java.util.Arrays.asList(imageViews).indexOf(imageView);
                shells[index].clicked();
                /*Toast.makeText(game.this,
                        "This is index: " + Integer.toString(index),   //+Integer.toString(countI)+" j:"+Integer.toString(countJ),
                        Toast.LENGTH_SHORT).show();*/
            }
        };


        //building layout- main frame- level 0
        LinearLayout gameLayout = new LinearLayout(this);
        gameLayout.setOrientation(LinearLayout.VERTICAL);

        // progressBar for time left - level 1
        LinearLayout progressBarLinearLayout = new LinearLayout(this);
        progressBarLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        progressBarLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f));
        //  progress bar- level 2
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        progressBar.setMax(TOTAL_TIME);
        progressBar.setProgress(0);


        progressBarLinearLayout.addView(progressBar);
        gameLayout.addView(progressBarLinearLayout);

        // score and other information- level 1
        scoreView = new TextView(this);
        scoreView.setText("0");
        scoreView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f));
        gameLayout.addView(scoreView);


        // creating images as well as classes
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

                //** creating shell class, and relating them to the imageView
                shells[countJ * TOTAL_WIDTH + countI] = new Shell(imageView, false);
                shells[countJ * TOTAL_WIDTH + countI].shellNumber=countJ * TOTAL_WIDTH + countI;

            }

            gameLayout.addView(linearLayout);
        }
        //make visible to program
        setContentView(gameLayout);
        startGameLoop();

        //set up vibrator
        my_vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        my_vibrator.vibrate(500);

        //set up animation
        /*for (int i =0; i < TOTAL_HEIGHT*TOTAL_WIDTH; i++) {

            popup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
            popdown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);

        }*/
    }


    private void startGameLoop(){
        //to start the gameLoop
        currentTime=0;
        gameLoopHandler.postDelayed(gameLoop, 1000/FPS);
    }

    private void endGame(){
        //to end game and show result

        if (maxCombo < combo){
            maxCombo = combo;
        }

        Intent intent = new Intent(this, ResultActivity.class);
        Bundle extras = new Bundle();
        extras.putInt("EXTRA_SCORE", total_score);
        extras.putInt("EXTRA_MAX_COMBO", maxCombo);
        intent.putExtras(extras);
        startActivity(intent);

        /*
        Intent intent = new Intent(this, ResultActivity.class)
                .putExtra(Intent.EXTRA_TEXT, "Score");
        startActivity(intent);
        */
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

                //Log.v(Integer.toString(shellOnBoard),"debug: shellOnBoard: ");
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


            /*
            if ((currentTime%(FPS*15))==0) {
                Toast.makeText(game.this,
                        "hello from timer",
                        Toast.LENGTH_SHORT).show();
            }
            */

            /* and here comes the "trick" */
            currentTime++;
            progressBar.setProgress(currentTime);
            scoreView.setText( "Score: " + Integer.toString(total_score) + "          Comb: " + Integer.toString(combo));

            if (currentTime <= TOTAL_TIME)
                gameLoopHandler.postDelayed(this, 1000/FPS);
            else
                endGame();
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
        double percent = 0.15;
        upTime = (upTime*(1-percent) + deta*(percent))*1.1;
        if (upTime<0.5){
            upTime = 0.5;
        }
        //Log.v(Double.toString(upTime),"debug: upTime");
        updateRate();
    }



    class Shell{
        boolean isAnimating = false;
        boolean isClicked = false;
        boolean isBlank = true;
        ImageView imageView;
        int scheduledResetTime = 0;
        int startTime = 0;
        Animation popUp;
        Animation popDown;
        public int shellNumber=0;

        public Shell(ImageView imageView, boolean isBlank){
            this.isBlank = isBlank;
            this.imageView = imageView;
            popUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
            popUp.setDuration(100);
            popDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
            popDown.setDuration(100);

            //popUp.setFillAfter(true);
            //popDown.setFillAfter(true);
            //popUp.
        }

        public void setClickable(boolean clickable){
            imageView.setClickable(clickable);
        }

        public void setImageResource(int drawable){
            imageView.setImageResource(drawable);   //R.drawable.android
        }

        public void startAnime(boolean up){
            if (up){
                isClicked=false;
                setUnlocked(this.imageView);
                startTime = currentTime;
                isBlank = false;
                setClickable(true);
                scheduledResetTime = (int)(FPS * upTime + 0.5) + currentTime;
                shellOnBoard = shellOnBoard + 1;

                //Log.v(Integer.toString(shellOnBoard), "debug: shellOnBoard: +1");
                //do real anime works
                imageView.setVisibility(View.VISIBLE);

                //Log.v(TAG, shellNumber + ": up");
                imageView.postOnAnimation(new Runnable(

                ) {
                    @Override
                    public void run() {

                        //Log.v(TAG, shellNumber + ": up-postOn");
                    }
                });
                imageView.startAnimation(popUp);

                isAnimating = false;
            } else {
                isAnimating = true;
                //do real anime works
                //Log.v(TAG, shellNumber + ": down-b4");
                imageView.postOnAnimation(new Runnable(

                ) {
                    @Override
                    public void run() {
                        //Log.v(TAG, shellNumber + ": down-postOn");
                        if ((!isClicked) && (scheduledResetTime == Integer.MAX_VALUE)) {
                            //Log.v(TAG, shellNumber + ": combo=0: " + combo);
                            if (maxCombo< combo){
                                maxCombo=combo;
                            }
                            combo = 0;
                            //Log.v(TAG, shellNumber + ": scheduledResetTime: " + scheduledResetTime);
                            //Log.v(TAG, shellNumber + ": currentTime: " + currentTime);
                        }

                        //Log.v(TAG, shellNumber + ": down");
                        isClicked = false;
                    }
                });

                imageView.startAnimation(popDown);
                //Log.v(TAG, shellNumber + ": down-after-VISIBLE");
                imageView.setVisibility(View.INVISIBLE);
                //Log.v(TAG, shellNumber + ": down-after-INVISIBLE");

                //on real anime finished
                //should happen when the image is completely gone
                //setClickable(false);
                scheduledResetTime = Integer.MAX_VALUE;
                isBlank = true;
                shellOnBoard = shellOnBoard - 1;
                //Log.v(Integer.toString(shellOnBoard),"debug: shellOnBoard: -1");

            }
        }


        public void clicked() {

            isClicked=true;
            combo++;

            //Log.v(TAG, shellNumber+": clicked, combo++: "+combo );

            setClickable(false);
            setLocked(this.imageView);
            if (! isAnimating) {
                startAnime(false);
            }
            int endTime = currentTime;
            updateUpTime(startTime - endTime);

            my_vibrator.vibrate(50);

            total_score += Math.round(100.0 * (1.0 + combo*combo/10.0));


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



    public static void  setLocked(ImageView v)
    {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
        v.setAlpha(128);   // 128 = 0.5
    }

    public static void  setUnlocked(ImageView v)
    {
        v.setColorFilter(null);
        v.setAlpha(255);
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
