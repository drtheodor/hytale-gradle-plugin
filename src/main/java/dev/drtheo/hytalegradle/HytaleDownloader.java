package dev.drtheo.hytalegradle;

import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.logging.Logger;
import org.gradle.process.ExecOperations;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class HytaleDownloader {
    private final Logger logger;
    private final String url;
    private final ExecOperations exec;
    private final FileSystemOperations fileOps;
    private final ArchiveOperations archiveOps;
    private final File buildDir;

    public HytaleDownloader(Logger logger, String url, ExecOperations exec, 
                             FileSystemOperations fileOps, ArchiveOperations archiveOps, File buildDir) {
        this.logger = logger;
        this.url = url;
        this.exec = exec;
        this.fileOps = fileOps;
        this.archiveOps = archiveOps;
        this.buildDir = buildDir;
    }

    public void download(File hytaleDir) {
        String exeName = getDownloaderName();
        File downloaderExe = new File(hytaleDir, exeName);

        if (!downloaderExe.exists()) {
            logger.lifecycle("Downloading Hytale Downloader...");
            File zipFile = new File(buildDir, "tmp/hytale-downloader.zip");
            zipFile.getParentFile().mkdirs();

            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                fileOps.copy(c -> {
                    c.from(archiveOps.zipTree(zipFile));
                    c.into(hytaleDir);
                });
                downloaderExe.setExecutable(true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to download Hytale tools", e);
            }
        }

        exec.exec(spec -> {
            spec.workingDir(hytaleDir);
            spec.commandLine(downloaderExe.getAbsolutePath());
        });
    }

    private String getDownloaderName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "hytale-downloader-windows-amd64.exe";
        if (os.contains("mac")) return "hytale-downloader-darwin-amd64";
        return "hytale-downloader-linux-amd64";
    }
}