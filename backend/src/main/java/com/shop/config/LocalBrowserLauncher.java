package com.shop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨电脑浏览器启动工具。
 * 优先直接调用本机浏览器程序，避免 Windows 中 cmd start 返回成功但窗口没有前台显示。
 */
public final class LocalBrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(LocalBrowserLauncher.class);

    private LocalBrowserLauncher() {
    }

    public static boolean launch(String projectUrl) {
        String os = System.getProperty("os.name", "").toLowerCase();

        try {
            if (os.contains("win")) {
                return launchOnWindows(projectUrl);
            }
            if (os.contains("mac")) {
                return executeAndWait("open", projectUrl);
            }
            return executeAndWait("xdg-open", projectUrl);
        } catch (Exception e) {
            log.error("打开本机浏览器失败，请手动访问 {}：{}", projectUrl, e.getMessage(), e);
            return false;
        }
    }

    private static boolean launchOnWindows(String projectUrl) {
        for (String browserPath : findInstalledBrowsers()) {
            try {
                Process process = new ProcessBuilder(
                        browserPath,
                        "--new-window",
                        "--start-maximized",
                        projectUrl
                ).start();
                process.waitFor();
                log.info("已通过本机浏览器打开项目网页：{}", browserPath);
                return true;
            } catch (Exception e) {
                log.warn("浏览器启动失败，继续尝试下一个：{}，原因：{}", browserPath, e.getMessage());
            }
        }

        try {
            Process process = new ProcessBuilder(
                    "rundll32.exe",
                    "url.dll,FileProtocolHandler",
                    projectUrl,
                    ",1"
            ).start();
            if (process.waitFor() == 0) {
                return true;
            }
        } catch (Exception e) {
            log.warn("rundll32 打开网页失败：{}", e.getMessage());
        }

        try {
            return executeAndWait("cmd.exe", "/c", "start", "", "/max", projectUrl);
        } catch (Exception e) {
            log.warn("系统默认命令打开网页失败：{}", e.getMessage());
            return false;
        }
    }

    private static List<String> findInstalledBrowsers() {
        List<String> browsers = new ArrayList<>();
        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String localAppData = System.getenv("LOCALAPPDATA");

        addBrowser(browsers, programFilesX86, "Microsoft\\Edge\\Application\\msedge.exe");
        addBrowser(browsers, programFiles, "Microsoft\\Edge\\Application\\msedge.exe");
        addBrowser(browsers, localAppData, "Microsoft\\Edge\\Application\\msedge.exe");
        addBrowser(browsers, programFiles, "Google\\Chrome\\Application\\chrome.exe");
        addBrowser(browsers, programFilesX86, "Google\\Chrome\\Application\\chrome.exe");
        addBrowser(browsers, localAppData, "Google\\Chrome\\Application\\chrome.exe");

        return browsers;
    }

    private static void addBrowser(List<String> browsers, String baseDir, String relativePath) {
        if (baseDir == null || baseDir.equals("null") || baseDir.isBlank()) {
            return;
        }

        Path path = Paths.get(baseDir, relativePath);
        if (Files.exists(path)) {
            browsers.add(path.toString());
        }
    }

    private static boolean executeAndWait(String... command) throws Exception {
        Process process = new ProcessBuilder(command).start();
        return process.waitFor() == 0;
    }
}
