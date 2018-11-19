package de.rwth.i5.kinectvision.machinevision;

import boofcv.abst.fiducial.FiducialDetector;
import boofcv.alg.filter.basic.GrayImageOps;
import boofcv.core.image.ConvertImage;
import boofcv.factory.fiducial.ConfigFiducialBinary;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.factory.filter.binary.ConfigThreshold;
import boofcv.factory.filter.binary.ThresholdType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU16;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.Polygon2D_F64;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * Class for finding fiducials in images using BoofCV
 */
@Slf4j
public class FiducialFinder {
    public static ArrayList<FiducialDetectionResult> findFiducialsFromBytes2(short[] data) {
        return findFiducials(toGrayF32Image(data, 512, 424), null);
    }

    public static ArrayList<FiducialDetectionResult> findFiducialsFromBytes(short[] data, BufferedImage buf) {
        GrayF32 f32 = new GrayF32(512, 424);
        GrayU16 u16 = toGrayU16Image(data, 512, 424);
        GrayU8 u8 = new GrayU8(512, 424);

//        GThresholdImageOps.threshold(u16, u8, 1200, true);
        // Select a global threshold using Otsu's method.
//        double threshold = GThresholdImageOps.computeOtsu(f32, 0, 255);
        ConvertImage.convert(u16, f32);
        GrayF32 bright = new GrayF32(f32.width, f32.height);
//        GrayImageOps.brighten(f32, 20000, Integer.MAX_VALUE, bright);
        GrayImageOps.stretch(f32, 10, 2000, Float.POSITIVE_INFINITY, bright);
//        ConvertBufferedImage.convertTo(u16, buf);
//        ConvertBufferedImage.convertTo(u16, buf);
//        bright.setData(f32.data);
        ConvertImage.convert(bright, u16);
        ConvertBufferedImage.convertTo(u16, buf);
        return findFiducials(bright, buf);
    }

    public static ArrayList<FiducialDetectionResult> findFiducials(GrayF32 original, BufferedImage bufferedImage) {
        ArrayList<FiducialDetectionResult> detectionList = new ArrayList<>();

        // Detect the fiducials
        FiducialDetector<GrayF32> detector = FactoryFiducial.squareBinary(
                new ConfigFiducialBinary(0.1), ConfigThreshold.local(ThresholdType.LOCAL_MEAN, 25), GrayF32.class);
//                new ConfigFiducialBinary(0.1), ConfigThresholdLocalOtsu.fixed(0.5),GrayF32.class);
        detector.detect(original);

//        log.info(detector.totalFound() + " fiducials found.");
        //Iterate over all fiducials found


//        System.out.println(detector.totalFound() + " FOUND");
        for (int i = 0; i < detector.totalFound(); i++) {
            Polygon2D_F64 bounds = new Polygon2D_F64();
            Point2D_F64 locationPixel = new Point2D_F64();
            detector.getCenter(i, locationPixel);
            detector.getBounds(i, bounds);
            FiducialDetectionResult fiducialDetectionResult = new FiducialDetectionResult(locationPixel, bounds, detector.getId(i));
//            System.out.println("ID:" + detector.getId(i));
            detectionList.add(fiducialDetectionResult);
            if (bufferedImage != null) {
                Graphics graphics = bufferedImage.getGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(((int) fiducialDetectionResult.getCenter().x) - 10, ((int) fiducialDetectionResult.getCenter().y) - 10, 20, 20);
//                bufferedImage.setRGB(((int) fiducialDetectionResult.getCenter().x), ((int) fiducialDetectionResult.getCenter().y), Color.RED.getRGB());
            }
//            if (detector.hasUniqueID())
//                System.out.println("Target ID = " + detector.getId(i));
//            if (detector.hasMessage())
//                System.out.println("Message   = " + detector.getMessage(i));
//            log.debug("2D Image Location = " + locationPixel);
        }

        return detectionList;
    }

    public static ArrayList<FiducialDetectionResult> findFiducials(GrayU8 original) {
        ArrayList<FiducialDetectionResult> detectionList = new ArrayList<>();
        // Detect the fiducials
        FiducialDetector<GrayU8> detector = FactoryFiducial.squareBinary(
                new ConfigFiducialBinary(0.5), ConfigThreshold.local(ThresholdType.LOCAL_MEAN, 25), GrayU8.class);
        detector.detect(original);

//        log.info(detector.totalFound() + " fiducials found.");
        //Iterate over all fiducials found


//        System.out.println(detector.totalFound() + " FOUND");
        for (int i = 0; i < detector.totalFound(); i++) {
            Polygon2D_F64 bounds = new Polygon2D_F64();
            Point2D_F64 locationPixel = new Point2D_F64();
            detector.getCenter(i, locationPixel);
            detector.getBounds(i, bounds);
            FiducialDetectionResult fiducialDetectionResult = new FiducialDetectionResult(locationPixel, bounds, detector.getId(i));
//            System.out.println("ID:" + detector.getId(i));
            detectionList.add(fiducialDetectionResult);
//            if (detector.hasUniqueID())
//                System.out.println("Target ID = " + detector.getId(i));
//            if (detector.hasMessage())
//                System.out.println("Message   = " + detector.getMessage(i));
//            log.debug("2D Image Location = " + locationPixel);
        }

        return detectionList;
    }

    /**
     * Converts a byte array to a BufferedImage
     *
     * @param data
     * @param w
     * @param h
     * @return
     */
    @Deprecated
    public static BufferedImage toBufIm(byte[] data, int w, int h) {
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
        IntBuffer intBuf =
                ByteBuffer.wrap(data)
                        .order(ByteOrder.BIG_ENDIAN)
                        .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);

        for (int i = 0; i < array.length; i++) {
            res.setRGB(i % w, (int) (i / w), Integer.reverse(array[i]));
        }
        return res;
    }

    /**
     * Converts a byte array to BoofCV greyscale image
     *
     * @param data The byte array containing the image
     * @param w    width in pixels
     * @param h    height in pixels
     * @return The GrayF32 image containing the conversion result
     */
    public static GrayF32 toGrayF32Image(short[] data, int w, int h) {
        GrayF32 res = new GrayF32(w, h);

        int iv;
        short sv;
        byte bv;
        for (int i = 0; i < w * h; i++) {
            sv = data[i];
            iv = sv >= 0 ? sv : 0x10000 + sv;
            bv = (byte) ((iv & 0xfff8) >> 6);

            res.set(i % w, i / w, data[i] * 2);
        }


        return res;
    }

    public static GrayS16 toGrayS16Image(short[] data, int w, int h) {

        GrayS16 res = new GrayS16(w, h);
        for (int i = 0; i < w * h; i++) {
            res.set(i % w, (int) (i / w), data[i]);
        }
        return res;
    }

    public static GrayU16 toGrayU16Image(short[] data, int w, int h) {
        GrayU16 res = new GrayU16(w, h);

        res.data = data;
        return res;
    }
}
