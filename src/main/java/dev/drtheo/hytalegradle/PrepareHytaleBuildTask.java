package dev.drtheo.hytalegradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.tasks.*;
import javax.inject.Inject;
import java.io.File;

public abstract class PrepareHytaleBuildTask extends DefaultTask {
    
    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();
    
    @InputDirectory
    public abstract DirectoryProperty getModsDir();
    
    @TaskAction
    public void prepare() {
        File modsDir = getModsDir().get().getAsFile();
        
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
        
        // Find the jar task output
        getProject().getTasks().named("jar", jarTask -> {
            jarTask.getOutputs().getFiles().getAsFileTree().visit(fileVisitDetails -> {
                if (fileVisitDetails.getFile().getName().endsWith(".jar")) {
                    File targetJar = new File(modsDir, "dev.jar");
                    
                    getFileSystemOperations().copy(copySpec -> {
                        copySpec.from(fileVisitDetails.getFile());
                        copySpec.into(modsDir);
                        copySpec.rename(oldName -> "dev.jar");
                    });
                    
                    getLogger().lifecycle("Copied {} to {}", 
                        fileVisitDetails.getFile().getName(), 
                        targetJar.getAbsolutePath());
                }
            });
        });
    }
}