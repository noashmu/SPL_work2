package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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


    public Camera(int id, int frequency, STATUS status, String cameraKey,String filePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<>();
        this.cameraKey=cameraKey;
        Initalizer(filePath);
    }
    public void Initalizer(String filePath) {
        try {
            // Parse the camera data JSON file
            JsonObject cameraDataJson = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();

            // Retrieve the data for this specific camera using the camera key
            JsonArray detectedObjectsArray = cameraDataJson.getAsJsonArray(cameraKey);

            if (detectedObjectsArray == null) {
                return;
            }

            // Process the detected objects
            for (JsonElement element : detectedObjectsArray) {
                JsonObject detectedObject = element.getAsJsonObject();
                int time = detectedObject.get("time").getAsInt();
                JsonArray detectedObjectsJsonArray = detectedObject.getAsJsonArray("detectedObjects");


                // Convert JsonArray to List<DetectedObject>
                ArrayList<DetectedObject> detectedObjectsListForTime = new ArrayList<>();
                for (JsonElement objElement : detectedObjectsJsonArray) {
                    JsonObject obj = objElement.getAsJsonObject();
                    String id = obj.get("id").getAsString();
                    String description = obj.get("description").getAsString();
                    detectedObjectsListForTime.add(new DetectedObject(id, description));
                }
                // Add the converted list to the StampedDetectedObjects
                detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjectsListForTime));
            }


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
        if (currTick >= this.frequency && currTick % this.frequency == 0)
            return true;
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
