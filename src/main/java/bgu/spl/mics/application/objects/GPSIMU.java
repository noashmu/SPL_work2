package bgu.spl.mics.application.objects;

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

    public GPSIMU(int currentTick, STATUS status) {
        this.currentTick = currentTick;
        this.status = status;
        poseList = new ArrayList<Pose>();
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
