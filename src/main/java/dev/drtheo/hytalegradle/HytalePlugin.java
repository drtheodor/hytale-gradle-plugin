package dev.drtheo.hytalegradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

public class HytalePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        HytalePluginExtension extension = project.getExtensions()
                .create("hytale", HytalePluginExtension.class);

        extension.getHytaleDir().convention(project.getLayout().getBuildDirectory().dir("hytale"));
        extension.getRunDir().convention(project.getLayout().getProjectDirectory().dir("run"));
        extension.getModsDir().convention(extension.getRunDir().dir("mods"));

        // 1. Setup Task
        TaskProvider<SetupHytaleServerTask> setupTask = project.getTasks().register("setupHytaleServer", SetupHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getDownloaderUrl().set(extension.getDownloaderUrl());
        });

        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, task -> {
            task.dependsOn(setupTask);
        });

        TaskProvider<Jar> jarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);

        TaskProvider<PrepareHytaleBuildTask> prepareTask = project.getTasks().register("prepareHytaleBuild", PrepareHytaleBuildTask.class, task -> {
            task.setGroup("Hytale");
            task.getModsDir().set(extension.getModsDir());
            task.getInputJar().set(jarTask.flatMap(Jar::getArchiveFile));
        });

        project.getTasks().register("runHytaleServer", RunHytaleServerTask.class, task -> {
            task.setGroup("Hytale");
            task.getHytaleDir().set(extension.getHytaleDir());
            task.getRunDir().set(extension.getRunDir());

            task.dependsOn(setupTask);
            task.dependsOn(prepareTask);
        });
    }
}