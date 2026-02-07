package com.myautomation.testrunners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")


@SelectClasspathResource("${features:features}")

@ConfigurationParameter(
        key = "cucumber.filter.tags",
        value = "${tags:}"
)
public class CustomizableTestSuiteTest {
}
