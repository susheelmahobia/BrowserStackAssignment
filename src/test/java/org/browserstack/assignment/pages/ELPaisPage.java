package org.browserstack.assignment.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ELPaisPage {
    private WebDriver driver;

    // Locators
    private static final By ACCEPT_BTN = By.xpath("//button//span[text()='Accept']");
    private static final By FIRST_5_ARTICLES = By.xpath("(//article)[position() <= 5]");
    private static final By ARTICLE_TITLE = By.xpath(".//h2//a");
    private static final By PARAGRAPHS = By.cssSelector("p");
    private static final By IMAGE = By.xpath(".//figure//img");

    // Constructor
    public ELPaisPage(WebDriver driver) {
        this.driver = driver;
    }

    // Methods to interact with elements
    public void acceptCookies() {
        List<WebElement> acceptButtons = driver.findElements(ACCEPT_BTN);
        if (!acceptButtons.isEmpty() && acceptButtons.get(0).isDisplayed()) {
            acceptButtons.get(0).click();
        }
    }

    public List<WebElement> getArticles() {
        return driver.findElements(FIRST_5_ARTICLES);
    }

    public String getArticleTitle(WebElement article) {
        return article.findElement(ARTICLE_TITLE).getText();
    }

    public String getArticleContent(WebElement article) {
        return article.findElement(PARAGRAPHS).getText();
    }

    public String getArticleImage(WebElement article) {
        List<WebElement> images = article.findElements(IMAGE);
        return images.isEmpty() ? null : images.get(0).getAttribute("src");
    }
}
