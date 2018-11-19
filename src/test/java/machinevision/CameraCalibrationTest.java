package machinevision;

import TestTools.KinectDataStore;
import de.rwth.i5.kinectvision.machinevision.CameraCalibration;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CameraCalibrationTest {


    /**
     * Testing method for the camera calibration. Opens an infrared and corresponding depth frame and checks
     * if the marker count is right and if the position is right.
     */
    @Test
    public void testCalibrationOneFile() {
        //Read infrared and depth file
        short[] infraredData = KinectDataStore.readInfraredData("KinectData\\infrared_1_1marker_1300mm.bin");
        DepthModel depthData = KinectDataStore.readDepthData("KinectData\\depth_1_1marker_1300mm.bin");

        //Generate 3d markers
        ArrayList<Marker3d> resultList = CameraCalibration.generate3dMarkers(infraredData, depthData, null);

        //Test output
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        Marker3d marker3d = resultList.get(0);
        assertEquals(marker3d.getPosition().x, 243);
        assertEquals(marker3d.getPosition().y, 91);
        assertEquals(marker3d.getPosition().z, 1279);
    }
}
