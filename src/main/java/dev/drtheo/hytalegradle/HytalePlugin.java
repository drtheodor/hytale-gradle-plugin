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
        // Fixed: Pass downloaderUrl to the task
        project.getTasks().register("setupHytaleServer", SetupHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Ensures HytaleServer.jar and Assets.zip are available");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getProjectDir().set(project.getProjectDir());
            task.getBuildDir().set(project.getLayout().getBuildDirectory());
            task.getDownloaderUrl().set(extension.getDownloaderUrl());
        });
        
        // Missing PrepareHytaleBuildTask implementation
        project.getTasks().register("prepareHytaleBuild", PrepareHytaleBuildTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Prepares mod for Hytale server");
            task.getModsDir().set(extension.getModsDir());
            task.dependsOn("jar");
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