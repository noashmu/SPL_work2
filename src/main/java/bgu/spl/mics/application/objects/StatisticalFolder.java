package bgu.spl.mics.application.objects;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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

    private static class StatisticalFolderHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numOfDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    public static StatisticalFolder getInstance() {return StatisticalFolderHolder.instance;}

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

    public synchronized void addLandmark() {
        this.numLandmarks++;
    }

    public synchronized void createOutputFile(String filePath, boolean isError, String errorDescription,
                                              String errorSource, List<DetectedObject> detectedObjects,
                                              List<List<CloudPoint>> cloudPoints, List<Pose> robotPoses) {
        String jsonContent = "{";
        jsonContent += "\"systemRuntime\": " + systemRuntime + ",";
        jsonContent += "\"numDetectedObjects\": " + numOfDetectedObjects + ",";
        jsonContent += "\"numTrackedObjects\": " + numTrackedObjects + ",";
        jsonContent += "\"numLandmarks\": " + numLandmarks + ",";
        jsonContent += "\"LandMarks\": { \n";
        for(LandMark l:FusionSlam.getInstance().getLandmarks()){
            jsonContent += ""+l.getId() +"\"{id\":" + l.getId() + "\"description\":" + l.getDescription() + "\"coordinates\":[";
            for (CloudPoint point : l.getCoordinates()){
                jsonContent += "\"{x\":" +point.getX() + ",\"y\":" +point.getY() + "},";
            }
            if (!l.getCoordinates().isEmpty()) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
            }
            jsonContent += "]\n";
        }

        if (isError) {
            jsonContent += "\"error\": {";
            jsonContent += "\"source\": \"" + errorSource + "\",";
            jsonContent += "\"description\": \"" + errorDescription + "\",";
            jsonContent += "\"lastFrames\": {";

            if(detectedObjects!=null || !detectedObjects.isEmpty()) {

                jsonContent += "\"detectedObjects\": [";
                for (DetectedObject detect : detectedObjects) {
                    jsonContent += "{";
                    jsonContent += "\"id:\": " + detect.getId() + "\"description:\": " + detect.getDescription() + ",";
                    jsonContent += "},";
                }
                if (!detectedObjects.isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "],";
            }
            if(cloudPoints!=null || !cloudPoints.isEmpty()) { 
                jsonContent += "\"lidarData\": [";
                for (List<CloudPoint> points : cloudPoints) {
                    for (CloudPoint point : points) {
                        jsonContent += "(" + point.getX() + "," + point.getY() + "),";
                    }
                }
                if (!cloudPoints.isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "],";
            }

            jsonContent += "\"robotPoses\": [";
            for (Pose pose : robotPoses) {
                jsonContent += pose + ",";
            }
            if (!robotPoses.isEmpty()) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
            }
            jsonContent += "]}}";
        }

        if (jsonContent.endsWith(",")) {
            jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
        }
        jsonContent += "}";

        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonContent);
            file.flush();
        } catch (IOException e) {
            //System.err.println("Error creating the output file: " + e.getMessage());
        }
    }

}
