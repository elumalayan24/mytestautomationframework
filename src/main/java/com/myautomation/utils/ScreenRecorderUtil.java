package com.myautomation.utils;

import java.io.File;

public class ScreenRecorderUtil {

    private static Process recorderProcess;
    private static String ffmpegPath;

    private static String findFFmpegPath() {
        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -version");
            if (process.waitFor() == 0) {
                return "ffmpeg";
            }
        } catch (Exception e) {
            System.out.println("[ScreenRecorder] FFmpeg not found: " + e.getMessage());
        }
        return null;
    }

    public static void start(String scenarioName) {
        try {
            // Find FFmpeg path
            if (ffmpegPath == null) {
                ffmpegPath = findFFmpegPath();
            }
            
            if (ffmpegPath == null) {
                System.out.println("[ScreenRecorder] FFmpeg not found. Screen recording disabled.");
                return;
            }

            File dir = new File("test-output/recordings");
            if (!dir.exists()) dir.mkdirs();

            String filePath = dir.getAbsolutePath() + "/" + scenarioName.replaceAll("\\s+", "_") + ".mp4";

            String command = String.format(
                    "%s -y -f gdigrab -draw_mouse 0 -framerate 10 -i desktop -pix_fmt yuv420p -vf \"drawtext=fontfile=C\\:/Windows/Fonts/arial.ttf:text='%s':fontcolor=white:fontsize=24:x=10:y=10\" -c:v libx264 -preset ultrafast -crf 23 -threads 2 %s", 
                    ffmpegPath, scenarioName, filePath
            );
            recorderProcess = Runtime.getRuntime().exec(command);
            System.out.println("[ScreenRecorder] Started recording: " + filePath);
        } catch (Exception e) {
            System.err.println("[ScreenRecorder] Failed to start: " + e.getMessage());
        }
    }

    public static void stop(String scenarioName) {
        try {
            if (recorderProcess != null) {
                recorderProcess.destroy();
            }
        } catch (Exception e) {
            System.err.println("[ScreenRecorder] Failed to stop: " + e.getMessage());
        }
    }
}
