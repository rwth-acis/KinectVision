package de.rwth.i5.kinectvision.visualization;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import de.rwth.i5.kinectvision.machinevision.model.BoundingSphere;
import de.rwth.i5.kinectvision.machinevision.model.Marker3d;
import de.rwth.i5.kinectvision.machinevision.model.PolygonMesh;
import de.rwth.i5.kinectvision.machinevision.model.Triangle;
import de.rwth.i5.kinectvision.robot.Robot;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Visualizer {
    BufferedImage buf = new BufferedImage(1280, 960, ColorModel.OPAQUE);
    ImagePanel panel = ShowImages.showWindow(buf, "Testvisualisierung");
    Graphics2D g = buf.createGraphics();
    PolygonMesh polygonMesh;
    BasicStroke text = new BasicStroke(10);
    BasicStroke normal = new BasicStroke(1);
    int markerSize = 10;
    int scale = 100;
    int tx = 600;
    double maxX = 0;

    public Visualizer() {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(1));
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    scale += 10;
//                    tx += (300 - e.getX()) / 5;
//                } else if (e.getButton() == MouseEvent.BUTTON3) {
//                    scale -= 10;
//                    tx += (300 - e.getX()) / 5;
//                }
                File outputfile = new File("C:\\Users\\Justin\\Desktop\\SCREENIES\\image" + maxX++ + ".png");
                try {
                    ImageIO.write(buf, "png", outputfile);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private double maxZ = 0;
    int sc = 40;

    public void visualizeHumans(ArrayList<Vector3d> humans, PolygonMesh polygonMesh, Robot robot, ArrayList<BoundingSphere> spheres, Vector3d nearestRob, Vector3d nearestHum) {
        g.setStroke(normal);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
        //Show human points from above
        g.setColor(Color.RED);
        if (humans != null)
            for (Vector3d human : humans) {
                if (Math.abs(human.x) != Double.POSITIVE_INFINITY && Math.abs(human.y) != Double.POSITIVE_INFINITY && Math.abs(human.z) != Double.POSITIVE_INFINITY) {
//                buf.setRGB(((int) ((human.x + 10) * 40)), ((int) ((human.z + 10) * 40)), Color.RED.getRGB());
                    g.drawLine(convertValue(human.x), convertValue(human.z), convertValue(human.x), convertValue(human.z));
                }
            }

        g.setColor(Color.YELLOW);
//        ArrayList<BoundingSphere> spheres = robot.transformRobot();

        if (spheres != null)
            for (BoundingSphere boundingSphere : spheres) {
                g.fillOval(convertValue(boundingSphere.getCenter().x - boundingSphere.getRadius()), convertValue(boundingSphere.getCenter().z - boundingSphere.getRadius()), ((int) (boundingSphere.getRadius() * scale * 2)), ((int) (boundingSphere.getRadius() * scale * 2)));
            }

        if (nearestHum != null && nearestRob != null) {
            g.setColor(Color.WHITE);
            g.setStroke(text);
            g.drawLine(convertValue(nearestHum.x), convertValue(nearestHum.z), convertValue(nearestRob.x), convertValue(nearestRob.z));
            g.setStroke(normal);

        }
        //Show robot
        g.setColor(Color.YELLOW);
//        polygonMesh = robot.getCurrentRealWorldModel();
//        if (polygonMesh != null) {
        if (false) {
            for (Triangle face : robot.getCurrentRealWorldModel()) {
                g.drawLine(convertValue(face.a.x), convertValue(face.a.z), convertValue(face.b.x), convertValue(face.b.z));
                g.drawLine(convertValue(face.b.x), convertValue(face.b.z), convertValue(face.c.x), convertValue(face.c.z));
                g.drawLine(convertValue(face.c.x), convertValue(face.c.z), convertValue(face.a.x), convertValue(face.a.z));
//                g.drawLine(convertValue(face.a.x), convertValue(face.a.z), convertValue(face.d.x), convertValue(face.d.z));
            }
        }

        //Show marker positions
        g.setColor(Color.BLUE);
        if (robot != null)
            for (Marker3d marker3d : robot.getBases()) {
                if (Math.abs(marker3d.getPosition().x) != Double.POSITIVE_INFINITY && Math.abs(marker3d.getPosition().y) != Double.POSITIVE_INFINITY && Math.abs(marker3d.getPosition().z) != Double.POSITIVE_INFINITY) {
                    g.fillRect(convertValue(marker3d.getPosition().x) - markerSize / 2, convertValue(marker3d.getPosition().z) - markerSize / 2, markerSize, markerSize);
//                g.drawImage(cross, convertValue(marker3d.getPosition().x) - 2, convertValue(marker3d.getPosition().z) - 2, null);
                }
            }
        g.setColor(Color.GREEN);
        if (polygonMesh != null) {
            if (polygonMesh.getMarker1() != null) {
                g.fillRect(convertValue(polygonMesh.getMarker1().x) - 2, convertValue(polygonMesh.getMarker1().z) - 2, 14, 14);
            }
            g.setColor(Color.MAGENTA);
            if (polygonMesh.getMarker2() != null) {
                g.fillRect(convertValue(polygonMesh.getMarker2().x) - 2, convertValue(polygonMesh.getMarker2().z) - 2, 14, 14);
            }
            g.setColor(Color.WHITE);
            if (polygonMesh.getMarker3() != null) {
                g.fillRect(convertValue(polygonMesh.getMarker3().x) - 2, convertValue(polygonMesh.getMarker3().z) - 2, 14, 14);
            }
        }
        g.setColor(Color.CYAN);
        g.fillRect(convertValue(0) - 5, convertValue(0) - 5, 10, 10);
        g.drawChars("KINECT".toCharArray(), 0, 6, convertValue(0) - 20, convertValue(0) - 20);
        panel.setBufferedImage(buf);
    }

    public void visualizeRobot(Robot robot, ArrayList<BoundingSphere> spheres) {
        g.setStroke(normal);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, buf.getWidth(), buf.getHeight());

        g.setColor(Color.YELLOW);
        if (spheres != null)
            for (BoundingSphere boundingSphere : spheres) {
                g.fillOval(convertValue1(boundingSphere.getCenter().x - boundingSphere.getRadius()), convertValue1(boundingSphere.getCenter().y - boundingSphere.getRadius()), ((int) (boundingSphere.getRadius() * sc * 2)), ((int) (boundingSphere.getRadius() * sc * 2)));
            }
        panel.setBufferedImage(buf);
    }

    private int convertValue1(double value) {
//        return (int) (900 - value * sc);
        return (int) (value * sc) + 40;
    }

    private int convertValue(double value) {
        return (int) ((value + 10) * scale) - tx;
    }

}
