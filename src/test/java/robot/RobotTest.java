package robot;

import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.machinevision.model.PolygonMesh;
import de.rwth.i5.kinectvision.machinevision.model.Triangle;
import de.rwth.i5.kinectvision.robot.Robot;
import de.rwth.i5.kinectvision.robot.RobotModel;
import de.rwth.i5.kinectvision.robot.RobotPart;
import org.junit.Test;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class for testing the Robot class
 */
public class RobotTest {
    /**
     * Creates Creates 3d points for visualization
     *
     * @param p1  The polygon to display
     * @param col Color red or blue
     */
    private static void createTestOutput(PolygonMesh p1, boolean col) {
        String color = col ? ";255;0;0" : ";0;0;255";
        for (Triangle triangle : p1) {
            System.out.println(triangle.a.x + ";" + triangle.a.y + ";" + triangle.a.z + color);
            System.out.println(triangle.b.x + ";" + triangle.b.y + ";" + triangle.b.z + color);
            System.out.println(triangle.c.x + ";" + triangle.c.y + ";" + triangle.c.z + color);
        }
    }

    /**
     * This tests checks if the transformation of the robot model according to marker positions does work.
     * As a reference a cube representing the robot has been translated in a 3D program to determine the
     * matrices. First translate (1,1,0), then rotate -45 degrees around M1, rotate about 45 degrees around
     * the normal vector of M1 and then scale ~1.4 to fit to M2. The third point M3 will not cause any rotation
     * in this test.
     */
    @Test
    public void testRealWorldTransformationTwoRotations() {
        //Initialize the robot
        Robot robot = new Robot();
//        robot.generateSampleRobotModel();
        /*
        Initialize markers
         */
//        robot.generateFromFiles("C:\\Users\\Justin\\Desktop\\");
        ArrayList<Marker3d> markers = new ArrayList<>();
        Marker3d marker1 = new Marker3d(1d, 1d, 0d, 1);
        markers.add(marker1);
        Marker3d marker2 = new Marker3d(3d, 3d, 2.8d, 2);
        markers.add(marker2);
        Marker3d marker3 = new Marker3d(1, 1, 1, 3);
        markers.add(marker3);
        //Tell the robot where the markers are
        robot.setRealWorldBasePositions(markers);
        /*
        Create reference objects
         */

        //reference 3d object is the robot before transformation
        PolygonMesh refObject = robot.getCombinedModel();
        System.out.println(refObject);
        //Reference m1
        Vector3d referenceMarker1 = new Vector3d(-1, -1, -1);
        //Reference m2
        Vector3d referenceMarker2 = new Vector3d(1, -1, -1);
        //Scale to fit to M2
        Matrix4d transformationMatrix = new Matrix4d();

        //This matrix has been calculated beforehand
        transformationMatrix.set(new double[]{0.99498743710662, -1.407124727947029, -0.9949874371066197, -0.4071247279470287,
                0.9949874371066199, 1.4071247279470287, -0.9949874371066202, 2.4071247279470285,
                1.407124727947029, 2.220446049250313E-16, 1.407124727947029, 2.814249455894058,
                0.0, 0.0, 0.0, 1.0});

        Triangle.transformVector(transformationMatrix, referenceMarker1);
        Triangle.transformVector(transformationMatrix, referenceMarker2);

        /*
        Marker 1 should not have been rotated
         */
        assertEquals(1, referenceMarker1.x, 0.1);
        assertEquals(1, referenceMarker1.y, 0.1);
        assertEquals(0, referenceMarker1.z, 0.1);

        /*
        Marker 2 should be on a certain location
         */
        assertEquals(3, referenceMarker2.x, 0.1);
        assertEquals(3, referenceMarker2.y, 0.1);
        assertEquals(2.8, referenceMarker2.z, 0.1);
        //Apply the reference transformation matrix to every triangle
        for (Triangle triangle : refObject) {
            triangle.applyTransformation(transformationMatrix);
        }

        /*
        Test
         */

        //Get the real world coordinates
        PolygonMesh testObj = robot.getCurrentRealWorldModel();
        int matches = 0;
        //Look for a matching triangle in the reference
        for (Triangle triangle : testObj) {
            for (Triangle refTriangle : refObject) {
                if (triangle.equalsEps(refTriangle, 0.40001)) {
                    matches++;
                    break;
                }
            }
        }

        //        PolygonMesh out1 = robot.getCombinedModel();

        //        createTestOutput(out1, false);
        //        createTestOutput(testObj, true);
        //Every triangle should have a match
        assertEquals(6, matches);
    }

