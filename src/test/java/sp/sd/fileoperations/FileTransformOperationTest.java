package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileTransformOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileTransformOperation fto = new FileTransformOperation("NewFileName.config", "**/*.xml");
        assertEquals("NewFileName.config", fto.getIncludes());
        assertEquals("**/*.xml", fto.getExcludes());
        assertTrue(fto.getUseDefaultExcludes());
    }

    static final class FileOperationPutEnvironment extends FileOperation {
        public final transient JenkinsRule jenkins;
        public final transient String key;
        public final transient String value;

        public FileOperationPutEnvironment(JenkinsRule jenkins, String key, String value) {
            this.jenkins = jenkins;
            this.key = key;
            this.value = value;
        }

        public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
            assertDoesNotThrow(
                    () -> {
                        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
                        EnvVars envVars = prop.getEnvVars();
                        envVars.put(key, value);
                        jenkins.jenkins.getGlobalNodeProperties().add(prop);
                    },
                    "Unexpected exception during environment put.");
            return true;
        }
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithTokens() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation("TestFile.txt", "$Content");
        FileOperationPutEnvironment fpo = new FileOperationPutEnvironment(jenkins, "Content", "ReplacementContent");
        FileTransformOperation fto = new FileTransformOperation("TestFile.txt", "");
        List<FileOperation> fop = Arrays.asList(fco, fpo, fto);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertEquals(
                "ReplacementContent", build.getWorkspace().child("TestFile.txt").readToString());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation(".gitignore", "$Content");
        FileOperationPutEnvironment fpo = new FileOperationPutEnvironment(jenkins, "Content", "ReplacementContent");
        FileTransformOperation fto = new FileTransformOperation(".gitignore", "");
        List<FileOperation> fop = Arrays.asList(fco, fpo, fto);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child(".gitignore").exists());
        assertEquals("$Content", build.getWorkspace().child(".gitignore").readToString());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithoutDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation(".gitignore", "$Content");
        FileOperationPutEnvironment fpo = new FileOperationPutEnvironment(jenkins, "Content", "ReplacementContent");
        FileTransformOperation fto = new FileTransformOperation(".gitignore", "");
        fto.setUseDefaultExcludes(false);
        List<FileOperation> fop = Arrays.asList(fco, fpo, fto);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertEquals(
                "ReplacementContent", build.getWorkspace().child(".gitignore").readToString());
    }

    @Test
    @WithoutJenkins
    void testSerializeWithXStream() {
        // Given
        FileTransformOperation originalObject = new FileTransformOperation("include", "exclude");
        originalObject.setUseDefaultExcludes(false);

        // When
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        String serializedObjectXml = xstream.toXML(originalObject);
        FileTransformOperation deserializedObject = (FileTransformOperation) xstream.fromXML(serializedObjectXml);

        // Then
        assertEquals(originalObject.getIncludes(), deserializedObject.getIncludes());
        assertEquals(originalObject.getExcludes(), deserializedObject.getExcludes());
        assertEquals(originalObject.getUseDefaultExcludes(), deserializedObject.getUseDefaultExcludes());
    }

    @Test
    @WithoutJenkins
    void testSerializeWithXStreamBackwardsCompatibility() {
        // Given
        String serializedObjectXml =
                "<FileTransformOperation><includes>include</includes><excludes>exclude</excludes></FileTransformOperation>";

        // When
        XStream xstream = new XStream();
        xstream.alias("FileTransformOperation", FileTransformOperation.class);
        xstream.addPermission(AnyTypePermission.ANY);
        FileTransformOperation deserializedObject = (FileTransformOperation) xstream.fromXML(serializedObjectXml);

        // Then
        assertTrue(deserializedObject.getUseDefaultExcludes());
    }
}
