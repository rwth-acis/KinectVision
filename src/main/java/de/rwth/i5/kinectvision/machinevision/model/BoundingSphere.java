package de.rwth.i5.kinectvision.machinevision.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class BoundingSphere {
    private Vector3d center;
    private double radius;

    public static Collection<? extends BoundingSphere> transform(Matrix4d rotationMatrix, List<BoundingSphere> boundingSpheres) {
        ArrayList<BoundingSphere> res = new ArrayList<>();
        for (BoundingSphere boundingSphere : boundingSpheres) {
            if (boundingSphere == null || boundingSphere.getCenter() == null) continue;
            Vector3d newCenter = new Vector3d(boundingSphere.getCenter());
            Triangle.transformVector(rotationMatrix, newCenter);
            res.add(new BoundingSphere(newCenter, boundingSphere.getRadius()));
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingSphere that = (BoundingSphere) o;
        return that.radius == radius && center.equals(that.center);
    }

    public boolean epsilonEquals(Object o, double eps) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingSphere that = (BoundingSphere) o;
        return Math.abs(that.radius) < radius + eps && Math.abs(that.radius) > radius - eps && center.epsilonEquals(that.center, eps);
    }

    @Override
    public int hashCode() {

        return Objects.hash(center, radius);
    }

    @Override
    public String toString() {
        return "BoundingSphere{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
