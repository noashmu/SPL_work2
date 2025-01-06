package bgu.spl.mics.application.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
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

    public GPSIMU(int currentTick, STATUS status,String filePath,String config) {
        this.currentTick = currentTick;
        this.status = status;
        poseList = new ArrayList<>();
        Initalizer(config,filePath);
    }
    public static String resolvePath(String basePath, String relativePath) {
        File baseFile = new File(basePath).getParentFile();
        File resolvedFile = new File(baseFile, relativePath);
        return resolvedFile.getAbsolutePath();
    }
    public void Initalizer(String config,String filePath)
    {
        try {
            String resolvedPath = resolvePath(config,filePath);
            JsonArray poseDataArray = JsonParser.parseReader(new FileReader(resolvedPath)).getAsJsonArray();
            for (JsonElement element : poseDataArray) {
                JsonObject poseObject = element.getAsJsonObject();
                int time = poseObject.get("time").getAsInt();
                double x = poseObject.get("x").getAsDouble();
                double y = poseObject.get("y").getAsDouble();
                double yaw = poseObject.get("yaw").getAsDouble();
                Pose p=new Pose(x, y, yaw,time);
                poseList.add(p);
                FusionSlam.getInstance().addPose(p);
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
