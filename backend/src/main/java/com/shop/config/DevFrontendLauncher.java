package com.shop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 在当前运行项目的电脑上自动打开项目网页。
 * 页面由 Spring Boot 自身托管，因此不依赖 Vite、Node.js 或特定用户的电脑路径。
 */
@Component
@ConditionalOnProperty(name = "app.auto-open-browser", havingValue = "true", matchIfMissing = true)
public class DevFrontendLauncher {

    private static final Logger log = LoggerFactory.getLogger(DevFrontendLauncher.class);

    @org.springframework.beans.factory.annotation.Value("${server.port:8080}")
    private String configuredPort;

    @EventListener(ApplicationReadyEvent.class)
    public void openProjectPageAfterStartup() {
        String projectUrl = resolveProjectUrl();
        Thread launcherThread = new Thread(() -> openBrowserSafely(projectUrl), "local-project-browser-launcher");
        launcherThread.setDaemon(true);
        launcherThread.start();
    }

    private void openBrowserSafely(String projectUrl) {
        if (!waitUntilWebServerReady(projectUrl)) {
            log.warn("未检测到本机网页服务就绪，请手动访问 {}", projectUrl);
            return;
        }

        if (!LocalBrowserLauncher.launch(projectUrl)) {
            log.error("自动打开网页失败，请手动访问 {}", projectUrl);
        }
    }

    private String resolveProjectUrl() {
        return "http://127.0.0.1:" + configuredPort + "/";
    }

    private boolean waitUntilWebServerReady(String projectUrl) {
        for (int i = 0; i < 20; i++) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(projectUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(500);
                connection.setReadTimeout(1000);
                connection.connect();
                connection.disconnect();
                return true;
            } catch (Exception ignored) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

}
