package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static class LidarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase(); // Singleton instance
    }

    private List<StampedCloudPoints> cloudPoints;
    private List<DetectedObject> detectedObjectsDB;
    private int TrackedObjectsCount;

    private LiDarDataBase() {
        cloudPoints = new ArrayList<>();
        detectedObjectsDB = new ArrayList<>();
        this.TrackedObjectsCount = 0;
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance() {
        return LidarDataBaseHolder.instance;
    }

    public ArrayList<ArrayList<CloudPoint>> getCloudPoints(List<DetectedObject> detectedObjectList, int time) {
        ArrayList<ArrayList<CloudPoint>> cloudPointArrayList = new ArrayList<>();

        for (StampedCloudPoints stampedCloudPoints : cloudPoints) {
            for (DetectedObject detectedObject : detectedObjectList) {
                if (stampedCloudPoints.getId().equals(detectedObject.getId()) && stampedCloudPoints.getTime() == time) {
                    ArrayList<CloudPoint> cloudPointArray = new ArrayList<>();
                    for (List<Double> points : stampedCloudPoints.getPoints()) {
                        cloudPointArray.add(new CloudPoint(points.get(0), points.get(1)));
                    }
                    cloudPointArrayList.add(cloudPointArray);
                }
            }
        }

        return cloudPointArrayList;
    }

    public ArrayList<CloudPoint> getCloudPoints2(DetectedObject d, int time) {
        ArrayList<CloudPoint> list = new ArrayList<>();
        for (StampedCloudPoints stampedCloudPoints : cloudPoints) {
            if (d.getId().equals(stampedCloudPoints.getId()) && stampedCloudPoints.getTime() == time) {
                for (List<Double> points : stampedCloudPoints.getPoints()) {
                    list.add(new CloudPoint(points.get(0), points.get(1)));
                }
            }
        }
        return list;
    }


    public List<DetectedObject> getDetectedObjectsList()
    {
        return this.detectedObjectsDB;
    }
    public DetectedObject getObjectFromID(String id,int time)
    {
        for (DetectedObject d : detectedObjectsDB) {
            if (d.getId().equals(id)) {
                for (StampedCloudPoints s:cloudPoints) {
                    if (d.getId().equals(s.getId())&&s.getTime()==time)
                        return d;
                }

            }
        }
        return null;
    }

    public void addCloudPoints(StampedCloudPoints stampedCloudPoints, double x, double y) {
        stampedCloudPoints.setPoints(x, y);
        cloudPoints.add(stampedCloudPoints);
    }
    public List<StampedCloudPoints> getStamped()
    {
        return this.cloudPoints;
    }
    public void setTrackedObjectsCount(int count)
    {
        this.TrackedObjectsCount=count;

    }
    public int getTrackedObjectsCount()
    {
        return this.TrackedObjectsCount;
    }
}