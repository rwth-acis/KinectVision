package de.rwth.i5.kinectvision.machinevision;

import de.rwth.i5.kinectvision.machinevision.model.DepthModel;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

public class MachineVision {
    /**
     * Method for masking humans in the point cloud
     *
     * @param o Point cloud data
     * @return Vectors containing human points
     */
    public static ArrayList<Vector3d> detectHumans(DepthModel o) {
        if (o == null) return null;
        ArrayList<Vector3d> res = new ArrayList<>();
        float x, y, z;
        for (int j = 0; j < 424 * 512; j++) {
            //If the player index is between 0 and 5 there is a human
            if (o.getPlayerIndex()[j] >= 0 && o.getPlayerIndex()[j] <= 5) {
                //Add XYZ point then
                x = o.getXYZ()[j * 3];
                y = o.getXYZ()[j * 3 + 1];
                z = o.getXYZ()[j * 3 + 2];
                res.add(new Vector3d(x, y, z));
            }
        }

//        System.out.println(res.size());
        return res;
    }
}