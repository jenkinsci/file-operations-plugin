package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileCopyOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileCopyOperation fco = new FileCopyOperation("**/*.config", "**/*.xml", "target", true, false, null, null);
        assertEquals("**/*.config", fco.getIncludes());
        assertEquals("**/*.xml", fco.getExcludes());
        assertEquals("target", fco.getTargetLocation());
        assertTrue(fco.getFlattenFiles());
        assertFalse(fco.getRenameFiles());
        assertNull(fco.getSourceCaptureExpression());
        assertNull(fco.getTargetNameExpression());
        assertTrue(fco.getUseDefaultExcludes());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepNoFlatten() throws Exception {
        // Given
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));
        fop.add(new FileCopyOperation("test-results-xml/**/*.xml", "", "test-results", false, false, null, null));

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-0/classA/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-0/classB/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-1/classB/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-1/classC/TestC.xml").exists());
    }


    @Test
    void testRunFileOperationWithFileOperationBuildStepWithFlatten() throws Exception {
        // Given
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));
        fop.add(new FileCopyOperation("test-results-xml/**/*.xml", "", "test-results", true, false, null, null));

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestC.xml").exists());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithFlattenAndRename() throws Exception {
        // Required to handle test being run on either Windows or Unix systems
        String dirSep = "(?:\\\\|/)";

        // Given
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));
        fop.add(new FileCopyOperation(
                "test-results-xml/**/*.xml",
                "",
                "test-results",
                true,
                true,
                ".*" + dirSep + "test-results-xml" + dirSep + ".*-([\\d]+)" +
                        dirSep + ".*" + dirSep + "([^" + dirSep + "]+)$",
                "$1-$2"));

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/0-TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/0-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestC.xml").exists());
    }

    /**
     * @see "help-sourceCaptureExpression.html"
     */
    @Test
    void testFileCopyOperationForSourceCaptureExpressionExample() throws Exception {
        // Required to handle test being run on either Windows or Unix systems
        String dirSep = "(?:\\\\|/)";

        // Given
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("dir1/info-app.txt", ""));
        fop.add(new FileCreateOperation("dir1/error-app.txt", ""));
        fop.add(new FileCopyOperation(
                "**/dir1/*.txt",
                "",
                "logs",
                true,
                true,
                "dir1" + dirSep + "(.*)-app\\.txt$",
                "$1.log"));

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("logs/info.log").exists());
        assertTrue(build.getWorkspace().child("logs/error.log").exists());
    }

    /**
     * Files will not be flatten because includes path is not included in sourceCaptureExpression.
     *
     * @see "https://github.com/jenkinsci/file-operations-plugin/issues/101"
     */
    @Test
    void testFileCopyOperationWithFlattenAndRenameFileWithoutMatchingRegex() throws Exception {
        // Given
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/Test-rename-A.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCopyOperation(
                "test-results-xml/**/*.xml",
                "",
                "test-results",
                true,
                true,
                ".*Test-rename-(.*)\\.xml$",
                "Test$1.log"));

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestA.log").exists());
        assertTrue(build.getWorkspace().child("test-results/TestB.xml").exists());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fileCreateOperation = new FileCreateOperation(".gitignore", "");
        FileCopyOperation fileCopyOperation = new FileCopyOperation(".gitignore", "", "output", false, false, null, null);
        List<FileOperation> fop = Arrays.asList(fileCreateOperation, fileCopyOperation);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child(".gitignore").exists());
        assertFalse(build.getWorkspace().child("output/.gitignore").exists());
    }

    @Test
    void testRunFileOperationWithFileOperationBuildStepWithoutDefaultExcludes() throws Exception {
        // Given
        FileCreateOperation fileCreateOperation = new FileCreateOperation(".gitignore", "");
        FileCopyOperation fileCopyOperation = new FileCopyOperation(".gitignore", "", "output", false, false, null, null);
        fileCopyOperation.setUseDefaultExcludes(false);
        List<FileOperation> fop = Arrays.asList(fileCreateOperation, fileCopyOperation);

        // When
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        // Then
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child(".gitignore").exists());
        assertTrue(build.getWorkspace().child("output/.gitignore").exists());
    }

    @Test
    @WithoutJenkins
    void testSerializeWithXStream() {
        // Given
        FileCopyOperation originalObject = new FileCopyOperation("include", "exclude", "output", false, false, null, null);
        originalObject.setUseDefaultExcludes(false);

        // When
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        String serializedObjectXml = xstream.toXML(originalObject);
        FileCopyOperation deserializedObject = (FileCopyOperation)xstream.fromXML(serializedObjectXml);

        // Then
        assertEquals(originalObject.getIncludes(), deserializedObject.getIncludes());
        assertEquals(originalObject.getExcludes(), deserializedObject.getExcludes());
        assertEquals(originalObject.getTargetLocation(), deserializedObject.getTargetLocation());
        assertEquals(originalObject.getFlattenFiles(), deserializedObject.getFlattenFiles());
        assertEquals(originalObject.getRenameFiles(), deserializedObject.getRenameFiles());
        assertEquals(originalObject.getSourceCaptureExpression(), deserializedObject.getSourceCaptureExpression());
        assertEquals(originalObject.getTargetNameExpression(), deserializedObject.getTargetNameExpression());
        assertEquals(originalObject.getUseDefaultExcludes(), deserializedObject.getUseDefaultExcludes());
    }

    @Test
    @WithoutJenkins
    void testSerializeWithXStreamBackwardsCompatibility() {
        // Given
        String serializedObjectXml =
                "<FileCopyOperation>" +
                "  <includes>include</includes>" +
                "  <excludes>exclude</excludes>" +
                "  <targetLocation>output</targetLocation>" +
                "  <flattenFiles>false</flattenFiles>" +
                "  <renameFiles>false</renameFiles>" +
                "</FileCopyOperation>";

        // When
        XStream xstream = new XStream();
        xstream.alias("FileCopyOperation", FileCopyOperation.class);
        xstream.addPermission(AnyTypePermission.ANY);
        FileCopyOperation deserializedObject = (FileCopyOperation)xstream.fromXML(serializedObjectXml);

        // Then
        assertTrue(deserializedObject.getUseDefaultExcludes());
    }
}
