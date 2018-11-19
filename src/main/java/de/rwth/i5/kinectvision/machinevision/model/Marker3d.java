package de.rwth.i5.kinectvision.machinevision.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3d;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class Marker3d {
    //    double x, y, z;
    long id;
    Vector3d position;

    public Marker3d() {

    }

    public Marker3d(double v, double v1, double v2, long id) {
        this.position = new Vector3d(v, v1, v2);
        this.id = id;
    }

    @Override
    public String toString() {
        return "Marker3d{" +
                "id=" + id +
                ", position=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Marker3d marker3d = (Marker3d) o;
        return id == marker3d.id &&
                Objects.equals(position, marker3d.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position);
    }
}
