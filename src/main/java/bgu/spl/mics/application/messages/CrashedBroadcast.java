package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;


import java.util.ArrayList;
import java.util.List;

public class CrashedBroadcast implements Broadcast {
    private final String errorDescription;
    private final String sensorCausingError;
    private DetectedObject detectedObject;
    private ArrayList<ArrayList<CloudPoint>> cloudPoints;
    private List<Pose> poseList;

    public CrashedBroadcast(String errorDescription, String sensorCausingError,
                            DetectedObject detectedObject, ArrayList<ArrayList<CloudPoint>> cloudPoints,
                            List<Pose> poseList) {
        this.errorDescription = errorDescription;
        this.sensorCausingError = sensorCausingError;
        this.detectedObject = detectedObject;
        this.cloudPoints = cloudPoints;
        this.poseList = poseList;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getSensorCausingError() {
        return sensorCausingError;
    }

    public DetectedObject getDetectedObjects(){
        return detectedObject;
    }

    public ArrayList<ArrayList<CloudPoint>> getCloudPoints(){
        return cloudPoints;
    }

    public List<Pose> getPoseList(){
        return poseList;
    }

}
