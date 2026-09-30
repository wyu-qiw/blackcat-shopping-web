package com.shop;

import com.shop.config.LocalBrowserLauncher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 线上购物平台启动类。
 */
@SpringBootApplication
@MapperScan("com.shop.mapper")
public class ShopApplication {

    public static void main(String[] args) {
        String port = resolvePort(args);
        String projectUrl = "http://127.0.0.1:" + port + "/";

        if (isBrowserAutoOpenEnabled(args) && isProjectAlreadyRunning(projectUrl)) {
            LocalBrowserLauncher.launch(projectUrl);
            return;
        }

        SpringApplication.run(ShopApplication.class, args);
    }

    private static String resolvePort(String[] args) {
        for (String arg : args) {
            if (arg != null && arg.startsWith("--server.port=")) {
                return arg.substring("--server.port=".length());
            }
        }

        String envPort = System.getenv("SERVER_PORT");
        return envPort == null || envPort.isBlank() ? "8080" : envPort;
    }

    private static boolean isBrowserAutoOpenEnabled(String[] args) {
        for (String arg : args) {
            if ("--app.auto-open-browser=false".equals(arg)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isProjectAlreadyRunning(String projectUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(projectUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.setReadTimeout(1000);

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            } finally {
                connection.disconnect();
            }

            return body.toString().contains("id=\"app\"");
        } catch (Exception ignored) {
            return false;
        }
    }
}
