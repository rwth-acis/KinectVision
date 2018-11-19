package TestTools;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import de.rwth.i5.kinectvision.Counter;
import de.rwth.i5.kinectvision.analysis.Evaluation;
import de.rwth.i5.kinectvision.machinevision.FrameHandler;
import de.rwth.i5.kinectvision.machinevision.MachineVision;
import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.mqtt.KinectClient;
import de.rwth.i5.kinectvision.robot.Robot;
import de.rwth.i5.kinectvision.robot.RobotClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.nustaq.serialization.FSTConfiguration;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * This is a tool for capturing depth and infrared data to store them in a file. To save simply click on the video
 * panel.
 */
public class Evaluator {
    private static final String path = "C:\\Users\\Justin\\Desktop\\Kinect Bilder\\";
    static boolean test = false;
    static ArrayList<Long> times = new ArrayList<>();
    static int x = 0, y = 0;
    private static KinectClient kinectClient;
    private static ImagePanel p;
    private static short[] infra;
    private static DepthModel depth;
    private static BufferedImage buf;
    private static ArrayList<Marker3d> markers = null;
    long start = 0;


    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    static {
        conf.registerClass(DepthModel.class);
    }

    public static void main(String args[]) {
//        vektoren();
//        picShow();
//        timeMeasure();
//        timeMeasureRobot();
//        timeMeasureEvaluation();
        testSerialization();
    }

    public static void testSerialization() {
        DepthModel depthModel = new DepthModel(new short[424 * 512], new byte[424 * 512], new float[424 * 512 * 3], null, 0, 0);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 100; i++) {
            Counter.start();
//            conf.asByteArray(depthModel);

            try (ObjectOutputStream oos = new ObjectOutputStream (baos)) {
                oos.writeObject (depthModel);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Counter.stop();
        }
        Counter.print();
    }

    public static void timeMeasureEvaluation() {
        Robot robot = new Robot();
        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\robotermodell.x3d"));
        ArrayList<Marker3d> bases = new ArrayList<>();
        bases.add(new Marker3d(1, 2, 3, 643));
        bases.add(new Marker3d(11, 22, 31, 131));
        bases.add(new Marker3d(10, 24, 5, 284));
        robot.setRealWorldBasePositions(bases);
        Evaluation evaluation = new Evaluation(null, robot, 0);
        ArrayList<Vector3d> humanPoints = new ArrayList<>();
        for (int i = 0; i < 424 * 512; i++) {
            humanPoints.add(new Vector3d(1, 2, 3));
        }
        for (int i = 0; i < 100; i++) {
            Counter.start();
            evaluation.evaluate(humanPoints);
            Counter.stop();
        }
        Counter.print();
    }

    public static void vektoren() {
        ArrayList<BoundingSphere> spheres = new ArrayList<>();
//        spheres.add(new BoundingSphere(new Vector3d(8.295717308804381, 2.5904315685426624, 2.6745256504647674), 0.452156));
//        spheres.add(new BoundingSphere(new Vector3d(-1.3745791585907394, 2.1504892818933676, 3.309932214267435), 0.676366));
//        spheres.add(new BoundingSphere(new Vector3d(-1.031219351095518, 1.5435199657593746, 2.8569244746367417), 0.355363));
//        spheres.add(new BoundingSphere(new Vector3d(-1.350409645940565, 1.7843420744050125, 2.922635985912939), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.335062981344941, 1.6093996206810601, 2.8757991342165186), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.6021891633671101, 1.7649875923377767, 3.0099225996653645), 0.344009));
//        spheres.add(new BoundingSphere(new Vector3d(-1.8199775601754773, 1.7374873881076223, 3.1398002075311586), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.0711169760013686, 1.7284773959007609, 3.2166635300969006), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.2114502482705367, 1.6956962554341786, 3.376471466345191), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.4339074362807578, 1.691468111768545, 3.3885350447416784), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-1.6792754391024305, 1.7029621392310172, 3.320179200821825), 0.300625));
//        spheres.add(new BoundingSphere(new Vector3d(-0.7055278921595267, 0.7998368874862916, 2.0276568298885893), 0.299195));
//        spheres.add(new BoundingSphere(new Vector3d(-0.8697559955517971, 1.082922940844833, 2.22603228799282), 0.324596));
//        spheres.add(new BoundingSphere(new Vector3d(-1.0221486197255212, 1.3377434749067572, 2.4086125757098698), 0.314788));
//        spheres.add(new BoundingSphere(new Vector3d(-1.2118890638572646, 1.583387000098801, 2.622288098634035), 0.327414));
//        spheres.add(new BoundingSphere(new Vector3d(-0.07966187623725318, 0.5984516754457925, 1.7688662582827681), 0.293971));
//        spheres.add(new BoundingSphere(new Vector3d(-0.4199820947197823, 0.7755570217775603, 2.139978895399463), 0.38521));
//        spheres.add(new BoundingSphere(new Vector3d(-0.698801640757031, 0.8581520755873431, 2.400917837987077), 0.320032));
//        spheres.add(new BoundingSphere(new Vector3d(0.23958532099865337, 0.4313695316565749, 1.4005532546844077), 0.305194));
        spheres.add(new BoundingSphere(new Vector3d(0.33075848869653424, 0.3739260048535442, 1.317804929472136), 0.226684));

        Vector3d point = new Vector3d(0.3369300365447998, 0.2704678177833557, 1.4580000638961792);
//        Vector3d point = new Vector3d(-0.24777235090732574, -0.31898483633995056, 1.659000039100647);
        System.out.println(calculateMinDistance(point, spheres));
    }

