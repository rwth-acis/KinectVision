package de.rwth.i5.kinectvision.mqtt;


import de.rwth.i5.kinectvision.Counter;
import de.rwth.i5.kinectvision.machinevision.FrameHandler;
import de.rwth.i5.kinectvision.machinevision.FrameSource;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.nustaq.serialization.FSTConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Class used as an MQTT client for the Kinect data. It subscribes to the data generated by the Kinect.
 */
@Slf4j
public class KinectClient implements FrameSource {
    @Setter
    private FrameHandler frameHandler;
    @Setter
    private String broker;
    @Setter
    private String clientId;


    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    static {
        conf.registerClass(DepthModel.class);
    }

    private static byte[] decompress(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = 0;
            try {
                count = inflater.inflate(buffer);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
            outputStream.write(buffer, 0, count);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public static short[] byteToShortArray(byte[] bytes) {
        short[] res = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(res);
        return res;
    }

    /**
     * Initialization method for the MQTT client. Connects to the broker.
     *
     * @throws MqttException
     */
    public void initialize() throws MqttException {
        //Initialize the Mqtt client and establish a connection
        MqttClient kinectClient = new MqttClient(broker, clientId, null);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        kinectClient.connect(connOpts);
        log.info("Connected to the MQTT broker {}.", broker);
        //Subscribe to the topics
        kinectClient.subscribe("infrared", 0);
        kinectClient.subscribe("depth", 0);
        kinectClient.setCallback(new MqttCallback() {
            /**
             * What should happen if the connection has lost?
             *
             * @param throwable
             */
            @Override
            public void connectionLost(Throwable throwable) {
                //TODO: What should happen here? Maybe the robot should stop?
                throwable.printStackTrace();
            }

            /**
             * Callback method for a message that has arrived from the Kinect.
             *
             * @param topic       The topic in which the message has been published
             * @param mqttMessage The message object
             * @throws Exception
             */
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

//                try {
////                    System.out.println("send");
//                    MqttTopic mqttTopic = kinectClient.getTopic("ping");
//                    mqttTopic.publish(mqttMessage);
////                    kinectClient.publish(mqttTopic, "1".getBytes(), 0, false);
////                    System.out.println("sent.");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                if (mqttMessage == null || mqttMessage.getPayload() == null)
                    return;

                //Decompress the payload
//                byte[] payload = decompress(mqttMessage.getPayload());
                byte[] payload = mqttMessage.getPayload();
//                log.debug("Message arrived. Topic: {}, message: {}", topic, mqttMessage.toString());

                switch (topic) {
                    case "depth":
                        //Depth stuff here
//                        log.debug("Depth frame");
                        //Deserialize
                        DepthModel depth = (DepthModel) conf.asObject(payload);
//                        Counter.time = System.currentTimeMillis();
                        //Handle
                        frameHandler.onDepthFrame(depth);
                        break;
                    case "infrared":
                        //Infrared stuff here
                        frameHandler.OnInfraredFrame(byteToShortArray(payload));
                        break;
                    case "color":
                        frameHandler.onColorFrame(payload);
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                //TODO: Should something happen here? I do not think so.
            }
        });
    }
}