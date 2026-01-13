package com.myautomation.stepdefinitions;

import com.myautomation.core.BaseTest;
import com.myautomation.pages.LoginPage;
import com.myautomation.utils.SessionLogger;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import static com.myautomation.utils.DriverManager.getDriver;

public class LoginSteps extends BaseTest {
    private WebDriver driver;
    private LoginPage loginPage;
    private final SessionLogger logger = new SessionLogger("LoginTest");

    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        driver = getDriver();
        loginPage = new LoginPage(driver);
        driver.get("https://www.saucedemo.com/");
        logger.log("Navigated to login page");
    }

    @When("I enter username {string} and password {string}")
    public void i_enter_username_and_password(String username, String password) {
        logger.log(String.format("Entering username: %s and password: %s", username, password.replaceAll(".", "*")));
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void i_click_the_login_button() {
        logger.log("Clicking login button");
        loginPage.clickLoginButton();
    }

    @Then("I should be logged in successfully")
    public void i_should_be_logged_in_successfully() {
        String currentUrl = driver.getCurrentUrl();
        boolean isLoggedIn = currentUrl.contains("inventory.html");
        logger.log("Verifying successful login. Current URL: " + currentUrl);
        Assert.assertTrue(isLoggedIn, "Login was not successful. Expected to be on inventory page but was on: " + currentUrl);
    }

    @Then("I should see the products page")
    public void i_should_see_the_products_page() {
        String title = driver.getTitle();
        logger.log("Verifying products page. Page title: " + title);
        Assert.assertTrue(title.contains("Swag Labs"));
    }

    @Then("I should see an error message {string}")
    public void i_should_see_an_error_message(String expectedErrorMessage) {
        String actualError = loginPage.getErrorMessage();
        logger.log("Verifying error message. Expected: " + expectedErrorMessage + ", Actual: " + actualError);
        Assert.assertEquals(actualError, expectedErrorMessage);
    }
}
