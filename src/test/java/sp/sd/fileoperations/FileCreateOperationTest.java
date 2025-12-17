package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileCreateOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileCreateOperation fco = new FileCreateOperation("NewFileName.txt", "This is File Content");
        assertEquals("NewFileName.txt", fco.getFileName());
        assertEquals("This is File Content", fco.getFileContent());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStep() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        FileCreateOperation fco = new FileCreateOperation("NewFileName.txt", "This is File Content");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(fco);
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("NewFileName.txt").exists());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TextFileName", "NewFileName.txt");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        FileCreateOperation fco = new FileCreateOperation("$TextFileName", "This is File Content");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(fco);
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("NewFileName.txt").exists());
    }
}
