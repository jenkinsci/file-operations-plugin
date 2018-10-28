File Operations Plugin
=====================
This plugin's main goal is to provide File Operations as Build Step.

At present following Operations implemented:
---
  1) File Copy Operation
  
  2) File Delete Operation
  
  3) File Create Operation
  
  4) File Download Operation
  
  5) File Transform Operation
  
  6) File UnTar Operation
  
  7) File UnZip Operation
  
  8) Folder Create Operation
  
  9) Folder Copy Operation
  
  10) Folder Delete Operation
  
  11) File Join
  
  12) File Properties To Json
  
  13) File Rename Operation
  
  14) Folder Rename Operation

This is will be helpful for doing cross platform file operations.
No need to search for Nix or Win commands to do file operations.

Job DSL Details:
----------------
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
            fileRenameOperation(String source, String destination)
            folderRenameOperation(String source, String destination)
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
            fileRenameOperation('test.txt', 'testrename.txt')
            folderRenameOperation('test', 'testrename')
          }
        }
    }
 ```
