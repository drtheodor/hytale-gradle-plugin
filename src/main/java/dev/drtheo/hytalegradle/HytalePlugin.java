package dev.drtheo.hytalegradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;

public class HytalePlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        HytalePluginExtension extension = project.getExtensions()
            .create("hytale", HytalePluginExtension.class, project);

        Configuration hytaleConfiguration = createHytaleConfiguration(project);

        populateHytaleConfiguration(project, hytaleConfiguration, extension);
        wireConfigurationToImplementation(project, hytaleConfiguration);

        registerTasks(project, extension);
    }

    private Configuration createHytaleConfiguration(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();

        // Create the configuration that users can reference
        Configuration hytaleConfig = configurations.create("hytaleServer");
        hytaleConfig.setDescription("Hytale Server dependency");
        hytaleConfig.setVisible(true);
        hytaleConfig.setCanBeResolved(false); // Not meant to be resolved directly
        hytaleConfig.setCanBeConsumed(false); // Not meant to be consumed by other projects

        return hytaleConfig;
    }

    private void populateHytaleConfiguration(Project project, Configuration hytaleConfig, HytalePluginExtension extension) {
        // Populate the configuration with the actual file dependency
        project.getDependencies().add(hytaleConfig.getName(),
                project.files(
                        extension.getHytaleDir()
                                .file("HytaleServer.jar")
                )
        );

        // Also ensure the setup task runs before configuration is accessed
        project.getTasks().named("setupHytaleServer", task -> {
            hytaleConfig.getDependencies().all(dep -> {
                task.dependsOn("setupHytaleServer");
            });
        });
    }

    private void wireConfigurationToImplementation(Project project, Configuration hytaleConfig) {
        // Wire hytale configuration to implementation
        project.afterEvaluate(p -> {
            Configuration implementation = p.getConfigurations().getByName("implementation");
            implementation.extendsFrom(hytaleConfig);
        });
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