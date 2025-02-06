package bdd.mongo.fileupload.service; // Your package name

import bdd.mongo.fileupload.entity.MyFile;
import bdd.mongo.fileupload.repository.MyFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileService {

    @Autowired
    private MyFileRepository fileRepository;


    public MyFile getFileById(String id) {
        return fileRepository.findById(id).orElse(null); // Use findById from MongoRepository
    }




    public Map<String, byte[]> saveMultipartFile(File file) throws IOException {
        // 1. Read file content
        byte[] fileContent = Files.readAllBytes(file.toPath());
        // 2. Determine content type (you might need a better way to do this)
        String contentType = Files.probeContentType(file.toPath()); // Or use a library like Apache Tika

        Map<String, byte[]> contentMap = new HashMap<>();
        contentMap.put(contentType, fileContent); // Assuming one content type per file
        return contentMap;
    }

    public byte[] getFileContent(String fileStorageLocation) throws IOException {
        // Logic to read the file content from the specified storage location
        // This depends on where you are storing your files (file system, cloud storage, etc.)
        // Example for file system:
        java.io.File file = new java.io.File(fileStorageLocation); // Assuming fileStorageLocation is a file path.
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            return fis.readAllBytes();
        }
        // Example for cloud storage (AWS S3):
        // return s3Client.getObject(bucketName, fileStorageLocation).getObjectContent().readAllBytes();

    }

    public MyFile saveMultipartFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String encodedFilename = originalFilename.replace(".", "_DOT_"); // Encode the filename

        String contentType = file.getContentType();
        byte[] content = file.getBytes();

        Map<String, byte[]> fileContentMap = new HashMap<>();
        fileContentMap.put(contentType, content);

        Map<String, Map<String, byte[]>> allContentMap = new HashMap<>();
        allContentMap.put(encodedFilename, fileContentMap); // Use encoded filename

        MyFile myFile = new MyFile(new Date(), allContentMap);
        return fileRepository.save(myFile);
    }

    public MyFile saveFile(MyFile myFile) throws IOException {
        return fileRepository.save(myFile);
    }

    public MyFile getFile(String filename) {
        return fileRepository.findByFilename(filename); // Filename will be encoded in the controller
    }

    public List<MyFile> getAllFiles() {
        return fileRepository.findAll();
    }
}