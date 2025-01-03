package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private String filePath;

    public LiDarWorkerTracker(int id, int frequency, STATUS status, String filePath, String config) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        lastTrackedObjects = new ArrayList<TrackedObject>();
        this.filePath=filePath;
        Initalizer(config,filePath);


    }
    public boolean isActive()
    {
        if (this.status==STATUS.UP)
            return true;
        return false;
    }
    public void turnOff()
    {
        this.status=STATUS.DOWN;
    }

    public static String resolvePath(String basePath, String relativePath) {
        File baseFile = new File(basePath).getParentFile(); // Get directory of the base file
        File resolvedFile = new File(baseFile, relativePath);
        return resolvedFile.getAbsolutePath();
    }
    public void Initalizer(String config,String filePath)
    {
        try {
            String resolvedPath = resolvePath(config,filePath);

            JsonArray lidarDataArray = JsonParser.parseReader(new FileReader(resolvedPath)).getAsJsonArray();

            for (JsonElement element : lidarDataArray) {
                JsonObject lidarObject = element.getAsJsonObject();
                int time = lidarObject.get("time").getAsInt();
                String id = lidarObject.get("id").getAsString();
                JsonArray cloudPointsArray = lidarObject.getAsJsonArray("cloudPoints");

                ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
                for (JsonElement pointElement : cloudPointsArray) {
                    JsonArray point = pointElement.getAsJsonArray();
                    double x = point.get(0).getAsDouble();
                    double y = point.get(1).getAsDouble();
                    cloudPoints.add(new CloudPoint(x,y));
                    LiDarDataBase.getInstance().addCloudPoints(new StampedCloudPoints(id,time), x,y);
                }

                DetectedObject d=LiDarDataBase.getInstance().getObjectFromID(id);
                lastTrackedObjects.add(new TrackedObject(d,cloudPoints,time));
            }
        } catch (IOException e) {
            System.err.println("Error reading lidar data file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing lidar: " + e.getMessage());
        }
    }

    public int getId()
    {
        return this.id;
    }

    public List<TrackedObject> getLastTrackedObjects(){
        return this.lastTrackedObjects;
    }

    public void setLastTrackedObjects(List<DetectedObject> detectedObjectList, int time){
        this.lastTrackedObjects = new ArrayList<>();
        ArrayList<ArrayList<CloudPoint>> cloudPoints = LiDarDataBase.getInstance().getCloudPoints(detectedObjectList);
        int i=0;

        for(DetectedObject detectedObject : detectedObjectList){
            this.lastTrackedObjects.add(new TrackedObject(detectedObject,cloudPoints.get(i), time));
            i++;
        }
    }

    public boolean shouldSendEvent(int currTick) {
        if(!lastTrackedObjects.isEmpty()) {
            for (TrackedObject t:lastTrackedObjects) {
                if (t.getTime() + frequency == currTick)
                    return true;
            }
        }
        return false;
    }
    public boolean detectError()
    {
        for (TrackedObject obj : lastTrackedObjects) {
            if ("ERROR".equals(obj.getId())) {
                return true;
            }
        }
        return false;
    }

}
