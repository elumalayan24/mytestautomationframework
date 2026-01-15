package com.myautomation.testrunners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features/login.feature"},
        glue = {"com.myautomation.stepdefinitions"},
        name = "^Successful login with valid credentials$",
        plugin = {
                "pretty",
                "com.myautomation.plugins.TestSuiteIdPlugin"
        }
)
public class DirectCucumberTest {
}
