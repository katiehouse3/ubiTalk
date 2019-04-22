package com.example.ubitalk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ubitalk.R;

import java.util.ArrayList;

public class MainActivity extends WearableActivity {

    Button speak, stop;
    static TextView textView;

    private SpeechRecognizer sr;
    private static final String TAG = "MyActivity";
    ProgressDialog dialog;
    int code;
    private Messenger mServiceMessenger;
    boolean isEndOfSpeech = false;
    boolean serviceconneted;

    static final Integer LOCATION = 0x1;

    // Layout
    Handler customHandler;
    static ConstraintLayout layout;

    // Layout Colors
    final String BLUE = "#448cff";
    final String RED = "##ea072c";
    final String GREEN = "#08c935";

    // Speed of speech
    final int slow = 1;
    final int fast = 3;
    private int n_words;
    private int n_words_total;


    String speed = "On pace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.frameLayout);

        speak = findViewById(R.id.speak);
        stop = findViewById(R.id.stop);

        textView = findViewById(R.id.write);

        customHandler = new android.os.Handler();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        stop.setVisibility(View.INVISIBLE);



        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, com.example.ubitalk.MyService.class);
                stopService(i);
                Toast.makeText(MainActivity.this, "stop speaking", Toast.LENGTH_SHORT).show();
                textView.setText("");
                speak.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                layout.setBackgroundColor(Color.DKGRAY);
            }
        });

        sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        sr.setRecognitionListener(new Listner());

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    askForPermission(Manifest.permission.RECORD_AUDIO, LOCATION);
                }
                Intent i = new Intent(MainActivity.this, com.example.ubitalk.MyService.class);
                bindService(i, connection, code);
                startService(i);
                Toast.makeText(MainActivity.this, "Start Speaking", Toast.LENGTH_SHORT).show();
                speak.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                layout.setBackgroundColor(Color.DKGRAY);

            }
        });
    }

    class Listner implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i("Speech", "ReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.i("Speech", "beginSpeech");

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.i("Speech", "onrms");

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i("Speech", "onbuffer");

        }

        @Override
        public void onEndOfSpeech() {
            isEndOfSpeech = true;

        }

        @Override
        public void onError(int error) {
            Log.i(TAG, "error " + error);
            if (!isEndOfSpeech) {
                return;
            }
            Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            //ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //String word = (String) data.get(data.size() - 1);
            //textView.setText(word);
            String str = new String();
            Log.i(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.i(TAG, "result " + data.get(i));
                str += data.get(i);
            }

            textView.setText("results: "+String.valueOf(data));
            n_words = data.size();
            n_words_total += n_words;
            String curr_speed = checkSpeed(n_words_total, 5000);
            if (curr_speed != speed) {
                speed = curr_speed;
                changeColor(speed);
            }

            dialog.dismiss();

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            /*
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            textView.setText(word);
            */
            String str = new String();
            ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.i(TAG, "result " + data.get(i));
                str += data.get(i);
            }

            textView.setText("results: "+String.valueOf(data));
            n_words = data.size();
            n_words_total += n_words;
            Log.i(TAG,  "words " +  n_words);
            String curr_speed = checkSpeed(n_words_total, 5000);
            if (curr_speed != speed) {
                speed = curr_speed;
                changeColor(speed);
            }

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }

    private void startListening(String speed) {
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
    private String checkSpeed(int n_words, int interval) {
        int curr_speed = 0;
        long time = System.currentTimeMillis();
        long update = 0;
        Log.i(TAG,  "time " +  time);
        Log.i(TAG,  "interval " +  interval);

        if ((time - update) > interval) {
            curr_speed = n_words / 60000; //calculate words per minute
            Log.i(TAG,  "curr_speed " +  curr_speed);
        }

        if (curr_speed <= slow){
            return "Slow";
        } else if (curr_speed > slow && curr_speed < fast) {
            return "On pace";
        } else {
            return "Fast";
        }
    }
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            }

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }



    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d("service", "connected");

            mServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = com.example.ubitalk.MyService.MSG_RECOGNIZER_START_LISTENING;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceconneted = false;
            Log.d("service", "disconnetd");
        }
    };
}

