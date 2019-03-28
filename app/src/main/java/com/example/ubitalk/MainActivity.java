package com.example.ubitalk;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.frameLayout);

        start = findViewById(R.id.button);
        stop = findViewById(R.id.button2);

        stop.setVisibility(View.INVISIBLE);

        n_filler_text = (TextView) findViewById(R.id.text);
        n_filler_text.setTextColor(Color.BLACK);

        customHandler = new android.os.Handler();

        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                col_index = 0;
                n_filler = 0;
                customHandler.postDelayed(updateTimerThread, 0);
            }

        });

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                n_filler_text.setText("");
                layout.setBackgroundColor(Color.DKGRAY);
                customHandler.removeCallbacksAndMessages(null);
            }
        });


        // Enables Always-on
        setAmbientEnabled();
    }

    private void changeColors() {
        layout.setBackgroundColor(Color.parseColor(colors[col_index]));
        n_filler_text.setText(Integer.toString(n_filler));
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


