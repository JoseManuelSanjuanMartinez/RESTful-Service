package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class MortgageAttachment {
    @Id
    private Integer id;
    private Integer mortgageId;
    private String filename;
    private String contentType;
    private long size;
    private String path;

    public MortgageAttachment() {
    }

    public MortgageAttachment(Integer id, Integer mortgageId, String filename, String contentType, long size, String path) {
        this.id = id;
        this.mortgageId = mortgageId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public Integer getMortgageId() {
        return mortgageId;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MortgageAttachment that = (MortgageAttachment) o;
        return Objects.equals(id, that.id) && Objects.equals(mortgageId, that.mortgageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mortgageId);
    }
}
