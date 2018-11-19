package robot;

import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.robot.ModelFileParser;
import de.rwth.i5.kinectvision.robot.RobotModel;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.vecmath.Vector3d;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModelFileParserTest {


    //    @Test
    public void testParseFile() {
        RobotModel robotModel = null;
        try {
            robotModel = ModelFileParser.parseFile(new File("C:\\Users\\Justin\\Desktop\\robo.x3d"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        assertNotNull(robotModel);
        assertNotNull(robotModel.getBasePoints());
        assertNotNull(robotModel.getRobotParts());
        assertNotNull(robotModel.getAxes());

//        assertEquals(new Vector3d(0, 1, 0), robotModel.getAxes()[0].getStart());
        assertEquals(3, robotModel.getRobotParts().size());
    }

    //    @Test
    public void testBaseCreation() throws ParserConfigurationException, SAXException, IOException {
        RobotModel robotModel = ModelFileParser.parseFile(new File(("C:\\Users\\Justin\\Desktop\\base.x3d")));
        assertNotNull(robotModel);
        assertNotNull(robotModel.getRobotParts());
        assertEquals(1, robotModel.getRobotParts().size());

        assertEquals(3, robotModel.getBasePoints().size());

        assertEquals(new Marker3d(0, new Vector3d(-1, -1, -1)), robotModel.getBasePoints().get(2));
        assertEquals(new Marker3d(1, new Vector3d(1, -1, -1)), robotModel.getBasePoints().get(1));
        assertEquals(new Marker3d(2, new Vector3d(-1, -1, 1)), robotModel.getBasePoints().get(0));
    }

    @Test
    public void testRobotCreation() {
        RobotModel robotModel = null;
        try {
            robotModel = ModelFileParser.parseFile(new File("C:\\Users\\Justin\\Desktop\\roboter_kugeln.x3d"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        assertNotNull(robotModel);
        assertNotNull(robotModel.getBasePoints());
        assertNotNull(robotModel.getRobotParts());
        assertNotNull(robotModel.getAxes());

        assertEquals(5, robotModel.getAxes().size());
        assertEquals(6, robotModel.getRobotParts().size());
        assertEquals(3, robotModel.getBasePoints().size());
    }

}
