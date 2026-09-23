package com.shop.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 本地开发辅助：
 * 在 IDEA 中携带 --app.dev.local-start=true 启动后端时，自动启动 Vite 前端。
 * Vite 配置了 open=true，因此前端启动后会自动打开浏览器。
 */
@Component
@ConditionalOnProperty(name = "app.dev.local-start", havingValue = "true")
public class DevFrontendLauncher {

    private static final int FRONTEND_PORT = 5173;
    private static final String FRONTEND_URL = "http://localhost:" + FRONTEND_PORT;

    private Process frontendProcess;

    @EventListener(ApplicationReadyEvent.class)
    public void startFrontend() throws Exception {
        if (isPortOpen(FRONTEND_PORT)) {
            openBrowser();
            return;
        }

        Path frontendDir = locateFrontendDir();
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

    private void openBrowser() throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(FRONTEND_URL));
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
