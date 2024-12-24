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
        private static final FusionSlam INSTANCE = new FusionSlam();
    }
        public static FusionSlam getInstance() {
            return FusionSlamHolder.INSTANCE;
        }

        public void addLandMark(LandMark l)
        {
            this.landmarks.add(l);
        }

        public void addPose(Pose p)
        {
            this.poses.add(p);
        }
    }

