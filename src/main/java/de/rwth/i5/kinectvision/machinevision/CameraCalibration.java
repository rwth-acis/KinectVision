package de.rwth.i5.kinectvision.machinevision;

import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for calculation the position of the robot and calibrate the camera.
 */
@Slf4j
public class CameraCalibration {
    //How many infrared and depth frames are used for calibration?
    private static final int NUM_CALIBRATIONS = 10;
    //How many frames have been processed successfully?
    private int calibrationCounter = 0;
    @Getter
    //The result
    private ArrayList<Marker3d> markers = new ArrayList<>();
    private HashMap<Long, ArrayList<Marker3d>> calibrations = new HashMap<>();
    @Getter
    @Setter
    private Integer calibrationNumber = NUM_CALIBRATIONS;

    /**
     * Given the infrared data and a corresponding depth image calculates the markers' 3D-position.
     *
     * @param infraredData Infrared frame data
     * @param depthData    Depth frame data
     * @param buf
     * @return A list containing all found and 3D-matched markers.
     */
    public static ArrayList<Marker3d> generate3dMarkers(short[] infraredData, DepthModel depthData, BufferedImage buf) {
        //Detect markers in the infrared image
        ArrayList<FiducialDetectionResult> detectionResult = FiducialFinder.findFiducialsFromBytes(infraredData, buf);
//        log.debug("Found " + detectionResult.size() + " markers.");
        ArrayList<Marker3d> res = new ArrayList<>();
        int x_rw, y_rw;
        float x, y, z;
        //For every marker found generate a 3D marker
        for (FiducialDetectionResult fiducialDetectionResult : detectionResult) {
            x_rw = (int) Math.floor(fiducialDetectionResult.getCenter().x);
            y_rw = (int) Math.floor(fiducialDetectionResult.getCenter().y);
            int j = y_rw * 512 + 511 - x_rw;
            x = depthData.getXYZ()[j * 3];
            y = depthData.getXYZ()[j * 3 + 1];
            z = depthData.getXYZ()[j * 3 + 2];
            //The marker takes the depth from the depth image as Z-coordinate
            Marker3d marker3d = new Marker3d(x, y, z, fiducialDetectionResult.getId());
            res.add(marker3d);
        }
        return res;
    }

    /**
     * The calibration method. Calibration takes place multiple times.
     *
     * @param infraredData Current infrared frame
     * @param depthData    Current depth frame
     * @return True if calibration completed. False if not.
     */
    public boolean calibrate(short[] infraredData, DepthModel depthData) {
        //If already successfully calibrated
        if (calibrationCounter >= NUM_CALIBRATIONS) {
            log.debug("Already calibrated");
            return true;
        }
        //Check for markers
        ArrayList<Marker3d> calibrationMarkers = generate3dMarkers(infraredData, depthData, null);
        //Check if there are no duplicates in the arraylist
        if (hasDuplicates(calibrationMarkers)) {
            log.error("Marker list has duplicates");
            return false;
        }

        //Add every marker to the calibration result list
        for (Marker3d calibrationMarker : calibrationMarkers) {
            //If there is no arraylist yet for the marker we create one
            calibrations.computeIfAbsent(calibrationMarker.getId(), k -> new ArrayList<>());
            //Add the marker to its ID list
            calibrations.get(calibrationMarker.getId()).add(calibrationMarker);
        }

        //Add result to the calibration list
        calibrationCounter++;
        //If there are enough calibrations
        if (calibrationCounter == NUM_CALIBRATIONS) {
            //Create an average object for every marker
            for (Map.Entry<Long, ArrayList<Marker3d>> longArrayListEntry : calibrations.entrySet()) {
                //The average vector
                Vector3d averageCenter = new Vector3d();
                for (Marker3d marker3d : longArrayListEntry.getValue()) {
                    averageCenter.add(marker3d.getPosition());
                }
                //Divide
                averageCenter.scale(1.0 / NUM_CALIBRATIONS);
                //The average object
                Marker3d average = new Marker3d(longArrayListEntry.getKey(), averageCenter);
                //Add it to the result list
                markers.add(average);
            }
            //Calibrated
            return true;
        }
        //Not calibrated yet
        return false;
    }


    /**
     * Checks for duplicates in the ArrayList
     *
     * @param markers The list with markers
     * @return True if there are duplicates
     */
    private boolean hasDuplicates(ArrayList<Marker3d> markers) {
        for (int i = 0; i < markers.size(); i++) {
            Marker3d marker3d = markers.get(i);
            for (int j = i + 1; j < markers.size(); j++) {
                Marker3d d = markers.get(j);
                if (marker3d.getId() == d.getId()) {
                    return true;
                }
            }
        }
        return false;
    }
}
