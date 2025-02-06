package bdd.mongo.fileupload.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;

@Document("files")
public class MyFile {

    @Id
    private String id;
    private Date currentdate;
    private Map<String, Map<String, byte[]>> content;

    public MyFile() {
        // Required for Spring Data MongoDB
        this.currentdate = new Date(); // Initialize current date on creation.
        this.content = new HashMap<>();
    }

    public MyFile(Date currentdate, Map<String, Map<String, byte[]>> content) {
        this.currentdate = currentdate;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCurrentdate() {
        return currentdate;
    }

    public void setCurrentdate(Date currentdate) {
        this.currentdate = currentdate;
    }

    public Map<String, Map<String, byte[]>> getContent() {
        return content;
    }

    public void setContent(Map<String, Map<String, byte[]>> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyFile myFile = (MyFile) o;
        return Objects.equals(id, myFile.id) && Objects.equals(currentdate, myFile.currentdate) && Objects.equals(content, myFile.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, currentdate, content);
    }

    @Override
    public String toString() {
        return "MyFile{" +
                "id='" + id + '\'' +
                ", currentdate=" + currentdate +
                ", content=" + (content != null ? content.keySet() : null) + // Print keys for brevity
                '}';
    }
}