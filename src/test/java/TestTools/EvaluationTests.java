package TestTools;

import de.rwth.i5.kinectvision.machinevision.CameraCalibration;
import de.rwth.i5.kinectvision.machinevision.MachineVision;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class EvaluationTests {
    CameraCalibration cameraCalibration = new CameraCalibration();

    public void testCompleteCalibration(short[] infrared, DepthModel depthModel) {
        if (cameraCalibration.calibrate(infrared, depthModel)) {
            ArrayList<Marker3d> markers = cameraCalibration.getMarkers();
            for (Marker3d marker3d : markers) {
                showResult(marker3d);
            }
        }
    }

    /**
     * Test the angle
     *
     * @param infrared
     * @param depthModel
     * @param buf
     */
    public ArrayList<Marker3d> testMarkerRecognition(short[] infrared, DepthModel depthModel, BufferedImage buf) {
        ArrayList<Marker3d> marker3ds = CameraCalibration.generate3dMarkers(infrared, depthModel, buf);
//        System.out.println("FOUND: " + marker3ds.size());
        Marker3d m643 = null, m284 = null;
        for (Marker3d marker3d : marker3ds) {
            if (marker3d.getId() == 643)
                m643 = marker3d;
            if (marker3d.getId() == 284)
                m284 = marker3d;

//            showResult(marker3d);
        }

        ArrayList<Vector3d> humans = MachineVision.detectHumans(depthModel);
        Graphics graphics = buf.getGraphics();
        graphics.setColor(Color.WHITE);
        if (depthModel != null)
            for (int i = 0; i < 512; i++) {
                for (int j = 0; j < 424; j++) {
                    if (depthModel.getPlayerIndex()[512 * j + i] > 0) {
                        graphics.fillRect(512 - i + 1, j - 1, 2, 2);
                    }
                }
            }

        if (m643 != null) {
            double min = Double.POSITIVE_INFINITY;

            if (humans != null)
                for (Vector3d human : humans) {
                    Vector3d len = new Vector3d(m643.getPosition());
                    len.sub(human);
                    min = Math.min(min, len.length());
                }

//            System.out.println(min);


//            Vector3d len = new Vector3d(m284.getPosition());
//            len.sub(m643.getPosition());
//            System.out.println(len.length());
        }
        return marker3ds;
    }

    private void showResult(Marker3d marker) {
        System.out.println("Marker '" + marker.getId() + "': ");
        System.out.println(marker.toString());
        System.out.println("Length: " + marker.getPosition().length());
        System.out.println();
    }
}
