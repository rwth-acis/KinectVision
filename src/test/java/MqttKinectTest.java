import de.rwth.i5.kinectvision.machinevision.FrameHandler;
import de.rwth.i5.kinectvision.machinevision.model.DepthModel;
import de.rwth.i5.kinectvision.mqtt.KinectClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:spring.xml")
@EnableConfigurationProperties
public class MqttKinectTest {

    static KinectClient kinectClient;


    public static void main(String args[]) {
        kinectClient = new KinectClient();
        kinectClient.setBroker("tcp://localhost:1883");
        kinectClient.setClientId("asdf");

        kinectClient.setFrameHandler(new FrameHandler() {
            @Override
            public void onDepthFrame(DepthModel o) {

            }

            @Override
            public void OnInfraredFrame(short[] data) {

            }

            @Override
            public void onColorFrame(byte[] payload) {

            }
        });
    }

}
