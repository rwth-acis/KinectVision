package de.rwth.i5.kinectvision.mqtt;

import de.rwth.i5.kinectvision.analysis.Evaluation;
import de.rwth.i5.kinectvision.machinevision.CameraCalibration;
import de.rwth.i5.kinectvision.machinevision.FrameHandler;
import de.rwth.i5.kinectvision.machinevision.MachineVision;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.robot.Robot;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

@Slf4j
public class KinectHandler implements FrameHandler {
    private DepthModel lastDepth;
    private boolean calibrated = false;
    @Setter
    private Evaluation evaluation;
    @Setter
    Robot robot;
    private CameraCalibration cameraCalibration = new CameraCalibration();

    /**
     * Kinect depth frame handler
     *
     * @param o The depth frame object
     */
    @Override
    public void onDepthFrame(DepthModel o) {
        //Update the last received depth frame
        lastDepth = o;
        //Stop here if no calibration has been made yet TODO:
        if (!calibrated) {
            return;
        }
        //Given the point cloud detect the humans in there
        ArrayList<Vector3d> humanPoints = MachineVision.detectHumans(o);
        // Evaluator handles accordingly to determine if an action is needed.
        if (evaluation != null) {
            evaluation.evaluate(humanPoints);
        }
    }

    /**
     * An infrared frame will be used to determine the camera resp. the robot position
     *
     * @param data The infrared frame
     */
    @Override
    public void OnInfraredFrame(short[] data) {
        //No need for calibration frames yet if the calibration has taken place
        if (calibrated) {
            return;
        }

        // If there is no depth frame, skip
        if (lastDepth == null) return;

        //If the calibration is not done yet
        if (!cameraCalibration.calibrate(data, lastDepth)) {
            return;
        }

        //Else we check the result
        ArrayList<Marker3d> calibrationResult = cameraCalibration.getMarkers();
        //Not enough markers found
        if (calibrationResult.size() < 3) {
            log.error("Not enough markers found for calibration. Retry.");
            //recreate the calibration object
            cameraCalibration = new CameraCalibration();
        } else {
            log.info("Calibration successful.");
            robot.setRealWorldBasePositions(calibrationResult);
            calibrated = true;
        }
    }

    @Override
    public void onColorFrame(byte[] payload) {
        //No color :/
    }
}
