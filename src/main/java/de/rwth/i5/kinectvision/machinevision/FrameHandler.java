package de.rwth.i5.kinectvision.machinevision;

import de.rwth.i5.kinectvision.machinevision.model.DepthModel;

/**
 * This interface is used as a handler for all kinect streams received.
 */
public interface FrameHandler {
    void onDepthFrame(DepthModel o);

    void OnInfraredFrame(short[] data);

    void onColorFrame(byte[] payload);

}
