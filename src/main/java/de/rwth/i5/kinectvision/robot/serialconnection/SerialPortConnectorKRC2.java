package de.rwth.i5.kinectvision.robot.serialconnection;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Class for handling the connection to a KUKA KR C2 controller using the RS232 serial port.
 */
@Slf4j
public class SerialPortConnectorKRC2 implements RobotConnector {
    @Setter
    @Getter
    private RobotHandler robotHandler;
    private SerialPort serialPort;

    private final static int PARITY = 0;
    private final static int BAUD = 9600;
    private final static int DATA_BITS = 8;
    private final static int STOP_BITS = 1;
    private final static int FLOW_CONTROL = SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED; //Used by default
    private double[] axisBuffer = new double[]{0, 0, 0, 0, 0, 0, 0};

    /**
     * @param portNumber
     * @throws SerialPortException
     */
    public SerialPortConnectorKRC2(int portNumber) throws SerialPortException {
        SerialPort[] comPorts = SerialPort.getCommPorts();
        if (comPorts.length == 0) {
            log.error("No serial port found");
            throw new SerialPortException("No serial port found");
        } else if (portNumber >= comPorts.length) {
            log.error("Port number out of range.");
            throw new SerialPortException("Port number out of range.");
        }
        serialPort = comPorts[portNumber];
        System.out.println(serialPort.toString());
    }

    /**
     * Converts 4 bytes in the array using Little Endian byte order.
     *
     * @param bytes  The whole byte array
     * @param offset The starting index
     * @return The little endian integer conversion
     */
    private static int convertNumber(byte[] bytes, int offset) {
        byte[] axisValueBytes = new byte[4];
        axisValueBytes[0] = bytes[offset];
        axisValueBytes[1] = bytes[offset + 1];
        axisValueBytes[2] = bytes[offset + 2];
        axisValueBytes[3] = bytes[offset + 3];

        ByteBuffer wrappedNumber = ByteBuffer.wrap(axisValueBytes);
        wrappedNumber.order(ByteOrder.LITTLE_ENDIAN); //Because robot sends LE
        return wrappedNumber.getInt();
    }

    public static void printPortNames() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println(port.toString());
        }
    }

    @Override
    public void stopRobot() {
//        serialPort.writeBytes(ByteBu, 4);
    }

    @Override
    public void continueRobot() {

    }

    /**
     * Connect with the controller using default parameters.
     *
     * @throws SerialPortException If connection does not work.
     */
    public void connect() throws SerialPortException {
        connect(BAUD, PARITY, DATA_BITS, STOP_BITS, FLOW_CONTROL);
    }

    /**
     * Sends data to the robot
     *
     * @param o The data which has to sent.
     */
    @Override
    public void sendData(Object o) {

    }

    /**
     * Connect with the controller
     */
    public void connect(int baud, int parity, int dataBits, int stopBits, int flowControl) throws SerialPortException {
        if (robotHandler == null) {
            throw new SerialPortException("No robot handler set. Has to be set before.");
        }

        //No port found?
        if (serialPort == null) {
            throw new SerialPortException("No serial port with name found.");
        }

        //The data callback
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                //Only react if a whole packet has been sent.
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            /**
             * The callback functions
             *
             * @param event
             */
            @Override
            public void serialEvent(SerialPortEvent event) {
                switch (event.getEventType()) {

                    case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                        // Only used for debugging.
                        byte[] buffer = new byte[serialPort.bytesAvailable()];
                        int numRead = serialPort.readBytes(buffer, buffer.length);
                        System.out.println("Read " + numRead + " bytes.");
                        System.out.println("Data available");
                        System.out.println("Received data: " + DatatypeConverter.printHexBinary(buffer));
                        break;

                    case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
                        //We do not send any data (yet)
                        System.out.println("Data Tx complete");
                        break;
                    case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                        /*
                        -----------------------------------------------------------
                        Already contains the data which has been read in the event
                        -----------------------------------------------------------
                         */

                        if (event.getReceivedData().length % 9 != 0)
                            //Happens at the beginning.
                            return;

                        //Iterate because there might be more than one value
                        for (int i = 0; i < event.getReceivedData().length; i += 9) {
                            int axisNumber = convertNumber(event.getReceivedData(), i);
                            double axisValue = convertNumber(event.getReceivedData(), i + 5) / 100000.0;

//                            log.debug("Axis " + axisNumber + ": " + axisValue + (axisNumber == 1 ? "mm" : "°"));
//                            System.out.println("Axis " + axisNumber + ": " + axisValue + (axisNumber == 7 ? "mm" : "°"));
                            if (axisNumber > 7 || axisNumber < 0) {
                                log.warn("Wrong axis number " + axisNumber);
                                return;
                            }
                            axisBuffer[axisNumber - 1] = axisValue;
                            if (axisNumber == 7) {
                                //After all 7 values received, set the axis value to the robot
                                robotHandler.onAxisData(Arrays.copyOf(axisBuffer, 7));
                            }

                        }
                        break;
                    default:
                        break;
                }
            }
        });

        //Setup the serial port
        serialPort.setParity(parity);
        serialPort.setBaudRate(baud);
        serialPort.setNumDataBits(dataBits);
        serialPort.setNumStopBits(stopBits);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
        serialPort.openPort();
    }
}
