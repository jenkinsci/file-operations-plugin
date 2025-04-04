package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilePropertiesToJsonOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testGetters() {
        FilePropertiesToJsonOperation operation = new FilePropertiesToJsonOperation("source.properties", "target.json");
        assertEquals("source.properties", operation.getSourceFile());
        assertEquals("target.json", operation.getTargetFile());
    }

    @Test
    public void testRunOperationWithValidFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-properties-to-json");
        
        // Get the workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        
        // Create a properties file in the workspace
        FilePath propsFile = workspace.child("test.properties");
        
        // Create properties content
        Properties props = new Properties();
        props.setProperty("stringProp", "value");
        props.setProperty("intProp", "123");
        props.setProperty("floatProp", "123.45");
        props.setProperty("boolProp", "true");
        
        // Save properties to file using FilePath API
        try (OutputStream os = propsFile.write()) {
            props.store(os, "Test Properties");
        }
        
        // Make sure the target file exists first
        FilePath targetFile = workspace.child("output.json");
        targetFile.write("{}", "UTF-8");
        
        // Configure the operation
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FilePropertiesToJsonOperation("test.properties", "output.json"));
        
        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));
        
        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        
        // Assert build success
        assertEquals(Result.SUCCESS, build.getResult());
        
        // Verify JSON file exists and contains correct content
        FilePath jsonFile = workspace.child("output.json");
        assertTrue(jsonFile.exists());
        
        // Parse and verify JSON content
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonFile.read());
        assertEquals("value", jsonNode.get("stringProp").asText());
        assertEquals(123, jsonNode.get("intProp").asInt());
        assertEquals(123.45f, jsonNode.get("floatProp").floatValue(), 0.001);
        assertEquals(true, jsonNode.get("boolProp").asBoolean());
    }

    @Test
    public void testRunOperationWithNonExistentFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-properties-nonexistent");
        
        // Get the workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        
        // Create target file with content
        FilePath targetFile = workspace.child("output.json");
        targetFile.write("{\"test\":\"original\"}", "UTF-8");
        
        // Configure the operation with non-existent source file
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FilePropertiesToJsonOperation("nonexistent.properties", "output.json"));
        
        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));
        
        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        
        // Assert build failure due to missing file
        assertEquals(Result.FAILURE, build.getResult());
    }
} 