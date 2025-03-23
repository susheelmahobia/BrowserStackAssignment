package org.assignment;

import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

public class browserStackAssignment {
    public static final String USERNAME = "susheelmahobia_Zdq204";
    public static final String ACCESS_KEY = "iL919zF8yRfsNaLsY5Ak";
    public static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";


    public static void main(String[] args) throws IOException, InterruptedException {
        String[][] platforms = {
                {"safari", "iPhone 15 Pro Max", "17"},
                {"chrome", "Samsung Galaxy Tab S9", "13"},
                {"chromium", "iPad 9th", "18"},
                {"chrome", "Windows", "11", "latest"},
                {"edge", "Windows", "10", "latest"}
        };

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (String[] platform : platforms) {
            executor.execute(() -> {
                try {
                    runTestOnBrowser(platform);
                } catch (Exception e) {
                    System.out.println("Error on " + Arrays.toString(platform) + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.MINUTES);
    }

    public static void runTestOnBrowser(String[] platform) throws Exception {
        WebDriver driver;
        String browser = platform[0];
        String os = platform[1];
        String osVersion = platform[2];
        String browserVersion = platform.length > 3 ? platform[3] : "latest";

        Map<String, Object> browserstackOptions = new HashMap<>();
        browserstackOptions.put("osVersion", osVersion);
        browserstackOptions.put("browserVersion", browserVersion);
        browserstackOptions.put("sessionName", "Parallel Test - " + browser);

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.setCapability("bstack:options", browserstackOptions);
            driver = new RemoteWebDriver(new URL(URL), options);
        } else if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions options = new EdgeOptions();
            options.setCapability("bstack:options", browserstackOptions);
            driver = new RemoteWebDriver(new URL(URL), options);
        } else if (browser.equalsIgnoreCase("safari")) {
            browserstackOptions.put("deviceName", os);
            SafariOptions options = new SafariOptions();
            options.setCapability("bstack:options", browserstackOptions);
            driver = new RemoteWebDriver(new URL(URL), options);
        } else if (browser.equalsIgnoreCase("chromium")) {
            browserstackOptions.put("deviceName", os);
            ChromeOptions options = new ChromeOptions();
            options.setCapability("bstack:options", browserstackOptions);
            driver = new RemoteWebDriver(new URL(URL), options);
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.get("https://elpais.com/opinion/");
        Thread.sleep(5000);

        handlePopup(driver);

        List<WebElement> articles = driver.findElements(By.cssSelector("article"));
        List<String> translatedTitles = new ArrayList<>();

        for (int i = 0; i < Math.min(5, articles.size()); i++) {
            try {
                WebElement article = articles.get(i);
                WebElement titleElement = article.findElement(By.xpath("//h2//a"));
                String title = titleElement.getText();
                System.out.println("Title: " + title);

                String articleUrl = article.findElement(By.tagName("a")).getAttribute("href");
                driver.get(articleUrl);
                Thread.sleep(3000);

                handlePopup(driver);

                List<WebElement> paragraphs = driver.findElements(By.cssSelector("p"));
                StringBuilder content = new StringBuilder();
                for (WebElement p : paragraphs) {
                    content.append(p.getText()).append("\n");
                }
                System.out.println("Content: " + content.substring(0, Math.min(content.length(), 500)) + "...");

                try {
                    WebElement imageElement = driver.findElement(By.cssSelector("img"));
                    String imageUrl = imageElement.getAttribute("src");
                    downloadImage(imageUrl, "article_" + (i + 1) + ".jpg");
                    System.out.println("Image downloaded: article_" + (i + 1) + ".jpg");
                } catch (Exception e) {
                    System.out.println("No image found: " + e.getMessage());
                }

                String translatedTitle = translateText(title);
                System.out.println("Translated Title: " + translatedTitle);
                translatedTitles.add(translatedTitle);

                driver.navigate().back();
                Thread.sleep(3000);

                handlePopup(driver);

            } catch (Exception e) {
                System.out.println("Error processing article: " + e.getMessage());
            }
        }

        analyzeWords(translatedTitles);
        driver.quit();
    }


    public static void handlePopup(WebDriver driver) {
        try {
            List<WebElement> acceptButton = driver.findElements(By.xpath("//button//span[text()='Accept']"));
            if (!acceptButton.isEmpty() && acceptButton.get(0).isDisplayed()) {
                acceptButton.get(0).click();
            }
        } catch (Exception e) {
        }
    }

    public static void downloadImage(String imageUrl, String fileName) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, Paths.get(fileName));
        }
    }

    public static String translateText(String text) throws IOException {
        String url = "https://google-translate113.p.rapidapi.com/api/v1/translator/text";
        String apiKey = "0b6ba5b783mshec20ab2bac5e560p172087jsnfd6b892cf57b";
        String apiHost = "google-translate113.p.rapidapi.com";
        String requestBody = String.format("{\"from\":\"auto\", \"to\":\"en\", \"text\":\"%s\"}", text);

        Response response = given()
                .header("Content-Type", "application/json")
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host", apiHost)
                .body(requestBody)
                .when()
                .post(url);

        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("trans");
        } else {
            throw new IOException("Translation failed: " + response.getBody().asString());
        }
    }

    public static void analyzeWords(List<String> titles) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String title : titles) {
            for (String word : title.toLowerCase().replaceAll("[.,!?]", "").split(" ")) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        System.out.println("\nRepeated Words:");
        wordCount.entrySet().stream().filter(entry -> entry.getValue() > 2).forEach(entry ->
                System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
