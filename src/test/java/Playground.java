import TestTools.KinectDataStore;
import TestTools.RobotSimulationClient;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import de.rwth.i5.kinectvision.analysis.Evaluation;
import de.rwth.i5.kinectvision.machinevision.FiducialFinder;
import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.PolygonMesh;
import de.rwth.i5.kinectvision.machinevision.model.Triangle;
import de.rwth.i5.kinectvision.mqtt.KinectClient;
import de.rwth.i5.kinectvision.mqtt.KinectHandler;
import de.rwth.i5.kinectvision.mqtt.SwevaClient;
import de.rwth.i5.kinectvision.robot.Robot;
import de.rwth.i5.kinectvision.robot.RobotClient;
import georegression.struct.point.Point3D_F32;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;
import org.springframework.boot.configurationprocessor.json.JSONException;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

//import edu.ufl.digitalworlds.j4k.DepthMap;

//import edu.ufl.digitalworlds.j4k.DepthMap;

/**
 * This class is for playing around with different tools
 */
public class Playground {

    public void testFiducialFinderFromFile() {
        File file = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("infrared_1.png"), "The provided file is null.").getFile());
        BufferedImage in = null;
        try {
            in = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedImage newImage = new BufferedImage(
                in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();
    }

    //    @Test
    public void x() {
        short a = (short) (0x8001);
        int unsigned = a >= 0 ? a : ((a & 0x7FFF) + 32768);
        System.out.println("If this is 1 we are on the right way: " + unsigned / 256);
    }

    //    @Test
    public void createDepthMapVisualization() {
        DepthModel depthModel = KinectDataStore.readDepthData("KinectData\\depth_1_1marker_1300mm.bin");
        BufferedImage buf = new BufferedImage(512, 424, ColorModel.OPAQUE);
        int rgb = 0;

        for (int j = 0; j < 424 * 512; j++) {
            rgb = 0;
            rgb = Color.HSBtoRGB(depthModel.getDepthFrame()[j] / 1000f, 1, 1);
            buf.setRGB(j % 512, (int) (j / 512), rgb);
        }
        ShowImages.showWindow(buf, "");
        while (true) {
        }
    }

    /**
     * This method generates a heatmap as a visualization for a depth frame
     */
//    @Test
    public void createDepthMapConversionVisualization() {
        DepthModel depthModel = KinectDataStore.readDepthData("KinectData\\ibims.bin");
//        DepthMap map = new DepthMap(512, 424, depthModel.getXYZ());

        BufferedImage buf = new BufferedImage(512, 424, ColorModel.OPAQUE);
        int rgb = 0;
        int x, y, z;
        try {
            FileWriter fileWriter = new FileWriter(new File("C:\\Users\\Justin\\Desktop\\yolo3000.txt"));


            for (int i = 0; i < 424; i++) {
                for (int j = 0; j < 512; j++) {
//                    fileWriter.write(map.realX[424 * i + j] + " " + map.realY[424 * i + j] + " " + map.realZ[424 * i + j] + "\n");
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ShowImages.showWindow(buf, "");
//        while (true) {
//        }

    }

    //    @Test
    public void showDepthMapHeatmap() {
        DepthModel depthModel = KinectDataStore.readDepthData("KinectData\\depth_1_1marker_1300mm.bin");
        BufferedImage buf = new BufferedImage(512, 424, ColorModel.OPAQUE);
        int rgb = 0;
        int x, y, z;
        Point3D_F32 conversion;

        for (int j = 0; j < 424 * 512; j++) {
            rgb = Color.HSBtoRGB(depthModel.getDepthFrame()[j] / 1000f, 1, 1);
            buf.setRGB(j % 512, j / 512, rgb);
        }
        //Display visualization
        ShowImages.showWindow(buf, "");
        while (true) {
        }
    }


    public void convertBinToBuf() {
        short[] data = KinectDataStore.readInfraredData("C:\\Users\\Justin\\Desktop\\Kinect Bilder\\infrared_1.bin");

        BufferedImage buf = VisualizeImageData.colorizeSign(FiducialFinder.toGrayF32Image(data, 512, 424), null, -1);
        ShowImages.showWindow(buf, "");
        while (true) {
        }

    }

    /**
     * This is a testing method for placing the robot in the point cloud. For this the point cloud and also the robot 3d model
     * are being exported as a xyz file. The marker positions are also included.
     */
//    @Test
    public void testRobotInPointCloud() {
        /*
        Load point cloud
         */


        DepthModel depthModel = KinectDataStore.readDepthData("KinectData\\ibims.bin");
//        DepthMap map = new DepthMap(512, 424, depthModel.getXYZ());


        ArrayList<Point3d> points = new ArrayList<>();
        for (int i = 0; i < 424; i++) {
            for (int j = 0; j < 512; j++) {
//                points.add(new Point3d(map.realX[424 * i + j], map.realY[424 * i + j], map.realZ[424 * i + j]));
            }
        }

        /*
        Create a robot
         */
        Robot robot = new Robot();
//        robot.generateSampleRobotModel();
        //Place the model inside the model

        /*
        Create visualization
         */
        try {
            FileWriter w = new FileWriter(new File("C:\\Users\\Justin\\Desktop\\testfile.txt"));
            PolygonMesh roboModel = robot.getCurrentRealWorldModel();
            for (Point3d point3d : points) {
                w.write(point3d.x + " " + point3d.y + " " + point3d.z + " 10 10 10\n");
            }

            for (Triangle triangle : roboModel) {
                w.write(triangle.a.x + " " + triangle.a.y + " " + triangle.a.z + " 255 0 0\n");
                w.write(triangle.b.x + " " + triangle.b.y + " " + triangle.b.z + " 255 0 0\n");
                w.write(triangle.c.x + " " + triangle.c.y + " " + triangle.c.z + " 255 0 0\n");
//                w.write(triangle.d.x + " " + triangle.d.y + " " + triangle.d.z + " 255 0 0\n");
            }


            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void extractXYZ() {
        DepthModel o = KinectDataStore.readDepthData("KinectData\\ibims.bin");
        float x, y, z;
        float xmin = 0, xmax = 0;
        float ymin = 0, ymax = 0;
        float zmin = 0, zmax = 0;

//        DepthMap ref = new DepthMap(512, 424, o.getXYZ());
        for (int j = 0; j < 424 * 512 * 3; j++) {
            //If the player index is between 0 and 5 there is a human
            //Add XYZ point then
            x = -o.getXYZ()[j];
            if (x != Float.NEGATIVE_INFINITY && x != Float.POSITIVE_INFINITY) {
                xmax = Math.max(x, xmax);
                xmin = Math.min(x, xmin);
            }
            j++;
            y = o.getXYZ()[j];
            if (y != Float.NEGATIVE_INFINITY && y != Float.POSITIVE_INFINITY) {
                ymax = Math.max(y, ymax);
                ymin = Math.min(y, ymin);
            }
            j++;
            z = o.getXYZ()[j];
            if (z != Float.NEGATIVE_INFINITY && z != Float.POSITIVE_INFINITY) {
                zmax = Math.max(z, zmax);
                zmin = Math.min(z, zmin);
            }
        }
        System.out.println(xmin);
        System.out.println(xmax);
        System.out.println(ymin);
        System.out.println(ymax);
        System.out.println(zmin);
        System.out.println(zmax);
    }

    public static double angle(Vector3d v1, Vector3d v2) {
        double angle = v1.angle(v2);
        if (v1.x * v2.y - v1.y * v2.x < 0)
            angle = -angle;
        return angle;
    }

    private double calculateS() {
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

    @Test
    public void visualizeEverything() {
        double S = calculateS();

        //Set up mqtt client for kinect
        KinectClient kinectClient = new KinectClient();
        kinectClient.setBroker("tcp://localhost:1883");
        kinectClient.setClientId("asdf");

        //Set the frame handler
        KinectHandler handler = new KinectHandler();
        kinectClient.setFrameHandler(handler);

        /*
        Initialize the RobotClient
         */
        Robot robot = new Robot();
        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\roboter_kugeln_scaled.x3d"));
        handler.setRobot(robot);
        /*
         * Initialize the evaluator
         */
        System.out.println("GUTEN TAG");


        RobotSimulationClient robotSimulationClient = new RobotSimulationClient();

        RobotClient robotClient = new RobotClient(robot, robotSimulationClient);
        robotSimulationClient.setRobotClient(robotClient);

        Evaluation evaluation = new Evaluation(robotClient, robot, S);
        handler.setEvaluation(evaluation);


        robotSimulationClient.startSimulation();
        try {
//        Connect to kinect
            kinectClient.initialize();
        } catch (MqttException e) {
            e.printStackTrace();
        }
//        ArrayList<Marker3d> markers = new ArrayList<>();
//        Marker3d marker3d = new Marker3d(1,1,1,1)
//        robot.setRealWorldBasePositions(new ArrayList<>());


        while (true) {

        }
//        KinectDataStore.readInfraredData();
//KinectDataStore.readDepthData();
    }

    //    @Test
    public void vectorTest() {
        Vector3d v1 = new Vector3d(1, 0, 0);
        Vector3d v2 = new Vector3d(-1, 0, 0);
        Vector3d v3 = new Vector3d(0, 1, 0);
        Vector3d v4 = new Vector3d(0, -1, 0);

        System.out.println(angle(v1, v3));
        System.out.println(angle(v1, v4));

    }

    @Test
    public void testRobotTransformation() {
        /*
        Initialize the RobotClient
         */
//        RobotClient robotClient = new RobotClient();

        Robot robot = new Robot();
//        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\roboter_kugeln_scaled.x3d"));
        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\robotermodell.x3d"));
//        SerialPortConnectorKRC2 connector = null;
//        try {
//            connector = new SerialPortConnectorKRC2(0);
//        } catch (SerialPortException e) {
//            e.printStackTrace();
//            System.err.println("No serial port found.");
//            return;
//        }


        RobotSimulationClient robotSimulationClient = new RobotSimulationClient();
        RobotClient robotClient = new RobotClient(robot, robotSimulationClient);
//        RobotClient robotClient = new RobotClient(robot, connector);
        robotSimulationClient.setRobotClient(robotClient);

        robotSimulationClient.startSimulation();

        /*
        connector.setRobotHandler(robotClient);
        try {
            connector.connect();
        } catch (SerialPortException e) {
            e.printStackTrace();
            System.err.println("No connection");
            return;
        }
        */
//        Visualizer visualizer = new Visualizer();

        SwevaClient swevaClient = new SwevaClient();
        swevaClient.setBroker("ws://broker.mqttdashboard.com:8000/mqtt");
        swevaClient.setClientId("blablabla");
        try {
            swevaClient.initialize();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        while (true) {
            ArrayList<BoundingSphere> spheres = robot.getRobotWithOrientation();
            try {
                swevaClient.publish(null, spheres, null, 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            visualizer.visualizeRobot(robot, spheres);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
