package dev.drtheo.hytalegradle;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;

class HytalePluginTest {
    
    @TempDir
    File testProjectDir;
    
    @Test
    void pluginRegistersTasks() {
        // Write build.gradle
        File buildFile = new File(testProjectDir, "build.gradle");
        String buildFileContent = """
            plugins {
                id 'com.hytale.plugin'
            }
        """;
        Files.write(buildFile.toPath(), buildFileContent.getBytes());
        
        // Run gradle tasks
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build();
        
        assertTrue(result.getOutput().contains("setupHytaleServer"));
        assertTrue(result.getOutput().contains("runHytaleServer"));
    }
}