package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FolderRenameOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testDefaults() {
        String sourceFolder = "sourceFolder";
        String destinationFolder = "destinationFolder";
        FolderRenameOperation operation = new FolderRenameOperation(sourceFolder, destinationFolder);

        assertEquals(sourceFolder, operation.getSource());
        assertEquals(destinationFolder, operation.getDestination());
    }

    @Test
    void testRunFolderRenameOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("folderRenameTest");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFolder = new FilePath(workspace, "sourceFolder");
        FilePath destinationFolder = new FilePath(workspace, "destinationFolder");

        sourceFolder.mkdirs();
        FilePath fileInSource = new FilePath(sourceFolder, "file.txt");
        fileInSource.write("Sample content", "UTF-8");

        FolderRenameOperation operation = new FolderRenameOperation("sourceFolder", "destinationFolder");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse(sourceFolder.exists(), "The source folder should have been renamed");
        FilePath renamedFile = new FilePath(destinationFolder, "file.txt");
        assertTrue(renamedFile.exists(), "The file should have been moved to the destination folder");
        assertEquals("Sample content", renamedFile.readToString());
    }

    @Test
    void testRunFolderRenameOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("SOURCE_FOLDER", "sourceFolder");
        envVars.put("DESTINATION_FOLDER", "destinationFolder");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("folderRenameTestWithTokens");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFolder = new FilePath(workspace, "sourceFolder");
        FilePath destinationFolder = new FilePath(workspace, "destinationFolder");

        sourceFolder.mkdirs();
        FilePath fileInSource = new FilePath(sourceFolder, "file.txt");
        fileInSource.write("Sample content", "UTF-8");

        FolderRenameOperation operation = new FolderRenameOperation("$SOURCE_FOLDER", "$DESTINATION_FOLDER");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse(sourceFolder.exists(), "The source folder should have been renamed");
        FilePath renamedFile = new FilePath(destinationFolder, "file.txt");
        assertTrue(renamedFile.exists(), "The file should have been moved to the destination folder");
        assertEquals("Sample content", renamedFile.readToString());
    }
}
