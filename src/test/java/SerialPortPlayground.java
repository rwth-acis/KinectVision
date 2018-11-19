import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SerialPortPlayground {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final static int parity = 0;
    private final static int baud = 9600;
    private final static int dataBits = 8;
    private final static int stopBits = 1;
    //Standardwerte

    public static void main(String[] args) {
//        SerialPort.getCommPorts();
//        byte[] bytes = new byte[]{(byte) 0xAF, 0xB, 0x1};
//        System.out.println(DatatypeConverter.printHexBinary(bytes));
//        System.out.println(bytesToHex(bytes));
        printPortNames();
        SerialPort[] comPorts = SerialPort.getCommPorts();
        if (comPorts.length == 0) {
            System.err.println("No serial port found");
            return;
        }
        testConnection(comPorts[0], parity, baud, dataBits, stopBits);
        while (true) {
//            System.out.println("yo");
            //Busy waiting...
        }
    }

    static long startTime = 0;

    public static void printPortNames() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println(port.toString());
        }
    }

    public static void testConnection(SerialPort serialPort, int parity, int baud, int dataBits, int stopBits) {
        //The data callback
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
//                return SerialPort.LISTENING_EVENT_DATA_WRITTEN | SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                switch (event.getEventType()) {
                    case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                        System.out.println(System.currentTimeMillis() - startTime);
                        startTime = System.currentTimeMillis();
//                        byte[] buffer = new byte[serialPort.bytesAvailable()];
//                        int numRead = serialPort.readBytes(buffer, buffer.length);
////                        byte[] buffer = new byte[4];
////                        serialPort.readBytes(buffer, 4, 0);
//                        System.out.println("Read " + numRead + " bytes.");
//                        System.out.println("Data available");
//                        System.out.println("Received data: " + DatatypeConverter.printHexBinary(buffer));
                        break;
                    case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
                        System.out.println("Data Tx complete");
                        break;
                    case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
//                        System.out.println(Arrays.toString(event.getReceivedData()));
                        System.out.println(Arrays.toString(event.getReceivedData()));
//                        if (event.getReceivedData().length % 9 != 0)
//                            return;
                        System.out.println(System.currentTimeMillis() - startTime);
                        startTime = System.currentTimeMillis();
                        if (true) return;
//                        ByteBuffer wrapped = ByteBuffer.wrap(event.getReceivedData()); // big-endian by default
//                        wrapped.order(ByteOrder.LITTLE_ENDIAN);
                        for (int i = 0; i < event.getReceivedData().length; i += 9) {
                            byte[] axisNumberBytes = new byte[4];
                            axisNumberBytes[0] = event.getReceivedData()[i];
                            axisNumberBytes[1] = event.getReceivedData()[i + 1];
                            axisNumberBytes[2] = event.getReceivedData()[i + 2];
                            axisNumberBytes[3] = event.getReceivedData()[i + 3];
                            ByteBuffer wrappedNumber = ByteBuffer.wrap(axisNumberBytes); // big-endian by default
                            wrappedNumber.order(ByteOrder.LITTLE_ENDIAN);
                            int axisNumber = wrappedNumber.getInt();

                            byte[] axisValueBytes = new byte[4];
                            axisValueBytes[0] = event.getReceivedData()[i + 5];
                            axisValueBytes[1] = event.getReceivedData()[i + 6];
                            axisValueBytes[2] = event.getReceivedData()[i + 7];
                            axisValueBytes[3] = event.getReceivedData()[i + 8];
                            ByteBuffer wrappedValue = ByteBuffer.wrap(axisValueBytes); // big-endian by default
                            wrappedValue.order(ByteOrder.LITTLE_ENDIAN);
                            double axisValue = wrappedValue.getInt() / 100000.0;
//                            System.out.println("Axis " + axisNumber + ": " + axisValue + (axisNumber == 7 ? "mm" : "°"));
//                            if (axisNumber == 7) {
//                                System.out.println(System.currentTimeMillis() - startTime);
//                                startTime = System.currentTimeMillis();
//                            }
                        }

//                        System.out.println(number / 1000.0);
//                        System.out.println("Data Rx complete");
//                        System.out.println();
                        break;
                    default:
                        break;
                }

                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN)
                    System.out.println("All bytes were successfully transmitted!");
            }
        });
        serialPort.setParity(parity);
        serialPort.setBaudRate(baud);
        serialPort.setNumDataBits(dataBits);
        serialPort.setNumStopBits(stopBits);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
        serialPort.openPort();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
