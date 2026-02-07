package com.myautomation.hooks;

import com.myautomation.database.DatabaseService;
import com.myautomation.database.DatabaseConfig;
import com.myautomation.config.ConfigManager;
import io.cucumber.java.*;

public class DatabaseHooks {
    
    @BeforeAll
    public static void beforeAll() {
        // Don't initialize test suite here - let startScenario detect engine from tags
        System.out.println("Database hooks initialized - engine will be detected from scenario tags");
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        // Start tracking scenario in database
        DatabaseService.startScenario(scenario);
        
        // Log scenario start
        DatabaseService.logInfo("Scenario Started", 
            "Name: " + scenario.getName() + 
            ", Tags: " + scenario.getSourceTagNames() +
            ", URI: " + scenario.getUri().toString());
    }
    
    @After
    public void afterScenario(Scenario scenario) {
        // Finish scenario tracking
        DatabaseService.finishScenario(scenario);
        
        // Log scenario completion with status
        String status = scenario.isFailed() ? "FAILED" : "PASSED";
        DatabaseService.logInfo("Scenario Completed", 
            "Name: " + scenario.getName() + 
            ", Status: " + status);
        
        // Log error if scenario failed
        if (scenario.isFailed()) {
            DatabaseService.logError("Scenario Failure", 
                "Scenario: " + scenario.getName() + 
                " failed. Check reports for details.");
        }
    }
    
    @AfterAll
    public static void afterAll() {
        // Finish test suite
        DatabaseService.finishTestSuite();
        
        // Print test summary
        DatabaseService.printTestSummary();
        
        // Don't close database connection here - let it be closed by JVM shutdown
        System.out.println("Database hooks completed - connection will be closed on JVM shutdown");
    }
}
