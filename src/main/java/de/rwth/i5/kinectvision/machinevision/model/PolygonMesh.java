package de.rwth.i5.kinectvision.machinevision.model;

import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Iterator;

@Getter
@Setter
public class PolygonMesh implements Iterable<Triangle> {
    ArrayList<Triangle> faces = new ArrayList<>();
    private Vector3d marker1, marker2, marker3;
//    ArrayList<Face> faces = new ArrayList<>();

    public static PolygonMesh transform(Matrix4d transformationMatrix, PolygonMesh mesh) {
        PolygonMesh res = mesh.copy();
        for (Triangle face : mesh) {
            face.applyTransformation(transformationMatrix);
        }
        return res;
    }

    public void combine(PolygonMesh polygonMesh) {
        for (Triangle mesh : polygonMesh) {
            faces.add(mesh.copy());
        }
    }

    @Override
    public String toString() {
        return "PolygonMesh{" +
                "faces=" + faces +
                '}';
    }

    @Override
    public Iterator<Triangle> iterator() {
//        System.out.println((faces == null) +" is faces null?");
        return faces.iterator();
    }

    /**
     * Creates a deep copy of this object
     *
     * @return The copied object
     */
    public PolygonMesh copy() {
        PolygonMesh res = new PolygonMesh();
        for (Triangle face : this) {
            res.getFaces().add(face.copy());
        }
        return res;
    }
}
