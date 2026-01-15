package dev.drtheo.hytalegradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class HytalePlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        HytalePluginExtension extension = project.getExtensions()
            .create("hytale", HytalePluginExtension.class, project);

        registerTasks(project, extension);
    }
    
    private void registerTasks(Project project, HytalePluginExtension extension) {
        project.getTasks().register("setupHytaleServer", SetupHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Ensures HytaleServer.jar and Assets.zip are available");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getProjectDir().set(project.getProjectDir());
            task.getBuildDir().set(project.getLayout().getBuildDirectory());
            task.getDownloaderUrl().set(extension.getDownloaderUrl());
        });

        project.getTasks().register("prepareHytaleBuild", PrepareHytaleBuildTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Prepares mod for Hytale server");
            task.getModsDir().set(extension.getModsDir());

            project.getTasks().named("jar", jarTask -> {
                jarTask.getOutputs().getFiles().forEach(file -> {
                    if (file.getName().endsWith(".jar")) {
                        task.getInputJar().set(file);
                    }
                });
            });

            task.dependsOn("build");
        });
        
        project.getTasks().register("runHytaleServer", RunHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Runs Hytale server with the mod");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getRunDir().set(extension.getRunDir());
            task.dependsOn("prepareHytaleBuild");
        });
    }
}