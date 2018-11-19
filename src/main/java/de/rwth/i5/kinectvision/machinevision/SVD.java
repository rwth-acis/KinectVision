package de.rwth.i5.kinectvision.machinevision;

import lombok.Getter;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for singular value decomposition
 */
@Getter
public class SVD {
    RealMatrix rotationMatrix;
    RealVector translation;

    private static RealVector centroid(List<RealVector> vecs) {
        RealVector res = MatrixUtils.createRealVector(new double[]{0, 0, 0});
        for (RealVector vec : vecs) {
            res = res.add(vec);
        }
        res = res.mapMultiply(1.0 / vecs.size());
        return res;
    }

    private static RealMatrix createCovarianceMatrix(List<RealVector> xList, List<RealVector> yList) {
        RealMatrix xMatrix = MatrixUtils.createRealMatrix(3, xList.size());
        RealMatrix yMatrix = MatrixUtils.createRealMatrix(3, yList.size());
        for (int i = 0; i < xList.size(); i++) {
            xMatrix.setColumnVector(i, xList.get(i));
        }

        for (int i = 0; i < yList.size(); i++) {
            yMatrix.setColumnVector(i, yList.get(i));
        }

        yMatrix = yMatrix.transpose();
        RealMatrix res = xMatrix.multiply(yMatrix);
        return res;
    }

    private static RealMatrix getRotationMatrix(RealMatrix sMatrix) {
        SingularValueDecomposition decomposition = new SingularValueDecomposition(sMatrix);
        RealMatrix V = decomposition.getV();
        RealMatrix U = decomposition.getU();
        U = U.transpose();

        double[] diag = new double[]{1, 1, new LUDecomposition(V.multiply(U)).getDeterminant()};
        RealMatrix detMatrix = MatrixUtils.createRealDiagonalMatrix(diag);
        return V.multiply(detMatrix).multiply(U);
    }

    private static RealVector getTranslation(RealVector qC, RealMatrix rMatrix, RealVector pC) {
        return qC.subtract(rMatrix.operate(pC));
    }

    private static ArrayList<RealVector> createCenteredVectors(List<RealVector> vecs, RealVector centroid) {
        ArrayList<RealVector> res = new ArrayList<>();
        for (RealVector vec : vecs) {
            RealVector vector = new ArrayRealVector(vec);
            vector = vector.subtract(centroid);
            res.add(vector);
        }
        return res;
    }

    /**
     * Calculates the rotation matrix and the translation vector for transforming a set of points to a corresponding
     * set of points using Singular Value Decomposition (SVD) and least-squares method. The points have to be aligned
     * properly, i.e. the first point in the source point set must correspond to the first point in the target set.
     *
     * @param pointSetSource The source point set which is going to be transformed
     * @param pointSetTarget The target point set
     * @return The translation vector
     */
    public void calculateRotationTranslation(List<RealVector> pointSetSource, List<RealVector> pointSetTarget) {
        RealVector pC = centroid(pointSetSource);
        RealVector qC = centroid(pointSetTarget);

        ArrayList<RealVector> xList = createCenteredVectors(pointSetSource, pC);
        ArrayList<RealVector> yList = createCenteredVectors(pointSetTarget, qC);

        RealMatrix covariance = createCovarianceMatrix(xList, yList);

        this.rotationMatrix = getRotationMatrix(covariance);
        this.translation = getTranslation(qC, this.rotationMatrix, pC);
    }
}
