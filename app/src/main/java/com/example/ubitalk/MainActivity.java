package com.example.ubitalk;

import android.content.Intent;
import android.graphics.Color;
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
import java.util.ArrayList;

import android.util.Log;

public class MainActivity extends WearableActivity
{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private int n_filler;
    TextView n_filler_text;
    Button start;
    Button stop;
    ConstraintLayout layout;
    Handler customHandler;

    String[] colors = new String[]{"#61F013", "#8FF011", "#BFF00F", "#EFF00E", "#F0C00C",
            "#F08F0B", "#F05D09", "#F02A08", "#F00616", "#EF0546"};
    int col_index = 0;

    private TextView mText;
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
        //start.setOnClickListener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                col_index = 0;
                n_filler = 0;
                customHandler.postDelayed(updateTimerThread, 0);

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-AU");
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
                //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                sr.startListening(intent);

                Log.i("111111","11111111");

            }

        });

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                //n_filler_text.setText("");
                layout.setBackgroundColor(Color.DKGRAY);
                customHandler.removeCallbacksAndMessages(null);
            }
        });


        // Enables Always-on
        setAmbientEnabled();
    }
    class listener implements RecognitionListener
    {
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
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            mText.setText("results: "+String.valueOf(data));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    private void changeColors() {
        layout.setBackgroundColor(Color.parseColor(colors[col_index]));
        //mText.setText(Integer.toString(n_filler));
        if (col_index < 9) {
            col_index++;
            n_filler++;
        }
    }

    private Runnable updateTimerThread = new Runnable()
    {
        public void run()
        {
            changeColors();
            customHandler.postDelayed(this, 1000);
        }
    };
}


