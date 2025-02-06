package bdd.mongo.fileupload.repository;

import bdd.mongo.fileupload.entity.MyFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyFileRepository extends MongoRepository<MyFile, String> {
    @Query("{ 'content.?key' : { $exists: true } }") // The CRUCIAL custom query
    MyFile findByFilename(String filename);
    List<MyFile> findAll();
}