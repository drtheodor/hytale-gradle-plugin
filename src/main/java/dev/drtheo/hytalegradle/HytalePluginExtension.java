package dev.drtheo.hytalegradle;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;

public class HytalePluginExtension {
    
    private final DirectoryProperty hytaleDir;
    private final DirectoryProperty modsDir;
    private final DirectoryProperty runDir;
    private final Property<String> downloaderUrl;

    private final FileCollection serverJar;

    public HytalePluginExtension(Project project) {
        this.hytaleDir = project.getObjects().directoryProperty();
        this.modsDir = project.getObjects().directoryProperty();
        this.runDir = project.getObjects().directoryProperty();
        this.downloaderUrl = project.getObjects().property(String.class);
        
        // Set defaults
        hytaleDir.set(project.getLayout().getBuildDirectory()
            .dir("hytale"));

        this.serverJar = project.files(hytaleDir.file("HytaleServer.jar"));

        modsDir.set(project.file("run/mods"));
        runDir.set(project.file("run"));
        downloaderUrl.set("https://downloader.hytale.com/hytale-downloader.zip");
    }
    
    public DirectoryProperty getHytaleDir() {
        return hytaleDir;
    }
    
    public DirectoryProperty getModsDir() {
        return modsDir;
    }
    
    public DirectoryProperty getRunDir() {
        return runDir;
    }
    
    public Property<String> getDownloaderUrl() {
        return downloaderUrl;
    }

    public FileCollection server() {
        return serverJar;
    }
}