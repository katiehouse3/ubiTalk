package com.example.ubitalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.speech.RecognitionListener;

import java.io.File;
import java.util.ArrayList;

import android.util.Log;

public class MainActivity extends WearableActivity
{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private int n_words;
    private int n_words_total;
    Button start;
    Button stop;
    ConstraintLayout layout;
    Handler customHandler;

    final String BLUE = "#448cff";
    final String RED = "##ea072c";
    final String GREEN = "#08c935";

    final int slow = 1;
    final int fast = 3;

    String speed = "On pace";
    TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "MyStt3Activity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.frameLayout);

        start = findViewById(R.id.button_speaking);
        stop = findViewById(R.id.button2);

        stop.setVisibility(View.INVISIBLE);

        //n_filler_text = (TextView) findViewById(R.id.text);
        //n_filler_text.setTextColor(Color.BLACK);

        customHandler = new android.os.Handler();
        mText = (TextView) findViewById(R.id.textView1);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                n_words = 0;
                n_words_total = 0;
                //customHandler.postDelayed(updateTimerThread, 0);

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
                //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

                requestRecordAudioPermission();
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra("android.speech.extra.DICTATION_MODE", true);
                sr.startListening(intent);

                Log.i("111111","11111111");

            }

        });

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                mText.setText("");
                layout.setBackgroundColor(Color.DKGRAY);
                customHandler.removeCallbacksAndMessages(null);
                sr.stopListening();
                //sr.cancel();
                //sr.destroy();
            }
        });


        // Enables Always-on
        setAmbientEnabled();
    }
    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);
            mText.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            /*
            mText.setText("results: "+String.valueOf(data));
            n_words = data.size();
            n_words_total += n_words;
            String curr_speed = checkSpeed(n_words_total, 5000);
            if (curr_speed != speed) {
                speed = curr_speed;
                changeColor(speed);
            }
            */
            mText.setText(data.get(0));

        }
        @Override
        public void onPartialResults(Bundle partialResults)
        {
            ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            mText.setText(word);

            Log.i("TEST", "partial_results: " + word);
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }

    }

    private void changeColor(String speed) {
        switch(speed) {
            case "Slow":
                layout.setBackgroundColor(Color.parseColor(BLUE));
                break;
            case "Fast":
                layout.setBackgroundColor(Color.parseColor(RED));
                break;
            default:
                layout.setBackgroundColor(Color.parseColor(GREEN));
        }
    //mText.setText(Integer.toString(n_filler));
}

    //interval supplied in milliseconds
    private String checkSpeed(int n_words, int interval) {
        int curr_speed = 0;
        long time = System.currentTimeMillis();
        long update = 0;

        if ((time - update) > interval) {
            curr_speed = n_words / 60000; //calculate words per minute
    }

    if (curr_speed <= slow){
        return "Slow";
    } else if (curr_speed > slow && curr_speed < fast) {
        return "On pace";
    } else {
        return "Fast";
    }
    }

    //private Runnable updateTimerThread = new Runnable()
    //{
     //   public void run()
     //   {
     //       changeColors();
     //       customHandler.postDelayed(this, 1000);
     //   }
    //};

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

}


