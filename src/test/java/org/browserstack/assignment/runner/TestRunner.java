package org.browserstack.assignment.runner;

import org.browserstack.assignment.tests.ELPaisTest;
import org.browserstack.assignment.utils.Utilities;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    @Test
    public static void runnerFunction() throws InterruptedException {
        List<Map<String, String>> platforms = Utilities.getPlatforms();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (Map<String, String> platform : platforms) {
            executor.execute(() -> {
                try {
                    ELPaisTest test = new ELPaisTest();
                    test.setup(
                            platform.get("browser"),
                            platform.getOrDefault("device", ""),
                            platform.get("os_version"),
                            platform.getOrDefault("browser_version", "latest")
                    );
                    test.elPaisTest();
                    test.tearDownTest();
                } catch (Exception e) {
                    System.out.println("Error on platform: " + platform + " - " + e.getMessage());
                }
            }
        );
        }

        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
            executor.shutdownNow();  // Force shutdown if tasks aren't completed within the given time
        }
    }
}
