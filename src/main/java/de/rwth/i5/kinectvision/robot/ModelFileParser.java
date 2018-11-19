package de.rwth.i5.kinectvision.robot;

import de.rwth.i5.kinectvision.machinevision.model.*;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for parsing a file containing the 3d bounding box model for the robot
 * <p>
 * VERY IMPORTANT NOTICE: CHOOSE Y FORWARD, Z UP IN BLENDER!!!
 */
@Slf4j
public class ModelFileParser {
    /**
     * This method parses the 3d file for the robot base containing the
     * base bounding box, two dummies for the rotation and multiple dummies for markers
     *
     * @param file The file to be parsed
     * @return A robot model containing the base bounding box, the first rotation axis and the markers
     */
    public static RobotModel parseFile(File file) throws IOException, SAXException, ParserConfigurationException {
        log.info("Parse base file");
        RobotModel res = new RobotModel();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("Group");

        //If there is no group element
        if (nList.getLength() == 0) {
            log.error("No element \"Group\" found.");
            return null;
        }

        //Handle every group item for the 3d models
        for (int i = 0; i < nList.getLength(); i++) {
//            RobotPart part = handleGroup(nList.item(i));
//            res.addRobotPart(part);
        }

        /*
         * Handle the dummys to get the axis
         */
        NodeList transformList = doc.getElementsByTagName("Transform");

        for (int i = 0; i < transformList.getLength(); i++) {
            handleAxisDummy(res, transformList.item(i));
            handleMarkerDummy(res, transformList.item(i));
            handleArmGroup(res, transformList.item(i));
        }

        return res;
    }

