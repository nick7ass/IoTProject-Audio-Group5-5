package com.example.group5_iotproject_mnz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Mqtt client and mosquitto for connection with broker
    private MqttAndroidClient client;

    //SERVER_URI holds the IP address of the Raspberry Pi as it has the broker on it.
    //The tcp is first the IPv4 address followed by the port used
    private static final String SERVER_URI = "tcp://192.168.0.83:1883";

    //TAG variable used for logging/debugging purposes. Not completely necessary for this project in particular but using such tags is a good habit to build
    private static final String TAG = "MainActivity";

    //Variable to hold voiceCommands
    private String voiceCommandFinal = "";

    //Variables for the speech recognizer:
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;

    //Simply used for debugging purposes/making sure everything works properly on this end (Hidden in UI if not debugging)
    //private TextView textView;
    //ImageView lightImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask permission for using recording on device
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //Getting the textview and imageview from id (only used for debugging purposes)
        //textView = findViewById(R.id.infoTextView);
        //lightImage = findViewById(R.id.lightImage);

        //Creating and initiating the necessary parts for speechrecognizer
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //Initiation of the listener automatically creates the different methods below.
        //Will primarily/only use onResults
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

            }

            @Override
            public void onResults(Bundle bundle) {
                //Makes an arraylist of the input from the recognizer
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                //String voiceCommand = "";

                //If the input is not null then it will create a string of the first input (on index 0)
                //and then set the text of the textview to the string (to see how the input turns out)
                //This will then be changed to make the lights change depending on the voice commands
                if(matches != null) {
                    String voiceCommand = matches.get(0);

                    //Change the light depending on input by setting the voiceCommandFinal variable:
                    //Commented out Image and Textview used for debugging purposes
                    if (voiceCommand.contains("dim")) {
                        voiceCommandFinal = "dimmed";
                        //lightImage.setImageResource(R.drawable.lightdim);
                        //textView.setText("Lights dimmed.");
                    }
                    else if (voiceCommand.contains("off")) {
                        voiceCommandFinal = "off";
                        //lightImage.setImageResource(R.drawable.lightoffcompletely);
                        //textView.setText("Lights turned off.");
                    }
                    else if (voiceCommand.contains("on")) {
                        voiceCommandFinal = "on";
                       //lightImage.setImageResource(R.drawable.lightonwarm);
                        //textView.setText("Lights set to 'On' but warmer since it's late.");
                    }
                }
                //If something goes wrong write out error message
                //else {//textView.setText(getString(R.string.error_message_noInput_voiceCommand));}

                //Starts publish method with the voice command as parameter:
                publish(voiceCommandFinal);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        //Calling method connect below for connection with broker
        connect();

        //Callback. What to do when connected
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                } else {
                    System.out.println("Connected to: " + serverURI);
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws
                    Exception {
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
//End of callback section

    }
    //OnCreate slut



    //Initializing and creating connection to the broker set in "SERVER_URI", in this case the IPv4 of the Raspberry pi with the broker and the port
    private void connect(){
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), SERVER_URI,
                        clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Connection successful:
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    //If something went wrong when trying to connect:                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //End of connect method

    //Method for publishing to topic
    //Takes the String lightMode which will be the mode for the light.
    private void publish(String lightMode) {
        String topic = "lightModeTopic";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(lightMode.getBytes());
        try {
            System.out.println("Voice command: " + lightMode + " was sent.");
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //End of method publish


    //Method that starts when "Voice Command" button is pressed
    //It will start listening for the voice command
    public void StartButton(View view){
        speechRecognizer.startListening(intentRecognizer);
    }
    //End of method that starts when "Voice Command" button is pressed

}