package com.myautomation.stepdefinitions;

import com.microsoft.playwright.Page;
import com.myautomation.core.drivers.PlaywrightDriverManager;
import com.myautomation.pages.playwright.PlaywrightLoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import static com.myautomation.utils.LogCaptureUtil.log;

public class PlaywrightLoginSteps {
    private Page page;
    private PlaywrightLoginPage loginPage;

    public PlaywrightLoginSteps() {
        this.page = PlaywrightDriverManager.getDriver();
        this.loginPage = new PlaywrightLoginPage(page);
    }

    @Given("I am on the login page using Playwright")
    public void i_am_on_the_login_page_using_playwright() {
        loginPage.navigate("https://www.saucedemo.com/");
        loginPage.waitForPageLoad();
        log("Navigated to login page using Playwright");
    }

    @When("I enter username {string} and password {string} using Playwright")
    public void i_enter_username_and_password_using_playwright(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        log(String.format("Entered username: %s and password: ******** using Playwright", username));
    }

    @When("I click the login button using Playwright")
    public void i_click_the_login_button_using_playwright() {
        loginPage.clickLoginButton();
        log("Clicked login button using Playwright");
    }

    @Then("I should be logged in successfully using Playwright")
    public void i_should_be_logged_in_successfully_using_playwright() {
        loginPage.waitForPageLoad();
        boolean isLoggedIn = loginPage.isLoggedIn();
        String currentUrl = loginPage.getCurrentUrl();
        log("Verifying successful login using Playwright. Current URL: " + currentUrl);
        Assert.assertTrue(isLoggedIn, "Login was not successful using Playwright. Expected to be on inventory page but was on: " + currentUrl);
    }

    @Then("I should see the products page using Playwright")
    public void i_should_see_the_products_page_using_playwright() {
        String title = loginPage.getTitle();
        log("Verifying products page using Playwright. Page title: " + title);
        Assert.assertTrue(title.contains("Swag Labs"));
    }

    @Then("I should see an error message {string} using Playwright")
    public void i_should_see_an_error_message_using_playwright(String expectedErrorMessage) {
        String actualError = loginPage.getErrorMessage();
        log("Verifying error message using Playwright. Expected: " + expectedErrorMessage + ", Actual: " + actualError);
        Assert.assertEquals(actualError, expectedErrorMessage);
    }
}
