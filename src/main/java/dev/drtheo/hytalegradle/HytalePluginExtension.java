package dev.drtheo.hytalegradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import javax.inject.Inject;

public abstract class HytalePluginExtension {

    // Properties are abstract; Gradle injects the implementation
    public abstract DirectoryProperty getHytaleDir();
    public abstract DirectoryProperty getModsDir();
    public abstract DirectoryProperty getRunDir();
    public abstract Property<String> getDownloaderUrl();

    private final ConfigurableFileCollection serverFiles;

    @Inject
    public HytalePluginExtension(ObjectFactory objects) {
        getDownloaderUrl().convention("https://downloader.hytale.com/hytale-downloader.zip");

        this.serverFiles = objects.fileCollection().from(
            getHytaleDir().file("HytaleServer.jar")
        );
    }

    /**
     * Used in build.gradle as: implementation hytale.server()
     */
    public FileCollection server() {
        return serverFiles;
    }
}