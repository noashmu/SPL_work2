package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime;
    private int numOfDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;

    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numOfDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    public int getSystemRuntime() { return systemRuntime; }

    public int getNumOfDetectedObjects() { return numOfDetectedObjects; }

    public int getNumTrackedObjects() { return numTrackedObjects; }

    public int getNumLandmarks() { return numLandmarks; }

    public synchronized void incrementRuntime() {
        this.systemRuntime++;
    }

    public synchronized void addDetectedObjects(int count) {
        this.numOfDetectedObjects += count;
    }

    public synchronized void addTrackedObjects(int count) {
        this.numTrackedObjects += count;
    }

    public synchronized void addLandmarks(int count) {
        this.numLandmarks += count;
    }
}
