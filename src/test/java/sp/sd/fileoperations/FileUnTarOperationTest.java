package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileUnTarOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testGetters() {
        FileUnTarOperation operation = new FileUnTarOperation("test.tar", "target", true);
        assertEquals("test.tar", operation.getFilePath());
        assertEquals("target", operation.getTargetLocation());
        assertTrue(operation.getIsGZIP());

        // Test with isGZIP false
        FileUnTarOperation operation2 = new FileUnTarOperation("test.tar", "target", false);
        assertFalse(operation2.getIsGZIP());
    }

    @Test
    void testUnTarRegularTarFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-untar-regular");

        // Get workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);

        // Create test content
        String testContent = "test content";
        FilePath sourceFile = workspace.child("source-file.txt");
        sourceFile.write(testContent, "UTF-8");

        // Create tar file
        FilePath tarFile = workspace.child("test.tar");
        try (TarArchiveOutputStream out =
                new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(tarFile.getRemote())))) {
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            // Create entry for the file
            TarArchiveEntry entry = new TarArchiveEntry("source-file.txt");
            entry.setSize(sourceFile.length());
            out.putArchiveEntry(entry);

            // Copy file content to tar
            try (InputStream in = sourceFile.read()) {
                IOUtils.copy(in, out);
            }

            // Close the entry
            out.closeArchiveEntry();
            out.finish();
        }

        // Configure the operation
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnTarOperation("test.tar", "extracted", false));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build success
        assertEquals(Result.SUCCESS, build.getResult());

        // Verify extracted file exists
        FilePath extractedFile = workspace.child("extracted/source-file.txt");
        assertTrue(extractedFile.exists());
        assertEquals(testContent, extractedFile.readToString());
    }

    @Test
    void testUnTarGzipFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-untar-gzip");

        // Get workspace
        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);

        // Create test content
        String testContent = "test gzip content";
        FilePath sourceFile = workspace.child("source-file.txt");
        sourceFile.write(testContent, "UTF-8");

        // Create tar.gz file
        FilePath tarGzFile = workspace.child("test.tar.gz");
        try (TarArchiveOutputStream out = new TarArchiveOutputStream(new GzipCompressorOutputStream(
                new BufferedOutputStream(new FileOutputStream(tarGzFile.getRemote()))))) {
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            // Create entry for the file
            TarArchiveEntry entry = new TarArchiveEntry("source-file.txt");
            entry.setSize(sourceFile.length());
            out.putArchiveEntry(entry);

            // Copy file content to tar
            try (InputStream in = sourceFile.read()) {
                IOUtils.copy(in, out);
            }

            // Close the entry
            out.closeArchiveEntry();
            out.finish();
        }

        // Configure the operation
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnTarOperation("test.tar.gz", "extracted-gz", true));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build success
        assertEquals(Result.SUCCESS, build.getResult());

        // Verify extracted file exists
        FilePath extractedFile = workspace.child("extracted-gz/source-file.txt");
        assertTrue(extractedFile.exists());
        assertEquals(testContent, extractedFile.readToString());
    }

    @Test
    void testUnTarNonExistentFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-untar-nonexistent");

        // Configure the operation with non-existent source file
        List<FileOperation> operations = new ArrayList<>();
        operations.add(new FileUnTarOperation("nonexistent.tar", "extracted", false));

        // Add the operation to the project
        project.getBuildersList().add(new FileOperationsBuilder(operations));

        // Run the build
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Assert build failure due to missing file
        assertEquals(Result.FAILURE, build.getResult());
    }
}
