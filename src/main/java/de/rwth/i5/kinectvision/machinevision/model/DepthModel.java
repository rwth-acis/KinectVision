package de.rwth.i5.kinectvision.machinevision.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class DepthModel implements Serializable {
    @Getter
    @Setter
    private short[] depthFrame;
    @Getter
    @Setter
    private byte[] playerIndex;
    @Getter
    @Setter
    private float[] XYZ;
    @Getter
    @Setter
    private float[] UV;
    @Getter
    @Setter
    private int width;
    @Getter
    @Setter
    private int height;


    public DepthModel(short[] depthFrame,
                      byte[] playerIndex,
                      float[] XYZ,
                      float[] UV, int width, int height) {
        this.depthFrame = depthFrame;
        this.playerIndex = playerIndex;
        this.XYZ = XYZ;
        this.UV = UV;
        this.width = width;
        this.height = height;
    }
}
