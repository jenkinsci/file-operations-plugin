package sp.sd.fileoperations.dsl;

import javaposse.jobdsl.dsl.Context;
import sp.sd.fileoperations.FileCopyOperation;
import sp.sd.fileoperations.FileCreateOperation;
import sp.sd.fileoperations.FileDeleteOperation;
import sp.sd.fileoperations.FileDownloadOperation;
import sp.sd.fileoperations.FileJoinOperation;
import sp.sd.fileoperations.FileOperation;
import sp.sd.fileoperations.FilePropertiesToJsonOperation;
import sp.sd.fileoperations.FileRenameOperation;
import sp.sd.fileoperations.FileTransformOperation;
import sp.sd.fileoperations.FileUnTarOperation;
import sp.sd.fileoperations.FileUnZipOperation;
import sp.sd.fileoperations.FileZipOperation;
import sp.sd.fileoperations.FolderCopyOperation;
import sp.sd.fileoperations.FolderCreateOperation;
import sp.sd.fileoperations.FolderDeleteOperation;
import sp.sd.fileoperations.FolderRenameOperation;

import java.util.ArrayList;
import java.util.List;

public class FileOperationsJobDslContext implements Context {
    final List<FileOperation> fileOperations = new ArrayList<>();

    public void fileCreateOperation(String fileName, String fileContent) {
        FileCreateOperation fileCreateOperation = new FileCreateOperation(fileName, fileContent);
        fileOperations.add(fileCreateOperation);
    }

    public void fileCopyOperation(String includes, String excludes, String targetLocation, boolean flattenFiles) {
        FileCopyOperation fileCopyOperation = new FileCopyOperation(includes, excludes, targetLocation, flattenFiles);
        fileOperations.add(fileCopyOperation);
    }

    public void fileDeleteOperation(String includes, String excludes) {
        FileDeleteOperation fileDeleteOperation = new FileDeleteOperation(includes, excludes);
        fileOperations.add(fileDeleteOperation);
    }

    public void fileDownloadOperation(String url, String userName, String password, String targetLocation, String targetFileName) {
        FileDownloadOperation fileDownloadOperation = new FileDownloadOperation(url, userName, password, targetLocation, targetFileName);
        fileOperations.add(fileDownloadOperation);
    }

    public void fileJoinOperation(String sourceFile, String targetFile) {
        FileJoinOperation fileJoinOperation = new FileJoinOperation(sourceFile, targetFile);
        fileOperations.add(fileJoinOperation);
    }

    public void filePropertiesToJsonOperation(String sourceFile, String targetFile) {
        FilePropertiesToJsonOperation filePropertiesToJsonOperation = new FilePropertiesToJsonOperation(sourceFile, targetFile);
        fileOperations.add(filePropertiesToJsonOperation);
    }

    public void fileTransformOperation(String includes, String excludes) {
        FileTransformOperation fileTransformOperation = new FileTransformOperation(includes, excludes);
        fileOperations.add(fileTransformOperation);
    }

    public void fileUnTarOperation(String filePath, String targetLocation, boolean isGZIP) {
        FileUnTarOperation fileUnTarOperation = new FileUnTarOperation(filePath, targetLocation, isGZIP);
        fileOperations.add(fileUnTarOperation);
    }

    public void fileUnZipOperation(String filePath, String targetLocation) {
        FileUnZipOperation fileUnZipOperation = new FileUnZipOperation(filePath, targetLocation);
        fileOperations.add(fileUnZipOperation);
    }

    public void fileZipOperation(String folderPath) {
        FileZipOperation fileZipOperation = new FileZipOperation(folderPath);
        fileOperations.add(fileZipOperation);
    }

    public void folderCopyOperation(String sourceFolderPath, String destinationFolderPath) {
        FolderCopyOperation folderCopyOperation = new FolderCopyOperation(sourceFolderPath, destinationFolderPath);
        fileOperations.add(folderCopyOperation);
    }

    public void folderCreateOperation(String folderPath) {
        FolderCreateOperation folderCreateOperation = new FolderCreateOperation(folderPath);
        fileOperations.add(folderCreateOperation);
    }

    public void folderDeleteOperation(String folderPath) {
        FolderDeleteOperation folderDeleteOperation = new FolderDeleteOperation(folderPath);
        fileOperations.add(folderDeleteOperation);
    }

    public void fileRenameOperation(String source, String destination) {
        FileRenameOperation fileRenameOperation = new FileRenameOperation(source, destination);
        fileOperations.add(fileRenameOperation);
    }

    public void folderRenameOperation(String source, String destination) {
        FolderRenameOperation folderRenameOperation = new FolderRenameOperation(source, destination);
        fileOperations.add(folderRenameOperation);
    }
}
