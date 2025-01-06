package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private List<DetectedObject> DetectedObjects;

    public StampedDetectedObjects(int time,List<DetectedObject> l) {
        this.time = time;
        this.DetectedObjects = l;
    }
    public int getTime()
    {
        return this.time;
    }
    public List<DetectedObject> getDetectedObjects()
    {
        return this.DetectedObjects;
    }
}
