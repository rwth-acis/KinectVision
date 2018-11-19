package de.rwth.i5.kinectvision.robot;

import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * This class is a representation of the robot's current position in the three-dimensional space of the Kinect.
 * For every movable part which receives its own positioning information by the robot there is a single object.
 */
@Getter
@Setter
@Slf4j
public class RobotModel {
    /**
     * Array containing the axis
     */
    private ArrayList<Axis> axes = new ArrayList<>();
    /**
     * The bounding boxes represent the bounds of the arms.
     **/
    private ArrayList<RobotPart> robotParts = new ArrayList<>();
    /**
     * This 3d point defines where the first base point is in the model (relative to the center of the model file).
     */
    private ArrayList<Marker3d> basePoints = new ArrayList<>();

    /**
     * Adds a new base point
     *
     * @param namedBasePoint Marker containing id and position
     */
    public void addBasePoint(Marker3d namedBasePoint) {
        basePoints.add(namedBasePoint);
    }

    /**
     * Adds a new robot part
     *
     * @param part The part to add
     */
    public void addRobotPart(RobotPart part) {
        robotParts.add(part);
    }

    /**
     * Adds a new position to the axes array
     *
     * @param index      The index of the axis
     * @param start      True if it is a start vector, false if end
     * @param position   The position to be set
     * @param isRotation true if the axis is a rotation axis
     */
    public void addAxis(int index, boolean start, Vector3d position, boolean isRotation) {
        Axis axis = findAxis(index);

        if (axis == null) {
            axis = new Axis();
            axes.add(axis);
            axis.setAxisNumber(index);
        }

        if (start) {
            axis.setStart(position);
        } else {
            axis.setEnd(position);
        }
        axis.setRotation(isRotation);
    }

    /**
     * Finds an axis given its number
     *
     * @param axisNumber The axis number
     * @return The axis if found, else null
     */
    public Axis findAxis(int axisNumber) {
        for (Axis axe : axes) {
            if (axe.getAxisNumber() == axisNumber) {
                return axe;
            }
        }
        return null;
    }


    /**
     * Finds a robot part with the name "arm_" + number
     *
     * @param number The number of the arm (nearest to base = 0)
     * @return The part if found, else null
     */
    public RobotPart getRobotPartByNumber(int number) {
        for (RobotPart robotPart : robotParts) {
            if (robotPart.getName().equals("arm_" + number)) {
                return robotPart;
            }
        }
        return null;
    }
}
