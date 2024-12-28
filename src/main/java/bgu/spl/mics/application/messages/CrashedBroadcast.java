package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.Pose;


import java.util.List;

public class CrashedBroadcast implements Broadcast {
    private final String errorDescription;
    private final String sensorCausingError;
    private List<DetectedObject> detectedObjects;
    private List<List<CloudPoint>> cloudPoints;
    private List<Pose> poseList;

    public CrashedBroadcast(String errorDescription, String sensorCausingError,
                            List<DetectedObject> detectedObjects, List<List<CloudPoint>> cloudPoints,
                            List<Pose> poseList) {
        this.errorDescription = errorDescription;
        this.sensorCausingError = sensorCausingError;
        this.detectedObjects = detectedObjects;
        this.cloudPoints = cloudPoints;
        this.poseList = poseList;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getSensorCausingError() {
        return sensorCausingError;
    }

    public List<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }

    public List<List<CloudPoint>> getCloudPoints(){
        return cloudPoints;
    }

    public List<Pose> getPoseList(){
        return poseList;
    }
}
