package com.example.ubitalk;


import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {

    //Use audio manageer services
    static protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    //To turn screen On uitll lock phone
    PowerManager.WakeLock wakeLock;
    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static int identify = 0, result = 0;
    String Currentdata = null, newcurrent = null;

    // Layout
    Handler customHandler;
    ConstraintLayout layout;

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
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        Log.i("service", "oncreate");
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<MyService> mtarget;

        public IncomingHandler(MyService target) {
            mtarget = new WeakReference<MyService>(target);
        }


        @Override
        public void handleMessage(Message msg) {
            final MyService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        if (!mIsStreamSolo) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                            } else {
                                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            }
                            mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                        } else {
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        }
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown;

    {
        mNoSpeechCountDown = new CountDownTimer(10000, 2000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinish() {
                Log.i("speech", "FINISHED");
                mIsCountDownOn = false;
                Message message;
                message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
                try {
                    mServerMessenger.send(message);
                    message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                    mServerMessenger.send(message);
                } catch (RemoteException e) {

                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count down anymore
            Log.i("speech", "BEGINNING OF SPEECH");
            if (mIsCountDownOn) {
                Log.i("speech", "countdown on BEGINNING OF SPEECH");
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            Log.i("speech", "countdown off BEGINNING OF SPEECH");
            //Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            //Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onError(int error) {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

            Log.i("speech", "onPartialResults");
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            if (Currentdata == null) {
                MainActivity.textView.setText("" + word);
            } else {
                MainActivity.textView.setText(Currentdata + " " + word);
                //MainActivity.textView.setSelection(MainActivity.textView.getText().length());
            }

            newcurrent = MainActivity.textView.getText().toString();
            identify = 1;
            Log.i("patial", "" + word);


            n_words = data.size();
            n_words_total += n_words;
            Log.i("ADDEDDD",  "Number of Words: " +  n_words);
            String curr_speed = checkSpeed(n_words_total, 5000);
            if (curr_speed != speed) {
                speed = curr_speed;
                changeColor(speed);
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
                MainActivity.textView.setText("");
                mNoSpeechCountDown.start();
            }
            Log.i("service", "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {

            //Log.d(TAG, "onResults"); //$NON-NLS-1$
             //$NON-NLS-1$
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);

            if (result == 0) {
                MainActivity.textView.setText(word);
                Currentdata = MainActivity.textView.getText().toString();
            } else if (result == 1) {
                if (Currentdata != null) {
                    MainActivity.textView.setText(Currentdata + "\n" + word);
                    //MainActivity.textView.setSelection(MainActivity.textView.getText().length());
                }
            }
            Currentdata = MainActivity.textView.getText().toString();

            Log.i("service", "" + Currentdata);

            if (mIsListening == true) {
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }
            result = 0;
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.i("TEST", "MY SERVICE on RMS changed");
        }

    }
    private void startListening(String speed) {
        switch(speed) {
            case "Slow":
                MainActivity.layout.setBackgroundColor(Color.parseColor(BLUE));
                break;
            case "Fast":
                MainActivity.layout.setBackgroundColor(Color.parseColor(RED));
                break;
            default:
                MainActivity.layout.setBackgroundColor(Color.parseColor(GREEN));
        }
        //mText.setText(Integer.toString(n_filler));
    }
    private void changeColor(String speed) {
        switch(speed) {
            case "Slow":
                MainActivity.layout.setBackgroundColor(Color.parseColor(BLUE));
                break;
            case "Fast":
                MainActivity.layout.setBackgroundColor(Color.parseColor(RED));
                break;
            default:
                MainActivity.layout.setBackgroundColor(Color.parseColor(GREEN));
        }
        //mText.setText(Integer.toString(n_filler));
    }
    private String checkSpeed(int n_words, int interval) {
        int curr_speed = 0;
        long time = System.currentTimeMillis();
        long update = 0;
        Log.i("ADDED",  "time " +  time);
        Log.i("ADDED",  "interval " +  interval);

        if ((time - update) > interval) {
            curr_speed = n_words / 60000; //calculate words per minute
            Log.i("ADDED",  "curr_speed " +  curr_speed);
        }

        if (curr_speed <= slow){
            return "Slow";
        } else if (curr_speed > slow && curr_speed < fast) {
            return "On pace";
        } else {
            return "Fast";
        }
    }
}