    private static double calculateMinDistance(Vector3d humanPoint, ArrayList<BoundingSphere> currentSpheres) {
        double min = Double.POSITIVE_INFINITY;
        for (BoundingSphere currentSphere : currentSpheres) {
            double distance = calculateDist(humanPoint, currentSphere);
            min = Math.min(distance, min);
        }
        return min;
    }

    private static double calculateDist(Vector3d point, BoundingSphere boundingSphere) {
        Vector3d distVec = new Vector3d();
        distVec.sub(point, boundingSphere.getCenter());

        return distVec.length() - boundingSphere.getRadius();
    }

    public static void picShow() {
        kinectClient = new KinectClient();
        kinectClient.setBroker("tcp://localhost:1883");
        kinectClient.setClientId("Sample Client");
        buf = new BufferedImage(512, 424, BufferedImage.TYPE_USHORT_GRAY);
        initKinect();
    }

    private static void initKinect() {
        //Generates a window for showing the output
        p = ShowImages.showWindow(buf, "");
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                click();
//                saveDataClicked();
//                x = e.getX();
//                y = e.getY();
            }
        });
        EvaluationTests evaluationTest = new EvaluationTests();
        kinectClient.setFrameHandler(new FrameHandler() {
            @Override
            public void onDepthFrame(DepthModel o) {
//                times.add(System.nanoTime());
                depth = o;
            }

            @Override
            public void OnInfraredFrame(short[] data) {

                //Infrared frame: Visualize and find markers
                infra = data;

//                if (depth == null) {
//                    return;
//                }
                markers = evaluationTest.testMarkerRecognition(infra, depth, buf);

//                    test = false;
//                }
                // For visualisation
//                BufferedImage buf = new BufferedImage(512, 424, BufferedImage.TYPE_USHORT_GRAY);
                int idx = 0;
                int iv = 0;
                short sv = 0;
                byte bv = 0;
                int abgr;
//                for (int i = 0; i < 512 * 424; i++) {
//                    sv = data[i];
//                    iv = sv >= 0 ? sv : 0x10000 + sv;
//                    bv = (byte) ((iv & 0xfff8) >> 6);
//                    abgr = bv + (bv << 8) + (bv << 16);
//                    buf.setRGB(i % 512, (i / 512), abgr);
//                }


//                Raster raster = Raster.createPackedRaster(DataBuffer.TYPE_USHORT, 512, 424, 1, 8, null);
//                ((WritableRaster) raster).setDataElements(0, 0, 512, 424, data);
//                buf = new BufferedImage(512, 424, BufferedImage.TYPE_USHORT_GRAY);
////                buf.setData(raster);
//
                for (int i = 0; i < 512 * 424; i++) {
                    sv = data[i];
                    iv = sv >= 0 ? sv : 0x10000 + sv;
                    bv = (byte) ((iv & 0xfff8) >> 6);
                    abgr = bv + (bv << 8) + (bv << 16);
//                    buf.setRGB(i % 512, (i / 512), data[i]);
                }
//                System.out.println(data[x + 512 * y]);
                Graphics2D g = buf.createGraphics();
                g.setStroke(new BasicStroke(20));
                g.setColor(Color.GREEN);


                p.setBufferedImage(buf);

            }

            @Override
            public void onColorFrame(byte[] payload) {
                //Unused
            }
        });

        //Start the kinect
        try {
            kinectClient.initialize();
            System.out.println("Connected to mqtt broker and wait for data.");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    static private void click() {
        System.out.println("---------------------------------------------------");
        if (markers.isEmpty()) {
            System.out.println("Not recognized");
        }
        for (Marker3d marker : markers) {
            System.out.println(marker);
            System.out.println(marker.getPosition().length());
        }
        System.out.println("---------------------------------------------------");
        System.out.println();

    }

    private static void timeMeasureRobot() {
        Robot robot = new Robot();
        robot.generateFromFiles(new File("C:\\Users\\Justin\\Desktop\\robotermodell.x3d"));
        ArrayList<Marker3d> bases = new ArrayList<>();
        bases.add(new Marker3d(1, 2, 3, 643));
        bases.add(new Marker3d(11, 22, 31, 131));
        bases.add(new Marker3d(10, 24, 5, 284));
        robot.setRealWorldBasePositions(bases);
        for (int i = 0; i < 100; i++) {
            Counter.start();
            robot.transformRobot();
            Counter.stop();
        }
        Counter.print();

    }

    private static void timeMeasure() {
        DepthModel depthModel = new DepthModel(null, new byte[424 * 512], new float[424 * 512 * 3], null, 0, 0);
        Counter.start();
        MachineVision.detectHumans(depthModel);
        Counter.stop();


    }

    private static void saveDataClicked() {
//        if (infra.length > 0) {
//            KinectDataStore.saveInfraredData("C:\\Users\\Justin\\Desktop\\Kinect Bilder\\Marker " + System.currentTimeMillis() + "ms_infra.bin", infra);
//            infra = new short[0];
//        }
//        if (depth != null) {
//            KinectDataStore.saveDepthData("C:\\Users\\Justin\\Desktop\\Kinect Bilder\\Marker " + System.currentTimeMillis() + "ms_depth.bin", depth);
//            depth = null;
//        }
        test = !test;
    }
}
