package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

public class FileDeleteOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileDeleteOperation fdo = new FileDeleteOperation("**/*.txt", "**/*.xml");
        assertEquals("**/*.txt", fdo.getIncludes());
        assertEquals("**/*.xml", fdo.getExcludes());
        assertEquals(true, fdo.getUseDefaultExcludes());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStep() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation("NewFileName.txt", "This is File Content");
        FileDeleteOperation fdo = new FileDeleteOperation("**/*.txt", "**/*.xml");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(fco);
        fop.add(fdo);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertFalse(build.getWorkspace().child("NewFileName.txt").exists());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStepWithTokens() throws Exception {
        // Given
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TextFileName", "NewFileName.txt");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FileCreateOperation fco = new FileCreateOperation("$TextFileName", "This is File Content");
        FileDeleteOperation fdo = new FileDeleteOperation("**/*.txt", "**/*.xml");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(fco);
        fop.add(fdo);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertFalse(build.getWorkspace().child("NewFileName.txt").exists());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStepWithDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation(".gitignore", "This is File Content");
        FileDeleteOperation fdo = new FileDeleteOperation(".gitignore", "");
        List<FileOperation> fop = Arrays.asList(fco, fdo);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child(".gitignore").exists());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStepWithoutDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fco = new FileCreateOperation(".gitignore", "This is File Content");
        FileDeleteOperation fdo = new FileDeleteOperation(".gitignore", "");
        fdo.setUseDefaultExcludes(false);
        List<FileOperation> fop = Arrays.asList(fco, fdo);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertFalse(build.getWorkspace().child(".gitignore").exists());
    }

    @Test
    @WithoutJenkins
    public void testSerializeWithXStream() {
        // Given
        FileDeleteOperation originalObject = new FileDeleteOperation("include", "exclude");
        originalObject.setUseDefaultExcludes(false);

        // When
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        String serializedObjectXml = xstream.toXML(originalObject);
        FileDeleteOperation deserializedObject = (FileDeleteOperation)xstream.fromXML(serializedObjectXml);

        // Then
        assertEquals(originalObject.getIncludes(), deserializedObject.getIncludes());
        assertEquals(originalObject.getExcludes(), deserializedObject.getExcludes());
        assertEquals(originalObject.getUseDefaultExcludes(), deserializedObject.getUseDefaultExcludes());
    }

    @Test
    @WithoutJenkins
    public void testSerializeWithXStreamBackwardsCompatibility() {
        // Given
        String serializedObjectXml = "<FileDeleteOperation><includes>include</includes><excludes>exclude</excludes></FileDeleteOperation>";

        // When
        XStream xstream = new XStream();
        xstream.alias("FileDeleteOperation", FileDeleteOperation.class);
        xstream.addPermission(AnyTypePermission.ANY);
        FileDeleteOperation deserializedObject = (FileDeleteOperation)xstream.fromXML(serializedObjectXml);

        // Then
        assertTrue(deserializedObject.getUseDefaultExcludes());
    }
}