    //    @Test
    public void testRotationMatrix360Degrees() {
        Matrix4d rotationMatrix2;
        Vector3d vec = new Vector3d(1, 2, 3);
        vec.normalize();
        //The rotation Matrix
        rotationMatrix2 = Robot.rotationMatrixArbitraryAxis(Math.toRadians(360), vec);
        //The resulting matrix should be (nearly) an identity matrix
        Matrix4d ref = new Matrix4d();
        ref.setIdentity();
        assertTrue(ref.epsilonEquals(rotationMatrix2, .000000001));
    }

    @Test
    public void testRotationMatrix90DegreesXAxis() {
        Matrix4d rotationMatrix2;
        Vector3d axisVector = new Vector3d(1, 0, 0);
        Vector3d vectorToRotate = new Vector3d(1, 1, 0);
        //The rotation Matrix
        rotationMatrix2 = Robot.rotationMatrixArbitraryAxis(Math.toRadians(90), axisVector);
        Triangle.transformVector(rotationMatrix2, vectorToRotate);
        assertTrue(new Vector3d(1, 0, 1).epsilonEquals(vectorToRotate, 0.1));
    }

    @Test
    public void testAnglePlanes90Degrees() {
        Vector3d x = new Vector3d(1, 0, 0); //x-axis
        Vector3d y = new Vector3d(0, 1, 0); //y-axis
        Vector3d z = new Vector3d(0, 0, 1); //z-axis
        assertEquals(Math.toRadians(90), Robot.getAnglePlanes(x, y, x, z), .000001);
    }

    @Test
    public void testAnglePlanes135Degrees() {
        Vector3d x = new Vector3d(1, 0, 0); //x-axis
        Vector3d y = new Vector3d(0, 1, 0); //y-axis
        Vector3d z = new Vector3d(0, -1, 1); //z-axis
        assertEquals(Math.toRadians(135), Robot.getAnglePlanes(x, y, x, z), .000001);
    }

    @Test
    public void testAnglePlanes180Degrees() {
        Vector3d x = new Vector3d(1, 0, 0); //x-axis
        Vector3d y = new Vector3d(0, 1, 0); //y-axis
        Vector3d z = new Vector3d(0, -1, 0); //z-axis
        assertEquals(Math.toRadians(180), Robot.getAnglePlanes(x, y, x, z), .000001);

    }

    /**
     * Loads the model of the robot
     */
    public static RobotModel generateSampleRobotModel() {
        //Creates a standard cube
        RobotModel robotModel = new RobotModel();
        robotModel.addBasePoint(new Marker3d(1, new Vector3d(-1f, -1f, -1f)));
        robotModel.addBasePoint(new Marker3d(2, new Vector3d(1f, -1f, -1f)));
        robotModel.addBasePoint(new Marker3d(3, new Vector3d(-1, -1, 1)));

        RobotPart base = new RobotPart();
        BoundingSphere boundingSphere = new BoundingSphere(new Vector3d(0, 0, 0), 1);
        base.setName("base");
        base.getBoundingSpheres().add(boundingSphere);

        RobotPart arm0 = new RobotPart();
        BoundingSphere boundingSphere1 = new BoundingSphere(new Vector3d(0, 0, 2), 1);
        arm0.setName("arm_0");
        arm0.getBoundingSpheres().add(boundingSphere1);

        robotModel.addAxis(0, true, new Vector3d(0, 0, 1), true);
        robotModel.addAxis(0, false, new Vector3d(1, 0, 1), true);

        RobotPart arm1 = new RobotPart();
        BoundingSphere boundingSphere2 = new BoundingSphere(new Vector3d(0, 0, 4), 1);
        arm1.setName("arm_1");
        arm1.getBoundingSpheres().add(boundingSphere2);

        robotModel.addAxis(1, true, new Vector3d(0, 0, 3), true);
        robotModel.addAxis(1, false, new Vector3d(0, 1, 3), true);


        robotModel.addRobotPart(base);
        robotModel.addRobotPart(arm0);
        robotModel.addRobotPart(arm1);
        return robotModel;
    }

