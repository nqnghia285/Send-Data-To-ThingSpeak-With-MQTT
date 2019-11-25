package com.nqnghia.senddatatothingspeakwithmqtt;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends AppCompatActivity {
    // Parameter ThingSpeak
    private static final String TAG = "UsingThingSpeakAPI";
    private static final String THINGSPEAK_CHANNEL_ID = "866855";
    private static final String THINGSPEAK_WRITE_API_KEY = "R8MCYSHH20RIBJWV";
    private static final String THINGSPEAK_READ_API_KEY = "QTRKZPHW65T7IHP2";
    private static final String THINGSPEAK_MQTT_API_KEY = "5YSL39EJ2T6DQBDP";

    private static final String THINGSPEAK_SERVER = "mqtt.thingspeak.com";
    private static final int THINGSPEAK_PORT = 1883;
    private static final String THINGSPEAK_SUBSCRIBE_FIELD1 = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/subscribe/fields/field1/" + THINGSPEAK_READ_API_KEY;
    private static final String THINGSPEAK_SUBSCRIBE_FIELD2 = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/subscribe/fields/field2/" + THINGSPEAK_READ_API_KEY;
    private static final String THINGSPEAK_SUBSCRIBE_FIELDS = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/subscribe/fields/+/" + THINGSPEAK_READ_API_KEY;
    private static final String THINGSPEAK_PUBLISH_FIELD1 = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/publish/fields/field1/" + THINGSPEAK_WRITE_API_KEY;
    private static final String THINGSPEAK_PUBLISH_FIELD2 = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/publish/fields/field2/" + THINGSPEAK_WRITE_API_KEY;
    private static final String THINGSPEAK_PUBLISH_FIELDS = "channels/" + THINGSPEAK_CHANNEL_ID +
            "/publish/" + THINGSPEAK_WRITE_API_KEY;

    private static final String THINGSPEAK_FEEDS = "/feeds.json?api_key=";
    private static final String THINGSPEAK_FIEDS = "/fields/1.json?api_key=";
    private static final String THINGSPEAK_FIELD1 = "&field1=";
    private static final String THINGSPEAK_FIELD2 = "&field2=";
    private static final String THINGSPEAK_RESULTS = "&results=2";
    private static final String THINGSPEAK_UPDATE_URL = "https://api.thingspeak.com/update?api_key=";
    private static final String THINGSPEAK_CHANNEL_URL = "https://api.thingspeak.com/channels/";

    private static final int MAX_NUBMER = 50;
    private static final int POST_TIME = 50;
    private static Random random;

    // MQTT
    private MqttHelper mqttHelper;
    private static final int QoS0 = 0;
    private MqttAndroidClient client;
    private MqttMessage message;
    private static Boolean StartedFlag;
    private static Boolean Connected = false;

    private static TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.textView);
        text.setText("Send data to ThingSpeak");
        ///////////////////////
        random = new Random();
    }

    @Override
    protected void onStart() {
        super.onStart();

        StartedFlag = false;
        mqttHelper = new MqttHelper(getApplicationContext(), THINGSPEAK_SERVER, THINGSPEAK_PORT, MqttClient.generateClientId(), THINGSPEAK_MQTT_API_KEY);
        mqttHelper.connect();
        mqttHelper.setMqttHandler(new MqttHelper.MqttHandler() {
            @Override
            public void handle(String topic, final MqttMessage message) {
                if (StartedFlag) {
                    encoding(topic, message);
                } else {
                    StartedFlag = true;
                }
            }
        });
        mqttHelper.setMqttSubscribe(new MqttHelper.MqttSubscribe() {
            @Override
            public void setSubscribe(IMqttToken asyncActionToken) {
                mqttHelper.subscribe(THINGSPEAK_SUBSCRIBE_FIELDS, QoS0);
            }
        });

        setupThingSpeakTimer();
    }

    private void encoding(String topic, MqttMessage message) {
        if (topic.contains("field1")) {
            Log.d("field1", message.toString());
        } else if (topic.contains("field2")) {
            Log.d("field2", message.toString());
        }
    }

    // ThingSpeak
    private void setupThingSpeakTimer() {
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                final int number1 = random.nextInt(MAX_NUBMER);
                final int number2 = random.nextInt(MAX_NUBMER);
                Log.d("Number", number1 + " " + number2);
                sendMessage(String.valueOf(number1), String.valueOf(number2), THINGSPEAK_PUBLISH_FIELDS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setTextColor(Color.rgb(
                                random.nextInt(255),
                                random.nextInt(255),
                                random.nextInt(255)));
                        text.setText("Message:\n" + "Field1: " + number1 + "\nField2: " + number2);
                    }
                });
            }
        };
        Timer aTimer = new Timer();
        aTimer.schedule(aTask, 3000, 100*POST_TIME);
    }

    private void sendMessage(String value1, String value2, String topic) {
        String ms = "field1=" + value1 + "&field2=" + value2 + "&status=MQTTPUBLISH";
        mqttHelper.publish(topic, ms.getBytes());
    }
}
