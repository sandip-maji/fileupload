package bdd.mongo.fileupload.controller;

import bdd.mongo.fileupload.entity.MyFile;
import bdd.mongo.fileupload.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;



    @GetMapping("/id/{id}/filenames")
    public ResponseEntity<?> getFilenamesById(@PathVariable String id) {
        MyFile myFile = fileService.getFileById(id);

        if (myFile == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Map<String, byte[]>> allContent = myFile.getContent();

        if (allContent.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<String> filenames = new ArrayList<>();
        for (String encodedFilename : allContent.keySet()) {
            filenames.add(encodedFilename.replace("_DOT_", "."));
        }

        return ResponseEntity.ok(filenames);
    }

    @GetMapping("/id/{id}/{filename}")
    public ResponseEntity<?> getImageByIdAndFilename(@PathVariable String id, @PathVariable String filename) {

        String encodedFilename = filename.replace(".", "_DOT_");

        MyFile myFile = fileService.getFileById(id);

        if (myFile == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Map<String, byte[]>> allContent = myFile.getContent();

        if (allContent.containsKey(encodedFilename)) {
            Map<String, byte[]> fileContentMap = allContent.get(encodedFilename);
            if (!fileContentMap.isEmpty()) {
                String contentType = fileContentMap.keySet().iterator().next();
                byte[] content = fileContentMap.get(contentType);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(content);

            } else {
                return ResponseEntity.notFound().build();
            }

        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/upload/from-path")
    public ResponseEntity<?> uploadFromPath(@RequestParam("path") String path) {

        MyFile myFile = null;
        try {
            Path directoryPath = Paths.get(path);

            if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
                return ResponseEntity.badRequest().body("Invalid path provided.  Must be a directory.");
            }

            File directory = directoryPath.toFile();
            File[] files = directory.listFiles();

            if (files == null || files.length == 0) {
                return ResponseEntity.noContent().build(); // Or a 204 No Content
            }


            Map<String, Map<String, byte[]>> uploadedFiles = new HashMap<>();

            for (File file : files) {
                if (file.isFile()) { // Only process files, not subdirectories
                    try {
                        String fileName = file.getName().replace(".", "_DOT_");
                        uploadedFiles.put(fileName, fileService.saveMultipartFile(file));
                    } catch (IOException e) {
                        // Handle individual file upload failures (log, retry, etc.)
                        System.err.println("Error uploading file " + file.getName() + ": " + e.getMessage());
                        // You might want to return a partial success response here
                    }
                }
            }

            if (uploadedFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No files could be uploaded from given path");
            }else{
                myFile = new MyFile(new Date(), uploadedFiles);
                myFile = fileService.saveFile(myFile);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage()); // More general error handling
        }

        return ResponseEntity.ok(myFile); // Return the results
    }


    @PostMapping("/upload/multiple")
    public Map<String, MyFile> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        Map<String, MyFile> uploadedFiles = new HashMap<>();
        for (MultipartFile file : files) {
            MyFile uploadedFile = fileService.saveMultipartFile(file);
            uploadedFiles.put(file.getOriginalFilename(), uploadedFile);
        }
        return uploadedFiles;
    }

    @PostMapping("/upload")
    public MyFile uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return fileService.saveMultipartFile(file);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        String encodedFilename = filename.replace(".", "_DOT_");
        MyFile myFile = fileService.getFile(encodedFilename);

        if (myFile == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, byte[]> fileContent = myFile.getContent().get(encodedFilename);
        if (fileContent == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = fileContent.keySet().iterator().next();
        byte[] content = fileContent.get(contentType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }

    @GetMapping("/all")
    public List<MyFile> getAllFiles() {
        return fileService.getAllFiles();
    }
}