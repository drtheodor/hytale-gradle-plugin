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
        configureDependencies(project);
        configureCompileDependencies(project);
    }
    
    private void registerTasks(Project project, HytalePluginExtension extension) {
        // Fixed: Pass downloaderUrl to the task
        project.getTasks().register("setupHytaleServer", SetupHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.setDescription("Ensures HytaleServer.jar and Assets.zip are available");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getProjectDir().set(project.getProjectDir());
            task.getBuildDir().set(project.getLayout().getBuildDirectory());
            task.getDownloaderUrl().set(extension.getDownloaderUrl()); // Fixed
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
    
    private void configureDependencies(Project project) {
        project.getDependencies().getExtensions().getExtraProperties()
            .set("hytale", new HytaleDependency(project));
    }
    
    private void configureCompileDependencies(Project project) {
        project.getTasks().named("compileJava", task -> {
            task.dependsOn("setupHytaleServer");
        });
    }
    
    private static class HytaleDependency {
        private final Project project;
        
        HytaleDependency(Project project) {
            this.project = project;
        }
        
        @SuppressWarnings("unused")
        public Object call(String version) {
            return project.files(project.getLayout().getBuildDirectory()
                .file("hytale/HytaleServer.jar"));
        }
        
        @SuppressWarnings("unused")
        public Object call() {
            return call("0.0.0");
        }
    }
}