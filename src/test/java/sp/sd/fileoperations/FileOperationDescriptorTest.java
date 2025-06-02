package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.WithoutJenkins;

class FileOperationDescriptorTest {

    @Test
    @WithoutJenkins
    void testFileOperationDescriptor() {
        // Test with a real implementation that exists in the codebase
        FileOperationDescriptor descriptor = new FileZipOperation.DescriptorImpl();
        assertEquals("File Zip", descriptor.getDisplayName());
        
        // Test another implementation
        FileOperationDescriptor descriptor2 = new FileUnZipOperation.DescriptorImpl();
        assertEquals("Unzip", descriptor2.getDisplayName());
    }
} 