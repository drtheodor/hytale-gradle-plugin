package dev.drtheo.hytalegradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;

public abstract class SetupHytaleServerTask extends DefaultTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    @InputDirectory
    @Optional
    public abstract DirectoryProperty getProjectDir();
    
    @OutputDirectory
    public abstract DirectoryProperty getHytaleDir();
    
    @Internal
    public abstract DirectoryProperty getBuildDir();

    @Input
    public abstract Property<String> getDownloaderUrl();
    
    @TaskAction
    public void setup() {
        File hytaleDir = getHytaleDir().get().getAsFile();
        File tmpDir = getBuildDir().get().dir("tmp").getAsFile();
        
        File hytaleJar = new File(hytaleDir, "HytaleServer.jar");
        File assetsZip = new File(hytaleDir, "Assets.zip");
        
        if (!hytaleJar.exists() || !assetsZip.exists()) {
            File parentJar = new File(getProjectDir().get().getAsFile().getParentFile(), "HytaleServer.jar");
            File parentAssets = new File(getProjectDir().get().getAsFile().getParentFile(), "Assets.zip");
            
            if (parentJar.exists() && parentAssets.exists()) {
                getLogger().lifecycle("Creating symbolic links from parent directory");
                try {
                    Files.createSymbolicLink(hytaleJar.toPath(), parentJar.toPath().toAbsolutePath());
                    Files.createSymbolicLink(assetsZip.toPath(), parentAssets.toPath().toAbsolutePath());
                    getLogger().lifecycle("Created symbolic links");
                } catch (Exception e) {
                    getLogger().warn("Symbolic links failed, copying files instead: {}", e.getMessage());

                    getProject().copy(copy -> {
                        copy.from(parentJar);
                        copy.into(hytaleDir);
                    });

                    getProject().copy(copy -> {
                        copy.from(parentAssets);
                        copy.into(hytaleDir);
                    });

                    getLogger().lifecycle("Copied files from parent directory");
                }
            } else {
                downloadHytaleServer(hytaleDir, tmpDir);
            }
        } else {
            getLogger().lifecycle("Hytale files already exist in {}", hytaleDir);
        }
    }
    
    private void downloadHytaleServer(File hytaleDir, File tmpDir) {
        HytaleDownloader downloader = new HytaleDownloader(
                getProject(),
                getLogger(),
                getDownloaderUrl().get(),  // Pass the URL from extension
                getExecOperations()
        );
        downloader.download(hytaleDir, tmpDir);
    }
}