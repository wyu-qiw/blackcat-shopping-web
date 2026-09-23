package com.shop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 本地开发辅助：
 * 在 IDEA 中携带 --app.dev.local-start=true 启动后端时，自动启动 Vite 前端，
 * 并在前端端口就绪后强制打开浏览器。
 */
@Component
@ConditionalOnProperty(name = "app.dev.local-start", havingValue = "true")
public class DevFrontendLauncher {

    private static final Logger log = LoggerFactory.getLogger(DevFrontendLauncher.class);
    private static final int FRONTEND_PORT = 5173;
    private static final String FRONTEND_URL = "http://localhost:" + FRONTEND_PORT;
    private static final long FRONTEND_START_TIMEOUT_MS = 60_000L;

    private Process frontendProcess;

    @EventListener(ApplicationReadyEvent.class)
    public void startFrontend() {
        if (isPortOpen(FRONTEND_PORT)) {
            log.info("检测到前端服务已运行，直接打开浏览器：{}", FRONTEND_URL);
            waitForFrontendThenOpenBrowser();
            return;
        }

        Path frontendDir = locateFrontendDir();

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "npm.cmd",
                    "run",
                    "dev"
            );

            builder.directory(frontendDir.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            frontendProcess = builder.start();
            Runtime.getRuntime().addShutdownHook(new Thread(this::stopFrontend));

            log.info("前端启动命令已执行，等待端口 {} 就绪后自动打开浏览器...", FRONTEND_PORT);
            waitForFrontendThenOpenBrowser();
        } catch (IOException e) {
            throw new IllegalStateException("启动前端失败，请检查 Node.js 和 npm 是否正确安装", e);
        }
    }

    private void waitForFrontendThenOpenBrowser() {
        Thread openerThread = new Thread(() -> {
            long deadline = System.currentTimeMillis() + FRONTEND_START_TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                if (isPortOpen(FRONTEND_PORT)) {
                    openBrowser();
                    return;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            log.warn("前端服务在 {} 毫秒内未启动成功，请手动访问 {}", FRONTEND_START_TIMEOUT_MS, FRONTEND_URL);
        }, "frontend-browser-launcher");

        openerThread.setDaemon(true);
        openerThread.start();
    }

    private void openBrowser() {
        log.info("正在打开前端页面：{}", FRONTEND_URL);

        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                // Windows 下 start 后面的空字符串是窗口标题，不能省略
                Process process = new ProcessBuilder("cmd.exe", "/c", "start", "", FRONTEND_URL).start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return;
                }
                throw new IllegalStateException("Windows start 命令退出码：" + exitCode);
            }

            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(URI.create(FRONTEND_URL));
                return;
            }

            throw new IllegalStateException("当前系统不支持自动打开浏览器");
        } catch (Exception e) {
            log.error("自动打开浏览器失败，请手动访问：{}", FRONTEND_URL, e);
        }
    }

    private Path locateFrontendDir() {
        Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        List<Path> candidates = Arrays.asList(
                currentDir.resolve("frontend"),
                currentDir.resolve("../frontend").normalize()
        );

        return candidates.stream()
                .filter(path -> Files.exists(path.resolve("package.json")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "未找到 frontend 目录，请检查 IDEA 运行配置中的 Working directory：" + currentDir
                ));
    }

    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void stopFrontend() {
        if (frontendProcess == null || !frontendProcess.isAlive()) {
            return;
        }

        try {
            long pid = frontendProcess.pid();
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "taskkill.exe",
                    "/PID",
                    String.valueOf(pid),
                    "/T",
                    "/F"
            ).start().waitFor();
        } catch (Exception ignored) {
            frontendProcess.destroy();
        }
    }
}

