package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileUnZipOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testGetters() {
        FileUnZipOperation operation = new FileUnZipOperation("test.zip", "target");
        assertEquals("test.zip", operation.getFilePath());
        assertEquals("target", operation.getTargetLocation());
    }

    @Test
    void testUnzipFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-unzip");

        // Get workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);

        // Create test content
        String testContent = "test content for zip file";
        String nestedContent = "nested content";

        // Create a zip file directly in the workspace
        FilePath zipFile = workspace.child("test.zip");

        try (OutputStream os = zipFile.write();
                ZipOutputStream zipOut = new ZipOutputStream(os)) {

            // Add a file entry
            ZipEntry entry = new ZipEntry("test-file.txt");
            zipOut.putNextEntry(entry);
            zipOut.write(testContent.getBytes());
            zipOut.closeEntry();

            // Add a directory entry
            ZipEntry dirEntry = new ZipEntry("test-dir/");
            zipOut.putNextEntry(dirEntry);
            zipOut.closeEntry();

            // Add a file in the directory
            ZipEntry nestedEntry = new ZipEntry("test-dir/nested-file.txt");
            zipOut.putNextEntry(nestedEntry);
            zipOut.write(nestedContent.getBytes());
            zipOut.closeEntry();
        }

        // Configure the operation
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnZipOperation("test.zip", "extracted"));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build success
        assertEquals(Result.SUCCESS, build.getResult());

        // Verify extracted files exist
        FilePath extractedDir = workspace.child("extracted");
        assertTrue(extractedDir.exists());

        FilePath extractedFile = extractedDir.child("test-file.txt");
        assertTrue(extractedFile.exists());
        assertEquals(testContent, extractedFile.readToString());

        FilePath extractedNestedDir = extractedDir.child("test-dir");
        assertTrue(extractedNestedDir.exists());

        FilePath extractedNestedFile = extractedNestedDir.child("nested-file.txt");
        assertTrue(extractedNestedFile.exists());
        assertEquals(nestedContent, extractedNestedFile.readToString());
    }

    @Test
    void testUnzipToExistingDirectory() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-unzip-existing-dir");

        // Get workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);

        // Create a pre-existing target directory with a file
        FilePath existingDir = workspace.child("existing-dir");
        existingDir.mkdirs();
        FilePath existingFile = existingDir.child("existing-file.txt");
        existingFile.write("existing content", "UTF-8");

        // Create a zip file in the workspace
        FilePath zipFile = workspace.child("test.zip");
        String newContent = "new content";

        try (OutputStream os = zipFile.write();
                ZipOutputStream zipOut = new ZipOutputStream(os)) {

            ZipEntry entry = new ZipEntry("new-file.txt");
            zipOut.putNextEntry(entry);
            zipOut.write(newContent.getBytes());
            zipOut.closeEntry();
        }

        // Configure the operation
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnZipOperation("test.zip", "existing-dir"));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build success
        assertEquals(Result.SUCCESS, build.getResult());

        // Verify both files exist
        assertTrue(existingFile.exists());
        assertEquals("existing content", existingFile.readToString());

        FilePath newFile = existingDir.child("new-file.txt");
        assertTrue(newFile.exists());
        assertEquals(newContent, newFile.readToString());
    }

    @Test
    void testUnzipNonExistentFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-unzip-nonexistent");

        // Configure the operation with non-existent source file
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnZipOperation("nonexistent.zip", "extracted"));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build failure due to missing file
        assertEquals(Result.FAILURE, build.getResult());
    }
}
