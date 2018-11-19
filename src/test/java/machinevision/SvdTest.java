package machinevision;

import de.rwth.i5.kinectvision.machinevision.SVD;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class SvdTest {
    private static boolean matrixEpsilonEquals(RealMatrix m1, RealMatrix m2, double eps) {
        for (int i = 0; i < m1.getRowDimension(); i++) {
            for (int j = 0; j < m1.getColumnDimension(); j++) {
                if (Math.abs(m1.getEntry(i, j)) > Math.abs(m2.getEntry(i, j)) + eps || Math.abs(m1.getEntry(i, j)) < Math.abs(m2.getEntry(i, j)) - eps) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean vectorEpsilonEquals(RealVector m1, RealVector m2, double eps) {
        for (int j = 0; j < m1.getDimension(); j++) {
            if (Math.abs(m1.getEntry(j)) > Math.abs(m2.getEntry(j)) + eps || Math.abs(m1.getEntry(j)) < Math.abs(m2.getEntry(j)) - eps) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void markerTranslationTest() {
        // Create some random markers
        RealVector realMarker1 = MatrixUtils.createRealVector(new double[]{1, 2, 3});
        RealVector realMarker2 = MatrixUtils.createRealVector(new double[]{2, 3, 4});
        RealVector realMarker3 = MatrixUtils.createRealVector(new double[]{3, 4, 5});

        //And translate them by 1,1,1
        RealVector modelMarker1 = MatrixUtils.createRealVector(new double[]{2, 3, 4});
        RealVector modelMarker2 = MatrixUtils.createRealVector(new double[]{3, 4, 5});
        RealVector modelMarker3 = MatrixUtils.createRealVector(new double[]{4, 5, 6});

        SVD svd = new SVD();
        svd.calculateRotationTranslation(Arrays.asList(realMarker1, realMarker2, realMarker3),
                Arrays.asList(modelMarker1, modelMarker2, modelMarker3));

        RealMatrix compRotation = MatrixUtils.createRealDiagonalMatrix(new double[]{1, 1, 1});
        assertTrue(matrixEpsilonEquals(compRotation, svd.getRotationMatrix(), 0.01));

        RealVector compTranslation = MatrixUtils.createRealVector(new double[]{1, 1, 1});
//        assertEquals(compTranslation, svd.getTranslation());
        assertTrue(vectorEpsilonEquals(compTranslation, svd.getTranslation(), 0.01));
    }
}