    /**
     * This method parses the given node and creates a RobotPart object in the robot if possible.
     *
     * @param robot The robot to append the part
     * @param node  The node containing the information
     */
    private static void handleArmGroup(RobotModel robot, Node node) {
        Node temp = node.getAttributes().getNamedItem("DEF");
        if (temp == null) return;
        String name = temp.getNodeValue();

        if (name.startsWith("base")) {
            name = "base";
        } else if (name.startsWith("arm_")) {
            name = name.substring(0, name.length() - 10);
        } else {
            return;
        }
        //Get arm name without TRANSFORM
        RobotPart robotPart = new RobotPart();

        robotPart.setName(name);
        //Get bounding spheres
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            BoundingSphere boundingSphere = parseBoundingTransform(children.item(i));
            if (boundingSphere != null) {
                robotPart.getBoundingSpheres().add(boundingSphere);
            }
        }
        //Add the part to the robot
        robot.addRobotPart(robotPart);
    }

    private static BoundingSphere parseBoundingTransform(Node node) {
        if (node == null || node.getAttributes() == null) return null;

        //Get translation String if available
        Node temp = node.getAttributes().getNamedItem("translation");
        if (temp == null) return null;
        String position = temp.getNodeValue();
        //Get scale String
        temp = node.getAttributes().getNamedItem("scale");
        if (temp == null) return null;
        String scale = temp.getNodeValue();

        //Create sphere
        Vector3d center = parseTranslationString(position);
        Vector3d radius = parseTranslationString(scale);
        if (center == null || radius == null) {
            log.debug("Translation or scale could not be parsed");
            return null;
        }
        return new BoundingSphere(center, radius.x);
    }

    /**
     * Adds a base position to the robot model if there is a marker dummy in the node
     *
     * @param res  The robot model to change
     * @param node The node containing the information
     */

    private static void handleMarkerDummy(RobotModel res, Node node) {
        log.info("Handle marker dummy");
        //Get the object's name
        String name = node.getAttributes().getNamedItem("DEF").getNodeValue();
        String translation = node.getAttributes().getNamedItem("translation").getNodeValue();

        if (!name.startsWith("marker_")) {
            return;
        }

        String[] splitted = name.split("_");
        if (splitted.length < 2) {
            return;
        }
        int markerID;
        try {
            markerID = Integer.parseInt(splitted[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        res.addBasePoint(new Marker3d(markerID, parseTranslationString(translation)));
        log.info("Base point created: " + res.getBasePoints().get(res.getBasePoints().size() - 1));
    }

    /**
     * Handles the transformation containing empties.
     *
     * @param robotModel The robot model to set the axis for
     * @param node       Node containing the transformation
     */
    private static void handleAxisDummy(RobotModel robotModel, Node node) {
        log.info("Handle axis dummy");
        //Get the object's name
        String name = node.getAttributes().getNamedItem("DEF").getNodeValue();
        Node element = node.getAttributes().getNamedItem("translation");
        if (element == null) {
            log.info("Element is null");
            return;
        }
        String translation = element.getNodeValue();
        if (!name.startsWith("axis_")) {
            log.info("Element not beginning with axis");
            return;
        }

        log.info("Found an axis");
        String[] splitted = name.split("_");
        Vector3d position = parseTranslationString(translation);
        int index = Integer.parseInt(splitted[1]);
        robotModel.addAxis(index, splitted[2].equals("start"), position, !splitted[3].equals("tr"));
        log.info("Axis generated." + position);
    }

    /**
     * Parses a string containing coordinates and returns a new Vector
     *
     * @param translation The string to parse
     * @return The vector containing the coordinates
     */
    private static Vector3d parseTranslationString(String translation) {
        List<Double> values = Arrays.stream(translation.split(" ")).map(Double::parseDouble).collect(Collectors.toList());
        if (values.size() < 3) {
            log.error("Translation is invalid. Size < 3");
            return null;
        }
        return new Vector3d(values.get(0), values.get(1), values.get(2));
    }

    /**
     * Method for parsing the groups containing the cubes
     *
     * @param node The node to be handled
     */
    private static RobotPart handleGroup(Node node) {
        log.debug("Handle group");

        //Get the cube name
        String name = node.getAttributes().getNamedItem("DEF").getNodeValue();

        Node shape = findItem("Shape", node.getChildNodes());
        if (shape == null) {
            log.error("No shape found in file.");
            return null;
        }

        Node indexSet = findItem("IndexedTriangleSet", shape.getChildNodes());
        if (indexSet == null) {
            log.error("No IndexedTriangleSet found in file.");
            return null;
        }

        Node coordinate = findItem("Coordinate", indexSet.getChildNodes());
        if (coordinate == null) {
            log.error("No coordinate found in file.");
            return null;
        }
        //Get the coordinate indices for the faces
        String coordIndex = indexSet.getAttributes().getNamedItem("index").getNodeValue();
        List<Integer> coordinateIndexes = Arrays.stream(coordIndex.split(" "))
                .map(Integer::parseInt).filter(integer -> integer != -1).collect(Collectors.toList());
        //Get the coordinates
        String coords = coordinate.getAttributes().getNamedItem("point").getNodeValue();
        List<Double> coordinates = Arrays.stream(coords.split(" "))
                .map(Double::parseDouble).collect(Collectors.toList());

        //Create vectors
        List<Vector3d> vector3ds = generateVectors(coordinates);
        //Create faces
//        ArrayList<Triangle> faces = generateFaces(vector3ds, coordinateIndexes);
        ArrayList<Triangle> faces = generateTriangles(vector3ds, coordinateIndexes);

        RobotPart arm = new RobotPart();
        PolygonMesh box = new PolygonMesh();
        box.setFaces(faces);
        arm.setBoundingBox(box);
        arm.setName(name);
        log.debug(arm.toString());
        return arm;
    }


    private static Node findItem(String name, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeName().equals(name)) {
                return nodeList.item(i);
            }
        }
        return null;
    }

    private static ArrayList<Triangle> generateTriangles(List<Vector3d> points, List<Integer> indices) {
        if (indices.size() % 3 != 0) {
            log.error("Size of list for face generation wrong");
            return null;
        }
        ArrayList<Triangle> res = new ArrayList<>();
        for (int i = 0; i < indices.size(); i += 4) {
            Triangle face = new Triangle(new Vector3d(points.get(indices.get(i))), new Vector3d(points.get(indices.get(i + 1))), new Vector3d(points.get(indices.get(i + 2))));
            res.add(face);
        }
        return res;
    }

    private static ArrayList<Face> generateFaces(List<Vector3d> points, List<Integer> indices) {
        if (indices.size() % 4 != 0) {
            log.error("Size of list for face generation wrong");
            return null;
        }
        ArrayList<Face> res = new ArrayList<>();
        for (int i = 0; i < indices.size(); i += 4) {
            Face face = new Face(new Vector3d(points.get(indices.get(i))), new Vector3d(points.get(indices.get(i + 1))), new Vector3d(points.get(indices.get(i + 2))), new Vector3d(points.get(indices.get(i + 3))));
            res.add(face);
        }
        return res;
    }

    private static ArrayList<Vector3d> generateVectors(List<Double> points) {
        if (points.size() % 3 != 0) {
            log.error("Size of point list is wrong");
            return null;
        }
        ArrayList<Vector3d> res = new ArrayList<>();
        for (int i = 0; i < points.size(); i += 3) {
            res.add(new Vector3d(points.get(i), points.get(i + 1), points.get(i + 2)));
        }
        return res;
    }

    private static void parseTransform(Node nodeList) {
        //TODO Parse emptys here

    }
}