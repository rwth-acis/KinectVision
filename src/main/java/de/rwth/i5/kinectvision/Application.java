package de.rwth.i5.kinectvision;


import de.rwth.i5.kinectvision.analysis.Evaluation;
import de.rwth.i5.kinectvision.mqtt.KinectClient;
import de.rwth.i5.kinectvision.mqtt.KinectHandler;
import de.rwth.i5.kinectvision.robot.Robot;
import de.rwth.i5.kinectvision.robot.RobotClient;
import de.rwth.i5.kinectvision.robot.serialconnection.SerialPortConnectorKRC2;
import de.rwth.i5.kinectvision.robot.serialconnection.SerialPortException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;

/**
 * Main class for starting
 */
@Slf4j
public class Application {
    private static final String KINECT_URL = "tcp://localhost:1883";

    /**
     * Calculate the minimum distance for the evaluation
     *
     * @return The calculated value
     */
    private static double calculateS() {
        //Human
        double tM = 0.033; //seconds
        double vMaxM = 1.600; //meters per second
        double sH = tM * vMaxM;
        //Robot
        double tR = 0.05;
        double tS = 0.05;
        double vMaxR = 1;
        double sR = tR * vMaxR;
        double sS = tS * vMaxR;
        //Parameter C
        double C = 0;
        //Uncertainty
        double zD = 0;
        double zR = 0;
        //Security distance
        double S = sH + sR + sS + C + zD + zR;
        return S;
    }

    public static void main(String[] args) {
        System.out.println("Guten Tag.");
        //Set up mqtt client for kinect
        KinectClient kinectClient = new KinectClient();
        kinectClient.setBroker("tcp://localhost:1883");
        kinectClient.setClientId("asdf");

        //Set the frame handler
        KinectHandler handler = new KinectHandler();
        kinectClient.setFrameHandler(handler);

        /*
        Initialize the Robot
         */
        Robot robot = new Robot();
        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\test.x3d"));
        handler.setRobot(robot);
        /*
         * Initialize the robot connection
         */

        SerialPortConnectorKRC2 serialPortConnectorKRC2;
        try {
            serialPortConnectorKRC2 = new SerialPortConnectorKRC2(0);
        } catch (SerialPortException e) {
            e.printStackTrace();
            log.error("Initialization of Robot Connector failed.");
            return;
        }

        RobotClient robotClient = new RobotClient(robot, serialPortConnectorKRC2);
        serialPortConnectorKRC2.setRobotHandler(robotClient);

        Evaluation evaluation = new Evaluation(robotClient, robot, calculateS());
        handler.setEvaluation(evaluation);


        try {
            serialPortConnectorKRC2.connect();
        } catch (SerialPortException e) {
            e.printStackTrace();
            log.error("Connection with robot failed.");
            return;
        }
        try {
            //Connect to kinect
            kinectClient.initialize();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        while (true) {

        }
    }
}