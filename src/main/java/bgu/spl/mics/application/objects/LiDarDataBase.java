package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static LiDarDataBase instance = null; // Singleton instance

    private List<StampedCloudPoints> cloudPoints;

    private LiDarDataBase() {
        cloudPoints = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase();
            instance.loadDataFromFile(filePath);
        }
        return instance;
    }

    public ArrayList<ArrayList<CloudPoint>> getCloudPoints(List<DetectedObject> detectedObjectList) {
        ArrayList<ArrayList<CloudPoint>> cloudPointArrayList = new ArrayList<>(); //each list represent the points of detected object

        for(StampedCloudPoints stampedCloudPoints : cloudPoints) {
            for(int i=0; i<detectedObjectList.size(); i++) {
                if(stampedCloudPoints.getId().equals(detectedObjectList.get(i).getId())) {
                    ArrayList<CloudPoint> cloudPointArray = new ArrayList<>();
                    for(List<Double> points: stampedCloudPoints.getPoints()){
                        cloudPointArray.add(new CloudPoint(points.get(0), points.get(1)));
                    }
                    cloudPointArrayList.add(i, cloudPointArray);
                }
            }
        }

        return cloudPointArrayList;
    }

    private void loadDataFromFile(String filePath) {

    }

    public void addCloudPoints(StampedCloudPoints stampedCloudPoints) {
        cloudPoints.add(stampedCloudPoints);
    }
}