package de.rwth.i5.kinectvision.robot;

import de.rwth.i5.kinectvision.robot.serialconnection.RobotConnector;
import de.rwth.i5.kinectvision.robot.serialconnection.RobotHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for handling the communication from and to the robot
 */
public class RobotClient implements RobotHandler {
    @Getter
    @Setter
    private Robot robot;
    private RobotConnector connector;

    public RobotClient(Robot robot, RobotConnector connector) {
        this.robot = robot;
        this.connector = connector;
    }

    @Override
    public void onAxisData(double[] angles) {
        angles[0] = angles[0] / 1000;
        robot.setAngles(angles);
    }

    @Override
    public void setAxis(int i, double axisValue) {
        robot.getAngles()[i] = axisValue;
    }

    public void stopRobot() {
        connector.stopRobot();
    }
}
