package org.browserstack.assignment.tests;

import org.browserstack.assignment.setup.Setup;
import org.browserstack.assignment.pages.ELPaisPage;
import org.browserstack.assignment.utils.Utilities;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ELPaisTest extends Setup {
    private ELPaisPage elPaisPage;

    public void setup(String browser, String os, String osVersion, String browserVersion) throws MalformedURLException {
        setupDriver(browser, os, osVersion, browserVersion);
        elPaisPage = new ELPaisPage(driver);
    }

    public void elPaisTest() throws InterruptedException {
        driver.get("https://elpais.com/opinion/");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        elPaisPage.acceptCookies();

        List<WebElement> articles = elPaisPage.getArticles();
        List<String> translatedTitles = new ArrayList<>();

        for (WebElement article : articles) {
            String title = elPaisPage.getArticleTitle(article);
            System.out.println("Title: " + title);

            String content = elPaisPage.getArticleContent(article);
            System.out.println("Content: " + content);

            String imageUrl = elPaisPage.getArticleImage(article);
            if (imageUrl != null) {
                Utilities.downloadImage(imageUrl);
            } else {
                System.out.println("No cover image available for this article!");
            }

            String translatedTitle = Utilities.translateText(title);
            System.out.println("Translated Title: " + translatedTitle);

            translatedTitles.add(translatedTitle);
        }

        Utilities.analyzeWords(translatedTitles);
    }

    @AfterMethod
    public void tearDownTest() {
        tearDown();
    }
}
