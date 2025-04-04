package org.browserstack.assignment.utils;

import io.restassured.response.Response;
import org.testng.Assert;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class Utilities {
    private static final Map<String, Object> config;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Utilities.class.getClassLoader().getResourceAsStream("config.yml")) {
            if (inputStream == null) {
                throw new RuntimeException("config.yml not found in resources folder");
            }
            config = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.yml file", e);
        }
    }

    // Get property from YAML
    public static String getProperty(String key) {
        String[] keys = key.split("\\.");
        Object value = config;

        for (String k : keys) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(k);
            } else {
                return null;
            }
        }
        return value != null ? value.toString() : null;
    }

    // Get list of platforms from YAML
    public static List<Map<String, String>> getPlatforms() {
        return (List<Map<String, String>>) config.get("platforms");
    }

    // Download image from URL
    public static void downloadImage(String imageUrl) {
        try {
            if (imageUrl == null) {
                throw new NullPointerException("Image Url is Null");
            }
            String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String imageDir = System.getProperty("user.dir") + "\\src\\test\\resources\\images\\" + imageName.substring(0, imageName.indexOf('?'));
            URL url = new URL(imageUrl);
            try (BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(imageDir)) {
                // Reading Image From Connection
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                // Saving Image
                while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                System.out.println("Image downloaded successfully: " + imageDir);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } catch (MalformedURLException | NullPointerException e) {
            Assert.fail(e.getMessage());
        }
    }

    // Translate text using API
    public static String translateText(String text) {
        if (text == null || text.isEmpty()) return "";

        Response response = given()
                .header("Content-Type", "application/json")
                .header("x-rapidapi-key", getProperty("translate.api.key"))
                .header("x-rapidapi-host", getProperty("translate.api.host"))
                .body("{\"from\":\"auto\", \"to\":\"en\", \"text\":\"" + text + "\"}")
                .post(getProperty("translate.api.url"));

        return response.getStatusCode() == 200
                ? response.jsonPath().getString("trans")
                : "Translation failed!";
    }

    // Analyze the most common words in a text
    public static void analyzeWords(List<String> titles) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String title : titles) {
            for (String word : title.toLowerCase().replaceAll("[.,!?]", "").split(" ")) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        System.out.println("\nRepeated Words:");
        wordCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2) // Only words appearing more than twice
                .forEach(entry -> System.out.println("\t" + entry.getKey() + ": " + entry.getValue()));
    }


}
