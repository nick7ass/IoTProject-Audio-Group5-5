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
import android.widget.ImageView;
import android.widget.TextView;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;

    //Imageview of lightbulb
    ImageView lightImage;

    //Not needed because what they do is set in Activity_main.xml in their attributes
   // private Button voiceCommandButton;
   // private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask permission for using recording
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //Getting the textview and imageview from ID
        textView = findViewById(R.id.infoTextView);
        lightImage = findViewById(R.id.lightImage);

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
                //textView.setText(getString(R.string.error_message_voiceCommand));
            }

            @Override
            public void onResults(Bundle bundle) {
                //Makes an arraylist of the input from the recognizer
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String voiceCommand = "";
                //If the input is not nothing then it will create a string of the first input (on index 0)
                //and then set the text of the textview to the string (to see how the input turns out)
                //This will then be changed to make the lights change depending on the voice commands
                if(matches != null) {
                    //Here code for Actuating/Simulating actuation should be
                    //Ta fram en method som typ kollar om stringen innehåller "dim" så ska
                    //ljuset bli dimmat men om det innehåller off så ska det off och on så on
                    //Lägg också till att det här ska anpassas utifrån tid?
                    voiceCommand = matches.get(0);
                    //textView.setText(voiceCommand);

                    //To be able to consider time: (Couldn't use "LocalTime" as we need API min of 22 to work since thats the only android phone available to us

                    //To get current time
                    Calendar calendar  = Calendar.getInstance();
                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

                    //Our time range for which to consider when controlling lights
                    int startHourRange = 6;
                    int endHourRange = 17;

                    //Change the light depending on input:

                    //Dim the lights if the command includes dim
                    if (voiceCommand.contains("dim")) {
                        lightImage.setImageResource(R.drawable.lightdim);
                        textView.setText("Lights dimmed.");
                    } //Turn off lights if command contains off
                    else if (voiceCommand.contains("off")) {
                        lightImage.setImageResource(R.drawable.lightoffcompletely);
                        textView.setText("Lights turned off.");
                    } //If it's daytime turn lights on bright
                    else if (voiceCommand.contains("on") && hourOfDay >= startHourRange && hourOfDay <= endHourRange) {
                        lightImage.setImageResource(R.drawable.lightonbright);
                        textView.setText("Lights turned on bright.");

                    } //If it's late then turn them on but warmer
                    else if (voiceCommand.contains("on")) {
                        lightImage.setImageResource(R.drawable.lightonwarm);
                        textView.setText("Lights set to 'On' but warmer since it's late.");
                    }
                } //If something goes wrong write out error message (Is this needed?)
                else {
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
    //Not sure we need this button
    /*public void StopButton(View view) {
        speechRecognizer.stopListening();
    }*/

}