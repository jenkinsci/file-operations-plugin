package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;

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
