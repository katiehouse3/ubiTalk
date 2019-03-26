package com.example.ubitalk;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public boolean app = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextView = (TextView) findViewById(R.id.text);

        final Button start = findViewById(R.id.button);
        //final Button stop = findViewById(R.id.button2);

        final TextView speech = (TextView) findViewById(R.id.textView);


        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        //stop.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //    }
        //});

        // Enables Always-on
        setAmbientEnabled();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }



}
