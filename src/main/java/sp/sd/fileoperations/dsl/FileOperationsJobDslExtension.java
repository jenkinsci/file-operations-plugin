package sp.sd.fileoperations.dsl;

/**
 * Created by suresh on 10/3/2016.
 */

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import sp.sd.fileoperations.FileOperationsBuilder;

/*
 ```
 job {
    steps {
        fileOperations {
            fileCreateOperation(String fileName, String fileContent)
            fileCopyOperation(String includes, String excludes, String targetLocation, boolean flattenFiles)
            fileDeleteOperation(String includes, String excludes)
            fileDownloadOperation(String url, String userName, String password, String targetLocation, String targetFileName)
            fileJoinOperation(String sourceFile, String targetFile)
            filePropertiesToJsonOperation(String sourceFile, String targetFile)
            fileTransformOperation(String includes, String excludes)
            fileUnTarOperation(String filePath, String targetLocation, boolean isGZIP)
            fileUnZipOperation(String filePath, String targetLocation)
            folderCopyOperation(String sourceFolderPath, String destinationFolderPath)
            folderCreateOperation(String folderPath)
            folderDeleteOperation(String folderPath)
        }
    }
 }
 ```
 For example:
 ```
    freeStyleJob('FileOperationsJob') {
        steps {
          fileOperations {
            fileCreateOperation('testdsl.txt','test content')
            fileCopyOperation('testdsl.txt','','.',false)
            fileDownloadOperation('http://192.168.56.1:8081/service/local/repositories/MyWorks/content/sp/sd/test-artifact/40/test-artifact-40-debug.zip','','','.','test.zip')
            fileDeleteOperation('testdsl.txt','')
            fileDeleteOperation('test.zip','')
            fileJoinOperation('testsource.txt','testtarget.txt')
            filePropertiesToJsonOperation('testsource.properties','testtarget.json')
            fileTransformOperation('testsource.txt','')
            fileUnTarOperation('package.tar','.',false)
            fileUnZipOperation('package.zip','.')
            folderCopyOperation('sourcefolder','destinationfolder')
            folderCreateOperation('newfolder')
            folderDeleteOperation('targetfolder')
          }
        }
    }
 ```
*/

@Extension(optional = true)
public class FileOperationsJobDslExtension extends ContextExtensionPoint {
    @RequiresPlugin(id = "file-operations", minimumVersion = "1.2")
    @DslExtensionMethod(context = StepContext.class)
    public Object fileOperations(Runnable closure) {
        FileOperationsJobDslContext context = new FileOperationsJobDslContext();
        executeInContext(closure, context);
        return new FileOperationsBuilder(context.fileOperations);
    }
}
