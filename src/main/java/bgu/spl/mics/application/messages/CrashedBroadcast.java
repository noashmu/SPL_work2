package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


import java.util.ArrayList;
import java.util.List;

public class CrashedBroadcast implements Broadcast {
    private final String errorDescription;
    private final String sensorCausingError;
    //private StampedDetectedObjects detectedObjects;
    private ArrayList<ArrayList<CloudPoint>> cloudPoints;
    private List<Pose> poseList;
    private int errorTick;

    public CrashedBroadcast(String errorDescription, String sensorCausingError,
                            ArrayList<ArrayList<CloudPoint>> cloudPoints,
                            List<Pose> poseList, int errorTick) {
        this.errorDescription = errorDescription;
        this.sensorCausingError = sensorCausingError;
        //this.detectedObjects = detectedObjects;
        this.cloudPoints = cloudPoints;
        this.poseList = poseList;
        this.errorTick = errorTick;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getSensorCausingError() {
        return sensorCausingError;
    }

    //public StampedDetectedObjects getDetectedObjects(){
        //return detectedObjects;
    //}

    public ArrayList<ArrayList<CloudPoint>> getCloudPoints(){
        return cloudPoints;
    }

    public List<Pose> getPoseList(){
        return poseList;
    }

    public int getErrorTick(){
        return errorTick;
    }

}
