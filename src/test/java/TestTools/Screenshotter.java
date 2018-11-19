package TestTools;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import de.rwth.i5.kinectvision.machinevision.FiducialDetectionResult;
import de.rwth.i5.kinectvision.machinevision.FiducialFinder;
import de.rwth.i5.kinectvision.machinevision.FrameHandler;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.mqtt.KinectClient;
import georegression.struct.point.Point2D_F64;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;

/**
 * This is a tool for capturing depth and infrared data to store them in a file. To save simply click on the video
 * panel.
 */
public class Screenshotter {
    private static final String path = "C:\\Users\\Justin\\Desktop\\Kinect Bilder\\";
    private static KinectClient kinectClient;
    private static ImagePanel p;
    private static short[] infra;
    private static DepthModel depth;
    private static BufferedImage buf;

    public static void main(String args[]) {
        kinectClient = new KinectClient();
        kinectClient.setBroker("tcp://localhost:1883");
        kinectClient.setClientId("Sample Client");
        buf = new BufferedImage(512, 424, BufferedImage.TYPE_4BYTE_ABGR);
        initKinect();
    }

    private static void initKinect() {
        //Generates a window for showing the output
        p = ShowImages.showWindow(buf, "");
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                saveDataClicked();
            }
        });

        kinectClient.setFrameHandler(new FrameHandler() {
            @Override
            public void onDepthFrame(DepthModel o) {
                //DepthFrame: Visualize
                depth = o;

                BufferedImage buf = new BufferedImage(512, 424, ColorModel.OPAQUE);
                Graphics2D g = buf.createGraphics();
                g.setStroke(new BasicStroke(10));
                g.setColor(Color.RED);
                int rgb = 0;
                for (int j = 0; j < 424 * 512; j++) {
//                    System.out.println(o.getXYZ()[j]);
                    rgb = 0;
                    rgb = Color.HSBtoRGB(o.getDepthFrame()[j] / 1000f, 1, 1);
                    buf.setRGB((j % 512), (int) (j / 512), rgb);
                }
                /*
                 * Set if the vis should be shown.
                 */
                //p.setBufferedImage(buf);

            }

            @Override
            public void OnInfraredFrame(short[] data) {
                //Infrared frame: Visualize and find markers
                infra = data;
//                GrayS16 img = FiducialFinder.toGrayS16Image(data, 512, 424);
                // For visualisation
                BufferedImage buf = new BufferedImage(512, 424, BufferedImage.TYPE_INT_BGR);
                int idx = 0;
                int iv = 0;
                short sv = 0;
                byte bv = 0;
                int abgr;
                for (int i = 0; i < 512 * 424; i++) {
                    sv = data[i];
                    iv = sv >= 0 ? sv : 0x10000 + sv;
                    bv = (byte) ((iv & 0xfff8) >> 6);
                    abgr = bv + (bv << 8) + (bv << 16);
                    buf.setRGB(i % 512, (i / 512), abgr);
                }
                // How many levels in wavelet transform
                int numLevels = 1;
                // Create the noise removal algorithm
                WaveletDenoiseFilter<GrayF32> denoiser =
                        FactoryImageDenoise.waveletBayes(GrayF32.class, numLevels, 0, 255);

                GrayF32 gray = new GrayF32(buf.getWidth(), buf.getHeight());
                GrayF32 denoised = new GrayF32(buf.getWidth(), buf.getHeight());
                ConvertBufferedImage.convertFrom(buf, gray);
                // remove noise from the image
                denoiser.process(gray, denoised);


//                ArrayList<FiducialDetectionResult> det = FiducialFinder.findFiducialsFromBytes(data);
//                GrayF32 gray = new GrayF32(buf.getWidth(), buf.getHeight());
                ConvertBufferedImage.convertFrom(buf, gray);
                ArrayList<FiducialDetectionResult> det = FiducialFinder.findFiducials(denoised, null);


                ConvertBufferedImage.convertTo(denoised, buf);
                Graphics2D g = buf.createGraphics();
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.GREEN);
                Point2D_F64 bound1;
                Point2D_F64 bound2;
                for (FiducialDetectionResult fiducialDetectionResult : det) {
                    for (int i = 0; i < fiducialDetectionResult.getBounds().size() - 1; i++) {
                        bound1 = fiducialDetectionResult.getBounds().get(i);
                        bound2 = fiducialDetectionResult.getBounds().get(i + 1);
                        //Draw line
                        g.drawLine(((int) bound1.x), (int) bound1.y, (int) bound2.x, (int) bound2.y);
                    }
                    if (fiducialDetectionResult.getBounds().size() > 1) {
                        bound1 = fiducialDetectionResult.getBounds().get(fiducialDetectionResult.getBounds().size() - 1);
                        bound2 = fiducialDetectionResult.getBounds().get(0);
                        g.drawLine(((int) bound1.x), (int) bound1.y, (int) bound2.x, (int) bound2.y);
                    }
                    //Draw center
                    g.fillRect(((int) (fiducialDetectionResult.getCenter().x - 10)), ((int) fiducialDetectionResult.getCenter().y - 10), 20, 20);
                }
                /*
                Set if vis should be shown
                 */

//                panel.
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

    private static void saveDataClicked() {
        if (infra.length > 0) {
            KinectDataStore.saveInfraredData("C:\\Users\\Justin\\Desktop\\Kinect Bilder\\Marker " + System.currentTimeMillis() + "ms_infra.bin", infra);
            infra = new short[0];
        }
        if (depth != null) {
            KinectDataStore.saveDepthData("C:\\Users\\Justin\\Desktop\\Kinect Bilder\\Marker " + System.currentTimeMillis() + "ms_depth.bin", depth);
            depth = null;
        }
    }
}
