package bgu.spl.mics.application.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;
    private String description;
    //maybe change
    private ArrayList<CloudPoint> coordinates;

    public TrackedObject(String id, int time, String description) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = new ArrayList<>();
    }

    public TrackedObject(DetectedObject detectedObject, ArrayList<CloudPoint> cloudPoints, int time) {
        this.id = detectedObject.getId();
        this.description = detectedObject.getDescription();
        this.time = time;
        this.coordinates = cloudPoints;
    }
    public String getId()
    {
        return this.id;
    }
    public void transformToGlobalCoordinates(Pose currentPose) {
        for (CloudPoint point : coordinates) {
            // Transform each CloudPoint's local coordinates to global coordinates
            double[] globalCoords = currentPose.transformToGlobal(point.getX(), point.getY());
            point.setX(globalCoords[0]); // Update CloudPoint's x coordinate
            point.setY(globalCoords[1]); // Update CloudPoint's y coordinate
        }
    }

    public String getDescription() {
        return this.description;
    }

    public ArrayList<CloudPoint> getCoordinates() {
        return this.coordinates;
    }
}
