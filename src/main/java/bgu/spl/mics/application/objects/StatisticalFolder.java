package bgu.spl.mics.application.objects;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public synchronized void incrementRuntime(int tick) {
        this.systemRuntime=this.systemRuntime+tick;
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
                                              ArrayList<ArrayList<CloudPoint>> cloudPoints, List<Pose> robotPoses) {
        String jsonContent = "{";
        jsonContent += "\"systemRuntime\": " + systemRuntime + ",";
        jsonContent += "\"numDetectedObjects\": " + numOfDetectedObjects + ",";
        jsonContent += "\"numTrackedObjects\": " + numTrackedObjects + ",";
        jsonContent += "\"numLandmarks\": " + numLandmarks + ",";
        jsonContent += "\"LandMarks\": { \n";
        for(LandMark l:FusionSlam.getInstance().getLandmarks()){
            jsonContent += "\""+l.getId()+"\"" +":{"+"\"id\":" +"\""+ l.getId()+"\""+","  + "\"description\":" +"\""+ l.getDescription() +"\""+","+ "\"coordinates\":[";
            for (CloudPoint point : l.getCoordinates()){
                jsonContent += "{"+"\"x\":" +point.getX() + ",\"y\":" +point.getY() + "},";
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
                    jsonContent += "\"id:\": " + "\""+detect.getId() + "\""+"\"description:\": " +"\""+ detect.getDescription() + "\""+ ",";
                    jsonContent += "},";
                }
                if (!detectedObjects.isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "],";
            }
            if(cloudPoints!=null) {
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
            if(robotPoses!=null) {
                for (Pose pose : robotPoses) {
                    jsonContent += pose + ",";
                }
                if (!robotPoses.isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "]}}";
            }
        }

        if (jsonContent.endsWith(",")) {
            jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
        }
        jsonContent += "}";
        final String filePath2 = filePath + "/output.json";
        final String js2=jsonContent;
        Thread writerThread = new Thread(() -> {
      //      filePath = filePath + "/output.json";
            try (FileWriter file = new FileWriter(filePath2)) {
                file.write(js2);
                file.flush();
            } catch (IOException e) {
                System.err.println("Error creating the output file: " + e.getMessage());
            }


    });
        writerThread.start();
}

        public synchronized void createOutputFileError(String filePath, boolean isError, String errorDescription, String errorSource,
                                                   List<DetectedObject> detectedObjects,
                                                  ArrayList<ArrayList<CloudPoint>> cloudPoints, List<Pose> robotPoses) {
            // Start creating the JSON content
            String jsonContent = "{";

            // Add error information if applicable
            if (isError) {
                jsonContent += "\"error\": \"" + errorDescription + "\",";
                jsonContent += "\"faultySensor\": \"" + errorSource + "\",";
                jsonContent += "\"lastCamerasFrame\": {";
                if (detectedObjects != null && !detectedObjects.isEmpty()) {
                    for (DetectedObject detectedObject : detectedObjects) {
                        jsonContent += "\"" + errorSource + "\": {";
                        jsonContent += "\"time\": " + this.systemRuntime + ",";
                        jsonContent += "\"detectedObjects\": [";
                        jsonContent += "{\"id\": \"" + detectedObject.getId() + "\",";
                        jsonContent += "\"description\": \"" + detectedObject.getDescription() + "\"}";
                        jsonContent += "]},";
                    }
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
                }
                jsonContent += "\n";
                jsonContent += "},";
                jsonContent += "\"lastLiDarWorkerTrackersFrame\": {";
                if (cloudPoints != null && !cloudPoints.isEmpty()) {
                    for (int i = 0; i < cloudPoints.size(); i++) {
                        jsonContent += "\"LiDarWorkerTracker" + (i + 1) + "\": [";
                        for (DetectedObject detectedObject : detectedObjects) {
                            jsonContent += "{\"id\": \"" + detectedObject.getId() + "\",";
                            jsonContent += "\"time\": " + this.systemRuntime + ",";
                            jsonContent += "\"description\": \"" + detectedObject.getDescription() + "\"}";
                            jsonContent += "]},";
                        }
                        for (CloudPoint point : cloudPoints.get(i)) {

                            jsonContent += "\"coordinates\": [";
                            jsonContent += "{\"x\": " + point.getX() + ",\"y\": " + point.getY() + "},";
                            jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
                            jsonContent += "]},";
                        }
                        jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
                        jsonContent += "],";
                    }
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
                }
                jsonContent += "},";
            }
            jsonContent += "\n";
            // Add robot poses
            jsonContent += "\"poses\": [";
            if (robotPoses != null && !robotPoses.isEmpty()) {
                for (Pose pose : robotPoses) {
                    jsonContent += "{\"time\": " + pose.getTime() + ",";
                    jsonContent += "\"x\": " + pose+ ",";
                    jsonContent += "\"y\": " + pose.getY() + ",";
                    jsonContent += "\"yaw\": " + pose.getYaw() + "},";
                }
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
            }
            jsonContent += "],";
            jsonContent += "\n";

            // Add statistics
            jsonContent += "\"statistics\": {";
            jsonContent += "\"systemRuntime\": " + this.systemRuntime + ",";
            jsonContent += "\"numDetectedObjects\": " + this.numOfDetectedObjects+ ",";
            jsonContent += "\"numTrackedObjects\": " +this.numTrackedObjects + ",";
            jsonContent += "\"numLandmarks\": " + this.numLandmarks + ",";
            jsonContent += "\"landMarks\": {";
            for (LandMark landMark : FusionSlam.getInstance().getLandmarks()) {
                jsonContent += "\"" + landMark.getId() + "\": {";
                jsonContent += "\"id\": \"" + landMark.getId() + "\",";
                jsonContent += "\"description\": \"" + landMark.getDescription() + "\",";
                jsonContent += "\"coordinates\": [";
                for (CloudPoint point : landMark.getCoordinates()) {
                    jsonContent += "{\"x\": " + point.getX() + ",\"y\": " + point.getY() + "},";
                }
                if (!landMark.getCoordinates().isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
                }
                jsonContent += "]},";
            }
            if (!FusionSlam.getInstance().getLandmarks().isEmpty()) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1); // Remove trailing comma
            }
            jsonContent += "}}";

            // Close JSON
            jsonContent += "}";

            // Write JSON to file
            final String outputFilePath = filePath + "/output.json";

            try (FileWriter file = new FileWriter(outputFilePath)) {
                file.write(jsonContent);
                file.flush();
            } catch (IOException e) {
                System.err.println("Error creating the output file: " + e.getMessage());
            }
        }
    }
