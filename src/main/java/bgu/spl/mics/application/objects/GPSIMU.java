package bgu.spl.mics.application.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU(int currentTick, STATUS status,String filePath) {
        this.currentTick = currentTick;
        this.status = status;
        poseList = new ArrayList<Pose>();
        Initalizer(filePath);
    }
    public void Initalizer(String filePath)
    {
        try {
            JsonArray poseDataArray = JsonParser.parseReader(new FileReader(filePath)).getAsJsonArray();

            for (JsonElement element : poseDataArray) {
                JsonObject poseObject = element.getAsJsonObject();
                int time = poseObject.get("time").getAsInt();
                double x = poseObject.get("x").getAsDouble();
                double y = poseObject.get("y").getAsDouble();
                double yaw = poseObject.get("yaw").getAsDouble();

                poseList.add(new Pose(x, y, yaw,time));
            }
        } catch (IOException e) {
            System.err.println("Error reading pose data file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing pose data: " + e.getMessage());
        }
    }
    public Pose getPose(int time){
        for(Pose pose : poseList){
            if (time==pose.getTime())
                return pose;
        }
        return null;
    }
    public STATUS getStatus()
    {
        return this.status;
    }

    public List<Pose> getPoseList(){
        return this.poseList;
    }
}
