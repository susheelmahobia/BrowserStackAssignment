package org.browserstack.assignment.setup;

import org.browserstack.assignment.utils.Utilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Setup {
    protected WebDriver driver;
    private static final String BROWSERSTACK_URL = Utilities.getProperty("browserstack.url");

    public void setupDriver(String browser, String os, String osVersion, String browserVersion) throws MalformedURLException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Map<String, Object> browserstackOptions = new HashMap<>();
        browserstackOptions.put("osVersion", osVersion);
        browserstackOptions.put("browserVersion", browserVersion);
        browserstackOptions.put("buildName","Build-" + now.format(formatter));

        MutableCapabilities options;
        switch (browser.toLowerCase()) {
            case "chrome", "chromium":
                options = new ChromeOptions();
                if (browser.equalsIgnoreCase("chromium")) {
                    browserstackOptions.put("deviceName", os);
                }
                break;
            case "edge":
                options = new EdgeOptions();
                break;
            case "firefox":
                options = new FirefoxOptions();
                break;
            case "safari":
                options = new SafariOptions();
                browserstackOptions.put("deviceName", os);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        options.setCapability("bstack:options", browserstackOptions);
        driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), options);

        // Common driver settings
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
