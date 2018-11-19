package de.rwth.i5.kinectvision.machinevision.model;

import lombok.AllArgsConstructor;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.Objects;

@AllArgsConstructor
public class Triangle {
    public Vector3d a, b, c;

    public void translate(Vector3d translationVector) {
        a.add(translationVector);
        b.add(translationVector);
        c.add(translationVector);
    }

    public static void transformVector(Matrix4d transformationMatrix, Vector3d vector3d) {
        double x, y, z;

        x = transformationMatrix.m00 * vector3d.x + transformationMatrix.m01 * vector3d.y + transformationMatrix.m02 * vector3d.z + transformationMatrix.m03;
        y = transformationMatrix.m10 * vector3d.x + transformationMatrix.m11 * vector3d.y + transformationMatrix.m12 * vector3d.z + transformationMatrix.m13;
        z = transformationMatrix.m20 * vector3d.x + transformationMatrix.m21 * vector3d.y + transformationMatrix.m22 * vector3d.z + transformationMatrix.m23;

        vector3d.x = x;
        vector3d.y = y;
        vector3d.z = z;
    }

    /**
     * Applys a given transformation matrix to the vectors in the triangle
     *
     * @param transformationMatrix The transformation matrix to apply
     */
    public void applyTransformation(Matrix4d transformationMatrix) {
        //Transform every vector
        transformVector(transformationMatrix, a);
        transformVector(transformationMatrix, b);
        transformVector(transformationMatrix, c);
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triangle triangle = (Triangle) o;
        return Objects.equals(a, triangle.a) &&
                Objects.equals(b, triangle.b) &&
                Objects.equals(c, triangle.c);
    }

    public boolean equalsEps(Triangle triangle, double eps) {
        return (triangle.a.epsilonEquals(a, eps) && triangle.b.epsilonEquals(b, eps) && triangle.c.epsilonEquals(c, eps));
    }

    @Override
    public int hashCode() {

        return Objects.hash(a, b, c);
    }


    public Triangle copy() {
        Vector3d a, b, c;
        a = new Vector3d(this.a.x, this.a.y, this.a.z);
        b = new Vector3d(this.b.x, this.b.y, this.b.z);
        c = new Vector3d(this.c.x, this.c.y, this.c.z);
        Triangle res = new Triangle(a, b, c);
        return res;
    }
}
