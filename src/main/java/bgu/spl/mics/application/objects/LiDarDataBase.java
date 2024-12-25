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

    public List<StampedCloudPoints> getCloudPoints() {
        return this.cloudPoints; // Return a copy for safety
    }

    private void loadDataFromFile(String filePath) {

    }
    public void addCloudPoints(StampedCloudPoints stampedCloudPoints) {
        cloudPoints.add(stampedCloudPoints);
    }
}