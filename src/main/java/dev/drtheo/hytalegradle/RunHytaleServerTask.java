package dev.drtheo.hytalegradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import javax.inject.Inject;
import java.io.File;

public abstract class RunHytaleServerTask extends DefaultTask {
    @Inject protected abstract ExecOperations getExecOperations();

    @InputDirectory public abstract DirectoryProperty getHytaleDir();
    @InputDirectory public abstract DirectoryProperty getRunDir();

    @TaskAction
    public void run() {
        File hytaleDir = getHytaleDir().get().getAsFile();
        File runDir = getRunDir().get().getAsFile();
        
        File serverJar = new File(hytaleDir, "HytaleServer.jar");
        File assetsZip = new File(hytaleDir, "Assets.zip");

        if (!runDir.exists()) runDir.mkdirs();

        getExecOperations().javaexec(spec -> {
            spec.setWorkingDir(runDir);
            spec.setExecutable("java");
            spec.args("-jar", 
                serverJar.getAbsolutePath(), 
                "--assets", assetsZip.getAbsolutePath(), 
                "--disable-sentry"
            );
        });
    }
}