package com.myautomation.testrunners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"com.myautomation.stepdefinitions", "com.myautomation.hooks"},
        features = "src/test/resources/features",
        plugin = {
                "pretty",
                "html:test-output/cucumber-reports.html",
                "json:test-output/cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        }
)
public class TestRunner {
}
