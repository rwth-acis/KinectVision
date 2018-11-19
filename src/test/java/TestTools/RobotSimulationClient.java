package TestTools;

import de.rwth.i5.kinectvision.robot.RobotClient;
import de.rwth.i5.kinectvision.robot.serialconnection.RobotConnector;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * Generates fake serial data with positions
 */
public class RobotSimulationClient implements RobotConnector {
    @Getter
    private double[] angles = new double[]{0, 0, 0, 0, 0, 0, 0};
    @Setter
    private RobotClient robotClient;
    private int axis = 0;
    private int degree = 0;
    private boolean dir;
    int counter = 0;

    private boolean halt = false;

    public void startSimulation() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!halt)
                    changeAxisRandomly();
                //TODO: Which format?
                robotClient.onAxisData(Arrays.copyOf(angles, angles.length));
//                for (double angle : angles) {
//                    System.out.println(angle);
//                }
            }
        }).start();
    }

    public void changeAxisRandomly() {

        for (int i = 0; i < angles.length; i++) {
            angles[i] += (Math.random() * 5) - 1;
//            angles[i] = 0;
        }

//        angles[i]
//        angles[2]--;
//        if (angles[2] < -180) {
//            angles[2] = -180;
//        }
//angles[2] = -180;
//        angles[0]++;
//        if (true) return;

//        if (moveTo(axis, degree, dir)) {
//            axis = 0;
//            degree = 180;
//            dir = true;
//        }

//        if (moveTo(axis, degree, dir)) {
//            axis = (int) (Math.random() * 5);
//            degree = (int) (Math.random() * 80) + 60;
//            dir = new Random().nextBoolean();
//        }
//        if (angles[0] < 180) {
//            angles[0]++;
//            return;
//        }
//        if (angles[1] > -90) {
//            angles[1] -= 1;
//            return;
//        }
//        if (angles[2] > -90) {
//            angles[2] -= 1;
//            return;
//        }
//        if (angles[3] > -90) {
//            angles[3] -= 1;
//            return;
//        }
//        if (angles[4] > -180) {
//            angles[4] -= 2;
//            return;
//        }
//        angles = new double[]{0, 0, 0, 0, 0, 0};
    }

    private boolean moveTo(int axis, double degree, boolean dir) {
        if (angles[axis] == degree) {
            return true;
        } else {
            angles[axis] = (angles[axis] + (dir ? (-1) : 1)) % 360;
            if (angles[axis] < 0) {
                angles[axis] = 359;
            }
            return angles[axis] == degree;
        }
    }


    @Override
    public void stopRobot() {
        halt = true;
    }

    @Override
    public void continueRobot() {
        halt = false;
    }

    @Override
    public void sendData(Object o) {

    }
}
