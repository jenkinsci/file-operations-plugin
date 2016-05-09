package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

public class FileCopyOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileCopyOperation fco = new FileCopyOperation("**/*.config", "**/*.xml", "target", true);
        assertEquals("**/*.config", fco.getIncludes());
        assertEquals("**/*.xml", fco.getExcludes());
        assertEquals("target", fco.getTargetLocation());
        assertEquals(true, fco.getFlattenFiles());
    }
}