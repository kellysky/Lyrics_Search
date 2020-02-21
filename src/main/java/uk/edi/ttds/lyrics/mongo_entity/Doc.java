package uk.edi.ttds.lyrics.mongo_entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@org.springframework.data.mongodb.core.mapping.Document(collection = "index")
public class Doc {
     @Id
     private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    @Field
     private String document_id;
}
