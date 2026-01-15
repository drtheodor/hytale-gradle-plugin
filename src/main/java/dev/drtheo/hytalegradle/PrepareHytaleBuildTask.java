package dev.drtheo.hytalegradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import javax.inject.Inject;
import java.io.File;

public abstract class PrepareHytaleBuildTask extends DefaultTask {
    
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();
    
    @OutputDirectory
    public abstract DirectoryProperty getModsDir();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getInputJar();
    
    @TaskAction
    public void prepare() {
        File modsDir = getModsDir().get().getAsFile();
        File inputJar = getInputJar().get().getAsFile();
        
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }

        getFileSystemOperations().copy(copySpec -> {
            copySpec.from(inputJar);
            copySpec.into(modsDir);
            copySpec.rename(oldName -> "dev.jar");
        });

        getLogger().lifecycle("Copied {} to {}/dev.jar",
                inputJar.getName(),
                modsDir.getAbsolutePath());
    }
}