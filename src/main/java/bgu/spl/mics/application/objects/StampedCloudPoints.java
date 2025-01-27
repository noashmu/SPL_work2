package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id;
    private int time;
    private ArrayList<ArrayList<Double>> cloudPoints;

    public StampedCloudPoints(String id, int time) {
        this.id = id;
        this.time = time;
        this.cloudPoints = new ArrayList<>();
    }

    public int getTime() {
        return time;
    }

    public String getId(){return id;}

    public ArrayList<ArrayList<Double>> getPoints() {
        return cloudPoints;
    }

    public void setPoints(double x, double y) {
        ArrayList<Double> point = new ArrayList<>();
        point.add(x);
        point.add(y);
        cloudPoints.add(point);
    }
}
