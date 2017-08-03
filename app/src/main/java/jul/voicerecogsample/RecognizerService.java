package jul.voicerecogsample;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by JuL on 2017. 8. 3..
 */

public class RecognizerService extends Service {

    private static String ACTION_START_FOREGROUND = "jul.voicerecogsample.start";
    private static String ACTION_STOP_FOREGROUND = "jul.voicerecogsample.stop";

    private final int MSG_END = 1;
    private final int MSG_RESTART = 2;


    private SpeechRecognizer speechRecognizer;

    private boolean isRunning = false;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_END:
                    speechRecognizer.setRecognitionListener(null);
                    speechRecognizer.stopListening();

                    if(isRunning){
                        sendEmptyMessageDelayed(MSG_RESTART, 2000);
                    }
                    break;
                case MSG_RESTART:
                    if(isRunning){
                        startListening();
                    }
                    break;
            }
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, RecognizerService.class);
        intent.setAction(ACTION_START_FOREGROUND);
        context.startService(intent);

    }

    public static void stop(Context context){
        Intent intent = new Intent(context, RecognizerService.class);
        intent.setAction(ACTION_STOP_FOREGROUND);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startListening()
    {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.e("test", "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.e("test", "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.e("test", "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                Log.e("test", "onEndOfSpeech");
            }

            @Override
            public void onError(int error) {
                Log.e("test", "onError : "+error);
                handler.sendEmptyMessage(MSG_END);
            }

            @Override
            public void onResults(Bundle results) {
                Log.e("test", "onResults");
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(data != null){
                    Log.e("test", data.toString());
                    Toast.makeText(RecognizerService.this, data.toString(), Toast.LENGTH_SHORT).show();
                }


                handler.sendEmptyMessage(MSG_END);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.e("test", "onPartialResults");
                ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(data != null){
                    Log.e("test", data.toString());
                    Toast.makeText(RecognizerService.this, data.toString(), Toast.LENGTH_SHORT).show();
                }


                handler.sendEmptyMessage(MSG_END);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.e("test", "onEvent");
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko_KR");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko_KR");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        speechRecognizer.startListening(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(ACTION_START_FOREGROUND.equalsIgnoreCase(intent.getAction())){
            isRunning = true;
            handler.sendEmptyMessage(MSG_RESTART);
        } else if(ACTION_STOP_FOREGROUND.equalsIgnoreCase(intent.getAction())){
            isRunning = false;
            speechRecognizer.setRecognitionListener(null);
            speechRecognizer.stopListening();
        }
        return START_STICKY;
    }
}
