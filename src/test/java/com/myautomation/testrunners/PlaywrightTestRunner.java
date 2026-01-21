package com.myautomation.testrunners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features/playwright-login.feature"},
        glue = {"com.myautomation.stepdefinitions", "com.myautomation.hooks"},
        plugin = {
                "pretty",
                "html:test-output/playwright-cucumber-reports.html",
                "json:test-output/playwright-cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
                "com.myautomation.adapters.CustomExtentCucumberAdapter:",
                "parallel"
        },
        dryRun = false,
        strict = true,
        monochrome = true
)
public class PlaywrightTestRunner {
}
