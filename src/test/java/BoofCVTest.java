import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import de.rwth.i5.kinectvision.machinevision.FiducialDetectionResult;
import de.rwth.i5.kinectvision.machinevision.FiducialFinder;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Class for testing BoofCV related stuff. It is intended to be a playground.
 */
public class BoofCVTest {
    @Test
    public void boofcvInitTest() {
        // load the lens distortion parameters and the input image
        BufferedImage input = null;
        try {
            input = UtilImageIO.loadImage(getClass().getResource("fidu_643.png").toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GrayF32 grayF32 = new GrayF32(input.getWidth(), input.getHeight());
        ConvertBufferedImage.convertFrom(input, grayF32);
        ArrayList<FiducialDetectionResult> testObj = FiducialFinder.findFiducials(grayF32, null);
        System.out.println(testObj.size());
        System.out.println(testObj.get(0).getId());

    }
}