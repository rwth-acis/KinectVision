package machinevision;

import TestTools.KinectDataStore;
import de.rwth.i5.kinectvision.machinevision.MachineVision;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import edu.ufl.digitalworlds.j4k.DepthMap;
import org.junit.Test;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class MachineVisionTest {
    /**
     * Tests if the output is correct
     */
    @Test
    public void detectHumansTest() {
        DepthModel o = KinectDataStore.readDepthData("KinectData\\ibims.bin");
        float x, y, z;
        //Reference depth map for XYZ check
        DepthMap ref = new DepthMap(512, 424, o.getXYZ());
        ArrayList<Vector3d> testObj = MachineVision.detectHumans(o);
        int i = 0;
        for (Vector3d vector3d : testObj) {
//            System.out.println(testObj);
            assertEquals(ref.realX[i++], vector3d.x, 0.001);
        }

    }
}
