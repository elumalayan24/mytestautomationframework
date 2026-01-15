package com.myautomation.testrunners;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;



public class TestSuiteRunner {

    public static void main(String[] args) {
        String featuresCsv = System.getProperty("features", "login.feature").trim();
        String glueCsv = System.getProperty("glue", "").trim();
        String tags = System.getProperty("tags", "").trim();
        
        // Auto-detect tags if none specified (for IDE runs)
        if (tags.isEmpty() && !featuresCsv.isEmpty()) {
            // Default to @smoke if feature file has smoke tag
            try {
                String featureContent = new String(Files.readAllBytes(
                    Paths.get("src/test/resources/features/" + featuresCsv)));
                if (featureContent.contains("@smoke")) {
                    tags = "@smoke";
                    System.out.println("Auto-detected @smoke tag - running smoke tests only");
                } else {
                    System.out.println("No @smoke tag found - running all scenarios");
                }
            } catch (Exception e) {
                System.out.println("Could not auto-detect tags, running all scenarios");
            }
        }

        String packagesCsv = System.getProperty("packages", "").trim();
        String classesCsv = System.getProperty("classes", "").trim();

        List<String> features = splitCsv(featuresCsv);
        List<String> glue = splitCsv(glueCsv);
        List<String> packages = splitCsv(packagesCsv);
        List<String> classes = splitCsv(classesCsv);

        if (features.isEmpty() && packages.isEmpty() && classes.isEmpty()) {
            packages = List.of("com.myautomation.tests");
        }

        int cucumberExitCode = 0;
        if (!features.isEmpty()) {
            cucumberExitCode = runCucumber(features, glue, tags);
        }

        boolean testNgFailed = false;
        if (!packages.isEmpty() || !classes.isEmpty()) {
            TestNG testNG = new TestNG();
            testNG.setXmlSuites(List.of(buildSuite(packages, classes)));
            testNG.run();
            testNgFailed = testNG.hasFailure();
        }

        if (cucumberExitCode != 0 || testNgFailed) {
            System.exit(1);
        }
    }

    private static int runCucumber(List<String> features, List<String> glue, String tags) {
        // Create output directory if it doesn't exist
        File reportDir = new File("target/cucumber-reports");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        // Configure Cucumber options
        List<String> cucumberOptions = new ArrayList<>();

        // Add glue packages
        if (!glue.isEmpty()) {
            for (String gluePackage : glue) {
                cucumberOptions.add("--glue");
                cucumberOptions.add(gluePackage);
            }
        } else {
            // Add default glue packages if none specified
            cucumberOptions.add("--glue");
            cucumberOptions.add("com.myautomation.stepdefinitions");
            cucumberOptions.add("--glue");
            cucumberOptions.add("com.myautomation.hooks");
        }
        
        if (tags != null && !tags.isBlank()) {
            cucumberOptions.add("--tags");
            cucumberOptions.add(tags);
        }
        
        // Add other useful plugins
        cucumberOptions.add("--plugin");
        cucumberOptions.add("pretty");
        cucumberOptions.add("--plugin");
        cucumberOptions.add("html:test-output/cucumber-reports.html");
        cucumberOptions.add("--plugin");
        cucumberOptions.add("json:test-output/cucumber.json");
        cucumberOptions.add("--plugin");
        cucumberOptions.add("junit:test-output/junit-report.xml");
        
        System.out.println("\n[INFO] Starting Cucumber Tests...");
        int exitStatus = io.cucumber.core.cli.Main.run(
                cucumberOptions.toArray(new String[0]),
                Thread.currentThread().getContextClassLoader()
        );
        
        // Log the report location
        String extentReportPath = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
        System.out.println("\n[INFO] Test execution completed!");
        System.out.println("[INFO] Extent Report: " + new java.io.File(extentReportPath).getAbsolutePath());
        System.out.println("[INFO] HTML Report: " + new java.io.File(System.getProperty("user.dir") + "/test-output/cucumber-reports.html").getAbsolutePath());
        System.out.println("[INFO] JSON Report: " + new java.io.File(System.getProperty("user.dir") + "/test-output/cucumber.json").getAbsolutePath());
        
        return exitStatus;
    }

    private static XmlSuite buildSuite(List<String> packages, List<String> classes) {
        XmlSuite suite = new XmlSuite();
        suite.setName("TestSuiteRunner");

        XmlTest test = new XmlTest(suite);
        test.setName("DynamicTests");

        if (!packages.isEmpty()) {
            List<XmlPackage> xmlPackages = packages.stream()
                    .map(XmlPackage::new)
                    .collect(Collectors.toList());
            test.setXmlPackages(xmlPackages);
        }

        if (!classes.isEmpty()) {
            List<XmlClass> xmlClasses = classes.stream()
                    .map(XmlClass::new)
                    .collect(Collectors.toList());
            test.setXmlClasses(xmlClasses);
        }

        return suite;
    }

    private static List<String> splitCsv(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
