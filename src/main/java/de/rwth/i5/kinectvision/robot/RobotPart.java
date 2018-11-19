package de.rwth.i5.kinectvision.robot;

import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.PolygonMesh;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Class representing a robot part
 */

public class RobotPart {
    /**
     * The bounding box of the arm
     */
    @Getter
    @Setter
    private PolygonMesh boundingBox;
    @Getter
    @Setter
    private String name;
    @Getter
    private ArrayList<BoundingSphere> boundingSpheres = new ArrayList<>();

    @Override
    public String toString() {
        return "RobotPart{" +
                "boundingBox=" + boundingBox +
                ", name='" + name + '\'' +
                '}';
    }
}
