package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private ArrayList<LandMark> landmarks;
    private List<Pose> poses;

    private FusionSlam()
    {
        this.landmarks=new ArrayList<>();
        this.poses=new ArrayList<>();
    }
    // Singleton instance holder
    private static class FusionSlamHolder {
        // TODO: Implement singleton instance logic.
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

public static FusionSlam getInstance() {
    return FusionSlamHolder.INSTANCE;
}

public boolean isNewLandmark(TrackedObject obj)
   {
       for (LandMark l:landmarks)
       {
           if (obj.getId().equals(l.getId()))
               return false;
       }
       return true;
   }

public void addLandMark(LandMark landMark)
{
    this.landmarks.add(landMark);
}

public void updateLandMark(TrackedObject obj)
{
    for (LandMark landMark : landmarks) {
        if (landMark.getId().equals(obj.getId())) {
            landMark.updateCoordinates(obj.getCoordinates());
            return;
        }
    }
}

    public void addPose(Pose p)
{
    this.poses.add(p);
}
    public Pose getCurrentPose() {
        if (poses.isEmpty())
            return null;
        return poses.get(poses.size() - 1);
    }
    public List<Pose> getPoses() { return this.poses; }

    public ArrayList<LandMark> getLandmarks() { return this.landmarks; }

}

