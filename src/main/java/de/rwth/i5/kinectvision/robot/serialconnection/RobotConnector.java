package de.rwth.i5.kinectvision.robot.serialconnection;

public interface RobotConnector {
    void stopRobot();

    void continueRobot();

    void sendData(Object o);
}
