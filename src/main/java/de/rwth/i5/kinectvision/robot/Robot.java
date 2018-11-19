package de.rwth.i5.kinectvision.robot;

import de.rwth.i5.kinectvision.machinevision.SVD;
import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.machinevision.model.PolygonMesh;
import de.rwth.i5.kinectvision.machinevision.model.Triangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.xml.sax.SAXException;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class loads the robot 3D model, handles all incoming robot events and moves the model accordingly.
 */
@Slf4j
public class Robot {
    private SVD svd = new SVD();
    private Object lock = new Object();
    @Getter
    @Setter
    private RobotModel robotModel;
    @Setter
    @Getter
    private double[] angles = new double[7];
    @Getter
    private boolean initialized = false;
    @Getter
    private ArrayList<Marker3d> bases = new ArrayList<>();
    public static final double[] defaultAngles = new double[]{0, 0, -90, 90, 0, 0, 0};

    /**
     * This method returns a model of the robot in its current orientation (the axis angles)
     *
     * @return The model of the robot
     */
    public ArrayList<BoundingSphere> getRobotWithOrientation() {
        //The resulting model
        ArrayList<BoundingSphere> res = new ArrayList<>();
        //Iterate over every part of the robot
        List<RobotPart> partList = robotModel.getRobotParts();

        //First add the base
        for (RobotPart robotPart : partList) {
            if (robotPart.getName().startsWith("base")) {
                for (BoundingSphere boundingSphere : robotPart.getBoundingSpheres()) {
                    res.add(new BoundingSphere(new Vector3d(boundingSphere.getCenter()), boundingSphere.getRadius()));
                }
                break;
            }
        }

        Matrix4d rotationMatrix = new Matrix4d();
        rotationMatrix.setIdentity();
        //Then transform every axis accordingly
        for (int i = 0; i < robotModel.getRobotParts().size() - 1; i++) {
            RobotPart part = robotModel.getRobotPartByNumber(i);
            if (part == null) {
                log.error("Robot part 'arm_" + i + "' has not been found. Cannot transform robot");
                return null;
            }

            //Achse finden
            Axis axis = robotModel.findAxis(i);

            //If axis is rotation axis
            if (axis.isRotation()) {
                //Startpunkt der Achse gibt Rotationspunkt an
                Vector3d rotOrigin = new Vector3d(axis.getStart());
                Triangle.transformVector(rotationMatrix, rotOrigin);
                //Rotationsvektor Achse start bis Achse ende
                Vector3d rotAxis = new Vector3d();
                Vector3d rotEnd = new Vector3d(axis.getEnd());
                Triangle.transformVector(rotationMatrix, rotEnd);
                rotAxis.sub(rotEnd, rotOrigin);
                //Winkel nach Achse
                double angle = Math.toRadians(angles[i] - defaultAngles[i]);
                //Rotationsmatrix_achse = Rotation um Achse
                Matrix4d rotationMatrixAxis = rotationMatrixArbitraryAxisFromPoint(angle, rotAxis, rotOrigin);
                //Rotation_gesamt = Rotationsmatrix_achse * Rotation_gesamt
                rotationMatrix.mul(rotationMatrixAxis, rotationMatrix);
                //Transformiere Part mit Rotation_gesamt
                res.addAll(BoundingSphere.transform(rotationMatrix, part.getBoundingSpheres()));
//                res.addAll(part.getBoundingSpheres());
            } else {
                //Startpunkt der Achse gibt Rotationspunkt an
                Vector3d translationOrigin = new Vector3d(axis.getStart());
                Triangle.transformVector(rotationMatrix, translationOrigin);
                //Rotationsvektor Achse start bis Achse ende
                Vector3d translationEnd = new Vector3d(axis.getEnd());
                Triangle.transformVector(rotationMatrix, translationEnd);

                Vector3d translationDirection = new Vector3d();
                translationDirection.sub(translationEnd, translationOrigin);
                translationDirection.normalize();
                double length = angles[i];
                translationDirection.scale(length);
                //Rotationsmatrix_achse = Rotation um Achse
                Vector3d translationDestination = new Vector3d(translationOrigin);
                translationDestination.add(translationDirection);
                Matrix4d translationMatrixAxis = generateTranslationMatrix(translationOrigin, translationDestination);
                //Rotation_gesamt = Rotationsmatrix_achse * Rotation_gesamt
                rotationMatrix.mul(translationMatrixAxis, rotationMatrix);
//                System.out.println(translationOrigin + ", " + translationDestination);
                //Transformiere Part mit Rotation_gesamt
                res.addAll(BoundingSphere.transform(rotationMatrix, part.getBoundingSpheres()));
//                res.addAll(part.getBoundingSpheres());
            }

        }
        return res;
    }

