package dev.drtheo.hytalegradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;

public abstract class SetupHytaleServerTask extends DefaultTask {
    @Inject protected abstract ExecOperations getExecOperations();
    @Inject protected abstract FileSystemOperations getFileSystemOperations();
    @Inject protected abstract ArchiveOperations getArchiveOperations();
    @Inject protected abstract ProjectLayout getProjectLayout();

    @OutputDirectory public abstract DirectoryProperty getHytaleDir();
    @Input public abstract Property<String> getDownloaderUrl();

    @TaskAction
    public void setup() {
        File hytaleDir = getHytaleDir().get().getAsFile();
        File hytaleJar = new File(hytaleDir, "HytaleServer.jar");
        File assetsZip = new File(hytaleDir, "Assets.zip");

        if (hytaleJar.exists() && assetsZip.exists()) return;

        // Logic for checking parent directory for existing files
        File parentDir = getProjectLayout().getProjectDirectory().getAsFile().getParentFile();
        File parentJar = new File(parentDir, "HytaleServer.jar");
        
        if (parentJar.exists()) {
            linkOrCopy(parentJar, hytaleJar);
            linkOrCopy(new File(parentDir, "Assets.zip"), assetsZip);
        } else {
            downloadServer(hytaleDir);
        }
    }

    private void linkOrCopy(File source, File target) {
        try {
            Files.createSymbolicLink(target.toPath(), source.toPath().toAbsolutePath());
        } catch (Exception e) {
            getFileSystemOperations().copy(c -> {
                c.from(source);
                c.into(target.getParentFile());
            });
        }
    }

    private void downloadServer(File hytaleDir) {
        new HytaleDownloader(
            getLogger(), 
            getDownloaderUrl().get(), 
            getExecOperations(),
            getFileSystemOperations(),
            getArchiveOperations(),
            getProjectLayout().getBuildDirectory().get().getAsFile()
        ).download(hytaleDir);
    }
}