package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileJoinOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileJoinOperation fjo = new FileJoinOperation("source.txt", "target.txt");
        assertEquals("source.txt", fjo.getSourceFile());
        assertEquals("target.txt", fjo.getTargetFile());
    }

    @Test
    void testRunFileJoinOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("fileJoinTest");

        FilePath sourceFile = new FilePath(jenkins.jenkins.getWorkspaceFor(project), "source.txt");
        sourceFile.write("Source File Content", StandardCharsets.UTF_8.name());

        FilePath targetFile = new FilePath(jenkins.jenkins.getWorkspaceFor(project), "target.txt");
        targetFile.write("Target File Content", StandardCharsets.UTF_8.name());

        FileJoinOperation fileJoinOp = new FileJoinOperation("source.txt", "target.txt");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(fileJoinOp)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        String expectedContent = "Target File Content" + System.lineSeparator() + "Source File Content";
        String actualContent = targetFile.readToString();
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void testRunFileJoinOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("SOURCE_FILE", "source.txt");
        envVars.put("TARGET_FILE", "target.txt");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("fileJoinTestWithTokens");

        FilePath sourceFile = new FilePath(jenkins.jenkins.getWorkspaceFor(project), "source.txt");
        sourceFile.write("Source File Content", StandardCharsets.UTF_8.name());

        FilePath targetFile = new FilePath(jenkins.jenkins.getWorkspaceFor(project), "target.txt");
        targetFile.write("Target File Content", StandardCharsets.UTF_8.name());

        FileJoinOperation fileJoinOp = new FileJoinOperation("$SOURCE_FILE", "$TARGET_FILE");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(fileJoinOp)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        String expectedContent = "Target File Content" + System.lineSeparator() + "Source File Content";
        String actualContent = targetFile.readToString();
        assertEquals(expectedContent, actualContent);
    }
}
