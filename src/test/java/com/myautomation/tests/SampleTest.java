package com.myautomation.tests;

import com.myautomation.core.BaseTest;
import com.myautomation.pages.BasePage;
import org.testng.annotations.Test;

public class SampleTest extends BaseTest {
    
    @Test(description = "Verify application loads successfully")
    public void testApplicationLoad() {
        // Example test case
        System.out.println("Application URL: " + driver.getCurrentUrl());
        // Add your test assertions here
    }
    
    @Test(description = "Sample test with page object model")
    public void testWithPageObject() {
        // Example of using page objects
        // HomePage homePage = new HomePage(driver);
        // homePage.performSomeAction();
        
        // Add your test assertions here
        System.out.println("Test with page object model");
    }
}
