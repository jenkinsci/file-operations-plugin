# Job DSL usage

### Template

```
 job {
    steps {
        fileOperations {
            fileCreateOperation(String fileName, String fileContent)
            fileCopyOperation(String includes, String excludes, String targetLocation, boolean flattenFiles, boolean renameFiles, String sourceCaptureExpression, String targetNameExpression)
            fileDeleteOperation(String includes, String excludes)
            fileDownloadOperation(String url, String userName, String password, String targetLocation, String targetFileName, String proxyHost, String proxyPort)
            fileJoinOperation(String sourceFile, String targetFile)
            filePropertiesToJsonOperation(String sourceFile, String targetFile)
            fileTransformOperation(String includes, String excludes)
            fileUnTarOperation(String filePath, String targetLocation, boolean isGZIP)
            fileUnZipOperation(String filePath, String targetLocation)
            folderCopyOperation(String sourceFolderPath, String destinationFolderPath)
            folderCreateOperation(String folderPath)
            folderDeleteOperation(String folderPath)
            fileRenameOperation(String source, String destination)
            folderRenameOperation(String source, String destination)
        }
    }
 }
 ```
 
### Example
 
 ```
    freeStyleJob('FileOperationsJob') {
        steps {
          fileOperations {
            fileCreateOperation('testdsl.txt','test content')
            fileCopyOperation('testdsl.txt','','.',false, true, ".*(?:\\\\|/)test-results-xml(?:\\\\|/).*-([\\d]+)(?:\\\\|/).*(?:\\\\|/)([^(?:\\\\|/)]+)$" , "$1-$2")
            fileDownloadOperation('http://192.168.56.1:8081/service/local/repositories/MyWorks/content/sp/sd/test-artifact/40/test-artifact-40-debug.zip','','','.','test.zip', 'proxy', '3128')
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
            fileRenameOperation('test.txt', 'testrename.txt')
            folderRenameOperation('test', 'testrename')
          }
        }
    }
 ```