    /**
     * Creates the rotation matrix for a rotation about an arbitrary axis with an origin point
     *
     * @param angle     The angle in rad
     * @param rotAxis   Axis vector
     * @param rotOrigin The origin point
     * @return The rotation matrix
     */
    private Matrix4d rotationMatrixArbitraryAxisFromPoint(double angle, Vector3d rotAxis, Vector3d rotOrigin) {
        Matrix4d translationMatrix = generateTranslationMatrix(rotOrigin, new Vector3d());
        Matrix4d rotationMatrix = rotationMatrixArbitraryAxis(angle, rotAxis);
        Matrix4d translationMatrixInverse = generateTranslationMatrix(new Vector3d(), rotOrigin);
        Matrix4d res = new Matrix4d();
        res.setIdentity();
        res.mul(translationMatrixInverse);
        res.mul(rotationMatrix);
        res.mul(translationMatrix);
        return res;
    }

    /**
     * Creates the rotation matrix for a rotation about an arbitrary axis
     *
     * @param radianAngle The angle in rad
     * @param axis        Axis vector
     * @return The rotation matrix
     */
    public static Matrix4d rotationMatrixArbitraryAxis(double radianAngle, Vector3d axis) {
        Vector3d axisNormalized = new Vector3d(axis);
        axisNormalized.normalize();
        Matrix4d rotationMatrix2 = new Matrix4d();
//        double radianAngle = Math.toRadians(degreeAngle);
        double cosA = Math.cos(radianAngle);
        double sinA = Math.sin(radianAngle);
        double a, b, c;
        a = axisNormalized.x;
        b = axisNormalized.y;
        c = axisNormalized.z;
        double K = 1 - cosA;
        //The rotation Matrix
        rotationMatrix2.m00 = a * a * K + cosA;
        rotationMatrix2.m01 = a * b * K - c * sinA;
        rotationMatrix2.m02 = a * c * K + b * sinA;

        rotationMatrix2.m10 = a * b * K + c * sinA;
        rotationMatrix2.m11 = b * b * K + cosA;
        rotationMatrix2.m12 = b * c * K - a * sinA;

        rotationMatrix2.m20 = a * c * K - b * sinA;
        rotationMatrix2.m21 = b * c * K + a * sinA;
        rotationMatrix2.m22 = c * c * K + cosA;

        rotationMatrix2.m33 = 1;
        return rotationMatrix2;
    }

    /**
     * Set the positions
     *
     * @param marker3dList A list containing markers with real world coordinates
     */
    public void setRealWorldBasePositions(ArrayList<Marker3d> marker3dList) {
        this.bases = marker3dList;
    }

    /**
     * Returns the combined polygon model without transformations by markers
     *
     * @return The combined model
     */
    public PolygonMesh getCombinedModel() {
        PolygonMesh res = new PolygonMesh();
        /*
        Whole model generation respecting the current axis orientations
         */
        //Add all robot parts to the resulting model
        Matrix3d rotationMatrix;

        for (int i = 0; i < this.robotModel.getRobotParts().size(); i++) {

        }

        for (RobotPart rp : this.robotModel.getRobotParts()) {
            res.combine(rp.getBoundingBox());
        }
        return res;
    }

