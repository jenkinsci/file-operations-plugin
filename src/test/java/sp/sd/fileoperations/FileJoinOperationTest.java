package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileJoinOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileJoinOperation fjo = new FileJoinOperation("source.txt", "target.txt");
        assertEquals("source.txt", fjo.getSourceFile());
        assertEquals("target.txt", fjo.getTargetFile());
    }

    @Test
    public void testRunFileJoinOperation() throws Exception {
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
    public void testRunFileJoinOperationWithTokens() throws Exception {
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
