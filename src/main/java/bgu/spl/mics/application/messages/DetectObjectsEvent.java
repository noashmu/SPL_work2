package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

public class DetectObjectsEvent implements Event<Boolean> {

    private List<DetectedObject> detectedObjects;
    private int time;

    public DetectObjectsEvent(List<DetectedObject> l,int t)
    {
        this.detectedObjects=l;
        this.time=t;
    }

    public List<DetectedObject> getDetectedObjects()
    {
        return this.detectedObjects;
    }

    public int getTime()
    {
        return this.time;
    }
}