    /**
     * This tests checks if the transformation of the robot model according to marker positions does work.
     * As a reference a cube representing the robot has been translated in a 3D program to determine the
     * matrices. Three points M1,M2,M3 are set and the cube should be rotated using every axis.
     */
//    @Test
    public void testRealWorldTransformationEveryAxis() {
        //Initialize the robot
        Robot robot = new Robot();
//        robot.generateSampleRobotModel();
//        robot.generateFromFiles("C:\\Users\\Justin\\Desktop\\");
        /*
        Initialize markers
         */

        ArrayList<Marker3d> markers = new ArrayList<>();
        Marker3d marker1 = new Marker3d(1d, 1d, 0d, 1);
        markers.add(marker1);
        Marker3d marker2 = new Marker3d(3d, 3d, 2.8d, 2);
        markers.add(marker2);
        Marker3d marker3 = new Marker3d(1, 1, 1, 3);
        markers.add(marker3);
        //Tell the robot where the markers are
        robot.setRealWorldBasePositions(markers);
        /*
        Create reference objects
         */

        //reference 3d object is the robot before transformation
        PolygonMesh refObject = robot.getCombinedModel();
        assertEquals(6, refObject.getFaces().size());
        System.out.println(refObject);
        //Reference m1
        Vector3d referenceMarker1 = new Vector3d(-1, -1, -1);
        //Reference m2
        Vector3d referenceMarker2 = new Vector3d(1, -1, -1);
        //Reference m3
//        Vector3d referenceMarker3 = new Vector3d(-1, -1, 1);

        Matrix4d transformationMatrix = new Matrix4d();

        //This matrix has been calculated beforehand
        transformationMatrix.set(new double[]{0.99498743710662, -1.407124727947029, -0.9949874371066197, -0.4071247279470287,
                0.9949874371066199, 1.4071247279470287, -0.9949874371066202, 2.4071247279470285,
                1.407124727947029, 2.220446049250313E-16, 1.407124727947029, 2.814249455894058,
                0.0, 0.0, 0.0, 1.0});

        Triangle.transformVector(transformationMatrix, referenceMarker1);
        Triangle.transformVector(transformationMatrix, referenceMarker2);

        /*
        Marker 1 should not have been rotated
         */
        assertEquals(1, referenceMarker1.x, 0.1);
        assertEquals(1, referenceMarker1.y, 0.1);
        assertEquals(0, referenceMarker1.z, 0.1);

        /*
        Marker 2 should be on a certain location
         */
        assertEquals(3, referenceMarker2.x, 0.1);
        assertEquals(3, referenceMarker2.y, 0.1);
        assertEquals(2.8, referenceMarker2.z, 0.1);


        /*
        Marker 3 should be on a certain location
         */
//        assertEquals(marker3.getPosition().x, referenceMarker3.x, 0.1);
//        assertEquals(marker3.getPosition().y, referenceMarker3.y, 0.1);
//        assertEquals(marker3.getPosition().z, referenceMarker3.z, 0.1);
        //Apply the reference transformation matrix to every triangle
        for (Triangle triangle : refObject) {
            triangle.applyTransformation(transformationMatrix);
        }

        /*
        Test
         */

        //Get the real world coordinates
        PolygonMesh testObj = robot.getCurrentRealWorldModel();
        assertEquals(6, testObj.getFaces().size());
        int matches = 0;
        //Look for a matching triangle in the reference
        for (Triangle triangle : testObj) {
            System.out.println(triangle);
            for (Triangle refTriangle : refObject) {
                if (triangle.equalsEps(refTriangle, 0.4001)) {
                    matches++;
                    break;
                }
            }
        }

        PolygonMesh out1 = robot.getCombinedModel();

        createTestOutput(out1, false);
        createTestOutput(testObj, true);
        //Every triangle should have a match
        assertEquals(6, matches);
    }

    @Test
    public void testGetRobotWithOrientationNoRotation() {
        Robot robot;
        robot = new Robot();
        robot.setRobotModel(generateSampleRobotModel());
        robot.setAngles(new double[]{0, 0, 0, 0, 0, 0});
        ArrayList<BoundingSphere> testObj = robot.getRobotWithOrientation();


        assertEquals(new BoundingSphere(new Vector3d(0, 0, 0), 1), testObj.get(0));
        assertEquals(new BoundingSphere(new Vector3d(0, 0, 2), 1), testObj.get(1));

    }

    @Test
    public void testGetRobotWithOrientation90Rotation() {
        Robot robot;
        robot = new Robot();
        robot.setRobotModel(generateSampleRobotModel());
        robot.setAngles(new double[]{90, 90, 0, 0, 0, 0});
        ArrayList<BoundingSphere> testObj = robot.getRobotWithOrientation();


        assertEquals(new BoundingSphere(new Vector3d(0, 0, 0), 1), testObj.get(0));
        assertEquals(new BoundingSphere(new Vector3d(0, -1, 1), 1), testObj.get(1));
        assertTrue(new BoundingSphere(new Vector3d(1, -2, 1), 1).epsilonEquals(testObj.get(2), 1E-6));
    }

    //    @Test
    public static void main(String args[]) {
//        RobotSimulationClient c = new RobotSimulationClient(new RobotClient(null,null));
//        c.startSimulation();
    }
}