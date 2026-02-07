package com.myautomation.testrunners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features/playwright-sample.feature",
                   "src/test/resources/features/screenshot-test.feature"},
        glue = "com.myautomation",
        plugin = {
                "pretty",
                "html:test-output/playwright-cucumber-report.html",
                "json:test-output/playwright-cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "@playwright"
)
public class PlaywrightTestRunner {
}
