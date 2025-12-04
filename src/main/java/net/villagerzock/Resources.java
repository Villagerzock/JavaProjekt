package net.villagerzock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {
    public static InputStream readFile(String path){
        try {
            InputStream in;
            if (path.startsWith("res://")){
                in = Resources.class.getResourceAsStream(path.substring("res:/".length()));

            }else if (path.startsWith("user://")){
                Path userPath = getAppDataDir("gravitySwitch");
                File userFolder = new File(userPath.toFile(), path.substring("user://".length()));
                if (!userFolder.exists()){
                    userFolder.getParentFile().mkdirs();
                    userFolder.createNewFile();
                }
                in = new FileInputStream(userFolder);
            }else {
                File file = new File(path);
                if (!file.exists()){
                    file.createNewFile();
                }
                in = new FileInputStream(path);
            }
            return in;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getAppDataDir(String appName) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows: %APPDATA%\AppName
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                // Fallback: user.home
                appData = System.getProperty("user.home");
            }
            return Paths.get(appData, appName);

        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/AppName
            String home = System.getProperty("user.home");
            return Paths.get(home, "Library", "Application Support", appName);

        } else {
            // Linux/Unix: $XDG_DATA_HOME oder ~/.local/share/AppName
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome == null || xdgDataHome.isEmpty()) {
                xdgDataHome = Paths.get(System.getProperty("user.home"), ".local", "share").toString();
            }
            return Paths.get(xdgDataHome, appName);
        }
    }
}
