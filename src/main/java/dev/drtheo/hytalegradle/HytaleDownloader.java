package dev.drtheo.hytalegradle;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.gradle.process.ExecOperations;

public class HytaleDownloader {
    
    private final Project project;
    private final Logger logger;
    private final String downloaderUrl;
    private final ExecOperations exec;
    
    public HytaleDownloader(Project project, Logger logger, String downloaderUrl, ExecOperations exec) {
        this.project = project;
        this.logger = logger;
        this.downloaderUrl = downloaderUrl;
        this.exec = exec;
    }
    
    public void download(File hytaleDir, File tmpDir) {
        String downloaderName = getDownloaderName();
        File downloader = new File(hytaleDir, downloaderName);
        
        if (!downloader.exists()) {
            logger.lifecycle("Downloading hytale-downloader from {}...", downloaderUrl);
            
            File downloaderZip = new File(tmpDir, "hytale-downloader.zip");
            
            try {
                FileUtils.copyURLToFile(new URL(downloaderUrl), downloaderZip);
                project.copy(copy -> {
                    copy.from(project.zipTree(downloaderZip));
                    copy.into(hytaleDir);
                });
                downloaderZip.delete();
                
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    downloader.setExecutable(true, false);
                }
                
                logger.lifecycle("Downloaded and extracted hytale-downloader");
            } catch (IOException e) {
                throw new RuntimeException("Failed to download hytale-downloader from " + downloaderUrl, e);
            }
        }
        
        logger.lifecycle("Running hytale-downloader...");
        exec.exec(exec -> {
            exec.workingDir(hytaleDir);
            exec.commandLine(downloader.getAbsolutePath());
        });
        
        logger.lifecycle("hytale-downloader completed");
    }
    
    private String getDownloaderName() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (os.contains("win")) {
            return "hytale-downloader-windows-amd64.exe";
        } else if (os.contains("mac")) {
            return "hytale-downloader-darwin-amd64";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            if (arch.contains("64")) {
                return "hytale-downloader-linux-amd64";
            } else if (arch.contains("arm") || arch.contains("aarch")) {
                return "hytale-downloader-linux-arm64";
            }
        }
        return "hytale-downloader-linux-amd64";
    }
}