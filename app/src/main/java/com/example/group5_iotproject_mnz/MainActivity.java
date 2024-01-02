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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //Mqtt client and mosquitto for connection with raspberry pi
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://192.168.0.81:1883";
            //"tcp://test.mosquitto.org:1883";
    private static final String TAG = "MainActivity";

    private String voiceCommandFinal;

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;

    //Imageview of lightbulb
    ImageView lightImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask permission for using recording
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //Getting the textview and imageview from ID
        textView = findViewById(R.id.infoTextView);
        lightImage = findViewById(R.id.lightImage);
        voiceCommandFinal = "dimmed";

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
                    /*Calendar calendar  = Calendar.getInstance();
                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

                    //Our time range for which to consider when controlling lights
                    int startHourRange = 6;
                    int endHourRange = 17;*/

                    //Change the light depending on input:

                    //Dim the lights if the command includes dim
                    if (voiceCommand.contains("dim")) {
                        voiceCommandFinal = "dimmed";
                        lightImage.setImageResource(R.drawable.lightdim);
                        textView.setText("Lights dimmed.");
                    } //Turn off lights if command contains off
                    else if (voiceCommand.contains("off")) {
                        voiceCommandFinal = "off";
                        lightImage.setImageResource(R.drawable.lightoffcompletely);
                        textView.setText("Lights turned off.");
                    } //If it's daytime turn lights on bright
                   /* else if (voiceCommand.contains("on") && hourOfDay >= startHourRange && hourOfDay <= endHourRange) {
                        voiceCommandFinal = "on";
                        lightImage.setImageResource(R.drawable.lightonbright);
                        textView.setText("Lights turned on bright."); } */

                //If it's late then turn them on but warmer
                    else if (voiceCommand.contains("on")) {
                        voiceCommandFinal = "on"; //removed "onWarm" and made it "on" because checks time in simulator but either works but redundant in both
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

        //Calling method connect below for connection with rasp
        connect();

        //Callback. What to do when connected
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    //subscribe(" YOUR-TOPIC"); //Byt ut yourtopic till the topic, typ iotproject/nmz/lights

                    //Testar publish här:
                    publish(voiceCommandFinal);
                    //Slut på test av publish här.
                } else {
                    System.out.println("Connected to: " + serverURI);
                    //Testar publish här:
                    publish(voiceCommandFinal);
                    //Slut på test av publish här.

                    //subscribe(" YOUR-TOPIC");
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

                 /* add code here to interact with elements
                 (text views, buttons)
                 using data from newMessage
                 */
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
//End of callback section

    }

    //Initializing and creating connection to the broker set in "SERVER_URI", in this case "tcp://test.mosquitto.org:1883"
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
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //End of connect method

    //Method for publishing to topic
    //Takes the String lightMode which will be the mode for the light, eg off, on, dimmed, etc.
    private void publish(String lightMode) {
        final String message = lightMode;
        int qos = 1;
        String topic = "lightModeTopic";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        try {

            client.publish(topic, mqttMessage);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //End of method publish


    //Method that starts when "Voice Command" button is pressed
    //It will start listening for the voice commands
    public void StartButton(View view){
        speechRecognizer.startListening(intentRecognizer);
    }

}