    public static double getAnglePlanes(Vector3d e1v1, Vector3d e1v2, Vector3d e2v1, Vector3d e2v2) {
        Vector3d normalE1 = new Vector3d();
        normalE1.cross(e1v1, e1v2);
        Vector3d normalE2 = new Vector3d();
        normalE2.cross(e2v1, e2v2);
        return normalE1.angle(normalE2);
    }

    /**
     * Generates the robot from files
     *
     * @param file The file
     */
    public void generateFromFiles(File file) {
        log.info("Generate from file");
        try {
            this.robotModel = ModelFileParser.parseFile(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method generates a real world 3d representation of the robot.
     * Here the loaded robot model, the current axis orientations and the base positions are respected.
     * Thus, the returned model is already transformed and aligned to match the Kinect's coordinate system.
     *
     * @return A real-world 3d representation of the robot with its current configuration.
     */
    public PolygonMesh getCurrentRealWorldModel() {
        /*
        TODO Whole model generation respecting the current axis orientations
         */
        PolygonMesh res = getCombinedModel();
//        PolygonMesh res = getRobotWithOrientation();
        //Add all robot parts to the resulting model
//        res.combine(robotModel.getArm());

        /*
        Transformation
         */
        //Get the base points
        if (bases.size() < 3) {
            log.error("Not enough (" + bases.size() + ") marker positions set for the robot. At least 3 needed!");
            return null;
        }
        /*
         * The first three recognized markers
         */
        Marker3d marker1, marker2, marker3;
        marker1 = bases.get(0);
        marker2 = bases.get(1);
        marker3 = bases.get(2);

        /*
         * The matching markers according to IDs
         */
        Marker3d base1 = null, base2 = null, base3 = null;
        for (Marker3d marker3d : robotModel.getBasePoints()) {
            if (marker3d.getId() == marker1.getId()) {
                base1 = marker3d;
            } else if (marker3d.getId() == marker2.getId()) {
                base2 = marker3d;
            } else if (marker3d.getId() == marker3.getId()) {
                base3 = marker3d;
            }
        }
        if (base1 == null || base2 == null || base3 == null) {
            log.error("No matching base found");
            return null;
        }

        ArrayList<RealVector> modelMarkers = new ArrayList<>();
        ArrayList<RealVector> realMarkers = new ArrayList<>();


        modelMarkers.add(vectorToRealVector(base1.getPosition()));
        modelMarkers.add(vectorToRealVector(base2.getPosition()));
        modelMarkers.add(vectorToRealVector(base3.getPosition()));

        realMarkers.add(vectorToRealVector(marker1.getPosition()));
        realMarkers.add(vectorToRealVector(marker2.getPosition()));
        realMarkers.add(vectorToRealVector(marker3.getPosition()));
        svd.calculateRotationTranslation(modelMarkers, realMarkers);

        Matrix4d transformationMatrix = generateTransformationMatrix(svd.getRotationMatrix(), svd.getTranslation());

        for (Triangle re : res) {
            re.applyTransformation(transformationMatrix);
        }
        //Transform the markers
        res.setMarker1(new Vector3d(base1.getPosition().x, base1.getPosition().y, base1.getPosition().z));
        res.setMarker2(new Vector3d(base2.getPosition().x, base2.getPosition().y, base2.getPosition().z));
        res.setMarker3(new Vector3d(base3.getPosition().x, base3.getPosition().y, base3.getPosition().z));
        Triangle.transformVector(transformationMatrix, res.getMarker1());
        Triangle.transformVector(transformationMatrix, res.getMarker2());
        Triangle.transformVector(transformationMatrix, res.getMarker3());
        return res;
    }

    /**
     * This method generates a real world 3d representation of the robot.
     * Here the loaded robot model, the current axis orientations and the base positions are respected.
     * Thus, the returned model is already transformed and aligned to match the Kinect's coordinate system.
     */
    public ArrayList<BoundingSphere> transformRobot() {
        //Get BSpheres from robot parts
        ArrayList<BoundingSphere> boundingSpheres;
/*
        ArrayList<BoundingSphere> boundingSpheres = new ArrayList<>();
        for (RobotPart robotPart : robotModel.getRobotParts()) {
            if (robotPart != null && robotPart.getBoundingSpheres() != null)
                for (BoundingSphere boundingSphere : robotPart.getBoundingSpheres()) {
                    if (boundingSphere != null && boundingSphere.getCenter() != null) {
                        //Clone the spheres
                        boundingSpheres.add(new BoundingSphere(new Vector3d(boundingSphere.getCenter()), boundingSphere.getRadius()));
                    }
                }
        }
        */
        boundingSpheres = getRobotWithOrientation();
        /*
        TODO Whole model generation respecting the current axis orientations
         */
        /*
        Transformation
         */
        //Get the base points
        if (bases.size() < 3) {
            log.error("Not enough (" + bases.size() + ") marker positions set for the robot. At least 3 needed!");
            return null;
        }
        /*
         * The first three recognized markers
         */
        Marker3d marker1, marker2, marker3;
        marker1 = bases.get(0);
        marker2 = bases.get(1);
        marker3 = bases.get(2);

        /*
         * The matching markers according to IDs
         */
        Marker3d base1 = null, base2 = null, base3 = null;
        for (Marker3d marker3d : robotModel.getBasePoints()) {
            if (marker3d.getId() == marker1.getId()) {
                base1 = marker3d;
            } else if (marker3d.getId() == marker2.getId()) {
                base2 = marker3d;
            } else if (marker3d.getId() == marker3.getId()) {
                base3 = marker3d;
            }
        }
        if (base1 == null || base2 == null || base3 == null) {
            log.error("No matching base found");
            return null;
        }

        ArrayList<RealVector> modelMarkers = new ArrayList<>();
        ArrayList<RealVector> realMarkers = new ArrayList<>();


        modelMarkers.add(vectorToRealVector(base1.getPosition()));
        modelMarkers.add(vectorToRealVector(base2.getPosition()));
        modelMarkers.add(vectorToRealVector(base3.getPosition()));

        realMarkers.add(vectorToRealVector(marker1.getPosition()));
        realMarkers.add(vectorToRealVector(marker2.getPosition()));
        realMarkers.add(vectorToRealVector(marker3.getPosition()));
        svd.calculateRotationTranslation(modelMarkers, realMarkers);

        Matrix4d transformationMatrix = generateTransformationMatrix(svd.getRotationMatrix(), svd.getTranslation());


        for (BoundingSphere boundingSphere : boundingSpheres) {
            Triangle.transformVector(transformationMatrix, boundingSphere.getCenter());
        }
        // Do this
//        synchronized (lock) {
//            currentBoundingSpheres = boundingSpheres;
//        }
        return boundingSpheres;
    }

    /**
     * Generates a Matrix4d transformation matrix with the given 3x3 rotation matrix and translation vector
     *
     * @param rotation    The rotation matrix
     * @param translation The translation vector
     * @return The merged 4x4 transformation matrix
     */
    private Matrix4d generateTransformationMatrix(RealMatrix rotation, RealVector translation) {
        Matrix4d res = new Matrix4d();
        //Translation
        res.setTranslation(new Vector3d(translation.getEntry(0), translation.getEntry(1), translation.getEntry(2)));
        //Rotation
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                res.setElement(i, j, rotation.getEntry(i, j));
            }
        }
        res.setElement(3, 3, 1);
        return res;
    }

    /**
     * Converts a Vector3d to a RealVector
     *
     * @param vector3d The vector to be converted
     * @return The conversion result
     */
    private RealVector vectorToRealVector(Vector3d vector3d) {
        return MatrixUtils.createRealVector(new double[]{vector3d.x, vector3d.y, vector3d.z});
    }

    private Matrix4d generateTranslationMatrix(Vector3d from, Vector3d to) {
        Matrix4d translationMatrix = new Matrix4d();
        translationMatrix.setIdentity();
        translationMatrix.m03 = to.x - from.x;
        translationMatrix.m13 = to.y - from.y;
        translationMatrix.m23 = to.z - from.z;
        return translationMatrix;
    }
}