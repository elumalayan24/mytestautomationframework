package com.myautomation.utils;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestSuiteRunner {

    public static void main(String[] args) {
        String featuresCsv = System.getProperty("features", "elu.feature").trim();
        String glueCsv = System.getProperty("glue", "").trim();
        String tags = System.getProperty("tags", "").trim();

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
        List<String> cucumberArgs = new ArrayList<>();

        cucumberArgs.addAll(features);
        for (String g : glue) {
            cucumberArgs.add("--glue");
            cucumberArgs.add(g);
        }
        if (tags != null && !tags.isBlank()) {
            cucumberArgs.add("--tags");
            cucumberArgs.add(tags);
        }
        cucumberArgs.add("--plugin");
        cucumberArgs.add("pretty");

        return io.cucumber.core.cli.Main.run(
                cucumberArgs.toArray(new String[0]),
                Thread.currentThread().getContextClassLoader()
        );
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
