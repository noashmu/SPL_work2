package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    private String cameraKey;


    public Camera(int id, int frequency, STATUS status, String cameraKey,String filePath,String configPath) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<>();
        this.cameraKey=cameraKey;
        Initalizer(configPath,filePath);
    }

    public static String resolvePath(String basePath, String relativePath) {
        File baseFile = new File(basePath).getParentFile(); // Get directory of the base file
        File resolvedFile = new File(baseFile, relativePath);
        return resolvedFile.getAbsolutePath();
    }


    public void Initalizer(String config,String filePath) {
       try
        {

            // Parse the camera data JSON file
            String resolvedPath = resolvePath(config,filePath);

            JsonObject cameraDataJson = JsonParser.parseReader(new FileReader(resolvedPath)).getAsJsonObject();


            // Retrieve the data for this specific camera using the camera key
            JsonArray cameraArray = cameraDataJson.getAsJsonArray(cameraKey);

            if (cameraArray == null) {
                return;
            }

            // Process the detected objects
          //  for (JsonElement outerElement : cameraArray) {
               // JsonArray innerArray =outerElement.getAsJsonArray();
                for (JsonElement innerElement: cameraArray) {
                    JsonObject detectedObject = innerElement.getAsJsonObject();
                    int time = detectedObject.get("time").getAsInt();
                    JsonArray detectedObjectsJsonArray = detectedObject.getAsJsonArray("detectedObjects");
                    ArrayList<DetectedObject> detectedObjectsListForTime = new ArrayList<>();
                    for (JsonElement objElement : detectedObjectsJsonArray) {
                        JsonObject obj = objElement.getAsJsonObject();
                        String id = obj.get("id").getAsString();
                        String description = obj.get("description").getAsString();
                        DetectedObject d=new DetectedObject(id, description);
                        detectedObjectsListForTime.add(d);
                        LiDarDataBase.getInstance().getDetectedObjectsList().add(d);

                    }
                    detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjectsListForTime));
                }
        //    }

        } catch (IOException e) {
            System.err.println("Error reading camera data file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing camera: " + e.getMessage());
        }
    }




    public boolean isActive() {
        if (status.equals(STATUS.UP))
            return true;
        return false;
    }

    public boolean shouldSendEvent(int currTick) {
        if (this.frequency!=0) {
            if (currTick >= this.frequency && currTick % this.frequency == 0)
                return true;
        }
        return false;

    }


    public List<DetectedObject> getDetectedObject(int currTick) {
        for (StampedDetectedObjects stampedObject : detectedObjectsList) {
            if (stampedObject.getTime() == currTick) {
                return stampedObject.getDetectedObjects();
            }
        }
        return new ArrayList<>();

    }

    public DetectObjectsEvent createDetectObjectsEvent(int currTick) {

        return new DetectObjectsEvent(getDetectedObject(currTick),currTick);
    }

    public STATUS getStatus() {
        return status;
    }

    public boolean detectError(int currTick)
    {
        List<DetectedObject> detectedObjects = getDetectedObject(currTick);
        for (DetectedObject obj : detectedObjects) {
            if ("ERROR".equals(obj.getId())) {
                return true;
            }
        }
        return false;
    }

    //אולי צריך לאחד את זה עם הפעולה הקודמת
    public String errorDescription(int currTick)
    {
        List<DetectedObject> detectedObjects = getDetectedObject(currTick);
        for (DetectedObject obj : detectedObjects) {
            if ("ERROR".equals(obj.getId())) {
                return obj.getDescription();
            }
        }
        return null;
    }
}
