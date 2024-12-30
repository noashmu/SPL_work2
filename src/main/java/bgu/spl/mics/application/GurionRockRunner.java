package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.Console;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.rmi.server.Skeleton;
import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        String configPath = args[0];
        Gson gson = new Gson();
        try {
            JsonObject configJson = JsonParser.parseReader(new FileReader(configPath)).getAsJsonObject();
            int tickTime = configJson.get("TickTime").getAsInt();
            int duration = configJson.get("Duration").getAsInt();

            Type cameraListType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<Camera> cameras = new ArrayList<>();

            JsonObject camerasObject = configJson.getAsJsonObject("Cameras");
            if (camerasObject != null && camerasObject.has("CamerasConfigurations")) {
                List<JsonObject> cameraConfigs = gson.fromJson(camerasObject.get("CamerasConfigurations"), cameraListType);

                for (JsonObject cameraConfig : cameraConfigs) {
                    Camera camera = new Camera(
                            cameraConfig.get("id").getAsInt(),
                            cameraConfig.get("frequency").getAsInt(),
                            STATUS.UP,
                            cameraConfig.get("camera_key").getAsString(),
                            camerasObject.get("camera_datas_path").getAsString(),configPath // Accessing the shared path
                    );
                    cameras.add(camera);
                }

                System.out.println("Cameras parsed: " + cameras);
            } else {
                System.out.println("CamerasConfigurations not found in the JSON.");
            }

            List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
            JsonObject lidarObject = configJson.getAsJsonObject("LidarWorkers");

            if (lidarObject != null && lidarObject.has("LidarConfigurations")) {
                Type lidarListType = new TypeToken<List<JsonObject>>() {}.getType();
                List<JsonObject> lidarConfigs = gson.fromJson(lidarObject.get("LidarConfigurations"), lidarListType);

                String lidarDataPath = lidarObject.get("lidars_data_path").getAsString(); // Shared data path

                for (JsonObject lidarConfig : lidarConfigs) {
                    LiDarWorkerTracker lidar = new LiDarWorkerTracker(
                            lidarConfig.get("id").getAsInt(),
                            lidarConfig.get("frequency").getAsInt(),
                            STATUS.UP,
                            lidarDataPath,configPath // Use the shared path
                    );
                    lidarWorkers.add(lidar);
                }

                System.out.println("LidarWorkers parsed: " + lidarWorkers);
            } else {
                System.out.println("LidarConfigurations not found in the JSON.");
            }

            String poseDataPath = configJson.get("poseJsonFile").getAsString();
                GPSIMU gpsimu = new GPSIMU(tickTime, STATUS.UP, poseDataPath,configPath);

                FusionSlam fusionSlam = FusionSlam.getInstance();

                List<Thread> microservices = new ArrayList<>();
                for (Camera camera : cameras) {
                    microservices.add(new Thread(new CameraService(camera)));
                }
                for (LiDarWorkerTracker lidar : lidarWorkers) {
                    microservices.add(new Thread(new LiDarService(lidar)));
                }
                microservices.add(new Thread(new PoseService(gpsimu)));
                microservices.add(new Thread(new FusionSlamService(fusionSlam)));

                // Initialize TimeService
                TimeService timeService = new TimeService(tickTime, duration);
                microservices.add(new Thread(timeService));

                //start all services
                for (Thread service : microservices) {
                    service.run();
                }

                // Wait for all services to finish
                for (Thread service : microservices) {
                    service.join();
                }
                StatisticalFolder s = StatisticalFolder.getInstance();

                //String outputPath = configPath.replace("config.json", "output_file.json");
                //try (FileWriter writer = new FileWriter(outputPath)) {
                //   gson.toJson(s.createOutputFile(configPath,), writer);
                //  }

                //  try (FileWriter writer = new FileWriter(outputPath)) {
                //    gson.toJson(fusionSlam.generateOutput(), writer);
            }

        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}