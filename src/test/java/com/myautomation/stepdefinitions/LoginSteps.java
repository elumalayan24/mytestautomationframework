package com.myautomation.stepdefinitions;

import com.myautomation.core.drivers.DriverManager;
import com.myautomation.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import static com.myautomation.utils.ReporterClass.logInfo;

public class LoginSteps {
    private final WebDriver driver;
    private LoginPage loginPage;

    public LoginSteps() {
        this.driver = DriverManager.getDriver();
    }

    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        loginPage = new LoginPage(driver);
        driver.get("https://www.saucedemo.com/");
        logInfo("Navigated to login page");
    }

    @When("I enter username {string} and password {string}")
    public void i_enter_username_and_password(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        logInfo(String.format("Entered username: %s and password: ********", username));
    }

    @When("I click the login button")
    public void i_click_the_login_button() {
        loginPage.clickLoginButton();
        logInfo("Clicked login button");
    }

    @Then("I should be logged in successfully")
    public void i_should_be_logged_in_successfully() {
        String currentUrl = driver.getCurrentUrl();
        boolean isLoggedIn = currentUrl.contains("inventory.html");
        logInfo("Verifying successful login. Current URL: " + currentUrl);
        Assert.assertTrue(isLoggedIn, "Login was not successful. Expected to be on inventory page but was on: " + currentUrl);
    }

    @Then("I should see the products page")
    public void i_should_see_the_products_page() {
        String title = driver.getTitle();
        logInfo("Verifying products page. Page title: " + title);
        Assert.assertTrue(title.contains("Swag Labs"));
    }

    @Then("I should see an error message {string}")
    public void i_should_see_an_error_message(String expectedErrorMessage) {
        String actualError = loginPage.getErrorMessage();
        logInfo("Verifying error message. Expected: " + expectedErrorMessage + ", Actual: " + actualError);
        Assert.assertEquals(actualError, expectedErrorMessage);
    }
}
