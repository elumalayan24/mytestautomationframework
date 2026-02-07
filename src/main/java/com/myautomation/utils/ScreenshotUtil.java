package com.myautomation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.myautomation.core.drivers.DriverFactory;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotUtil {

    private static final String SCREENSHOT_DIR = "test-output/screenshots";

    public static String capture(WebDriver driver, String name) {
        try {
            File dir = new File(SCREENSHOT_DIR);
            if (!dir.exists()) dir.mkdirs();

            if (driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                File screenshotFile = new File(dir, name + ".png");
                try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                    fos.write(screenshot);
                }
                return screenshotFile.getAbsolutePath();
            }
        } catch (Exception e) {
            System.err.println("[ScreenshotUtil] Failed: " + e.getMessage());
        }
        return null;
    }
}
