package com.example.group5_iotproject_mnz;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;

    //Not needed because what they do is set in Activity_main.xml in their attributes
   // private Button voiceCommandButton;
   // private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask permission for using recording
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //Getting the textview from ID
        textView = findViewById(R.id.infoTextView);

        //Buttons Not needed because of made their methods connected through their attribute in xml
        //voiceCommandButton = findViewById(R.id.voiceCommandButton);
        //stopButton = findViewById(R.id.stopButton);

        //Creating and initiating the necessary parts for speechrecognizer
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //Initiation of the listener automatically creates the different methods below.
        //Will primarily/only use onResults and error at least at first
        //If we remove buttons then maybe we will use other methods as well
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                textView.setText(getString(R.string.error_message_voiceCommand));
            }

            @Override
            public void onResults(Bundle bundle) {
                //Makes an arraylist of the input from the recognizer
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                //If the input is not nothing then it will create a string of the first input (on index 0)
                //and then set the text of the textview to the string (to see how the input turns out)
                //This will then be changed to make the lights change depending on the voice commands
                if(matches != null) {
                    string = matches.get(0);
                    textView.setText(string);
                } else {
                    textView.setText(getString(R.string.error_message_noInput_voiceCommand));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    //Method that starts when "Voice Command" button is pressed
    //It will start listening for the voice commands
    public void StartButton(View view){
        speechRecognizer.startListening(intentRecognizer);
    }

    //This method is connected to the stop button and will
    //make the speechrecognizer stop listening for input
    public void StopButton(View view) {
        speechRecognizer.stopListening();
    }

}