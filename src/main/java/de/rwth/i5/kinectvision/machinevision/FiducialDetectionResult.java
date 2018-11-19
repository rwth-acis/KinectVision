package de.rwth.i5.kinectvision.machinevision;

import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.Polygon2D_F64;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FiducialDetectionResult {
    private Point2D_F64 center;
    private Polygon2D_F64 bounds;
    private long id;
}
