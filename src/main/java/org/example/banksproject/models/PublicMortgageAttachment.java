package org.example.banksproject.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("MORTGAGE_ATTACHMENT")
public class PublicMortgageAttachment {
    @Id
    private Integer id;
    private Integer mortgageId;
    private String filename;
    private String contentType;
    private long size;

    public PublicMortgageAttachment() {
    }

    public PublicMortgageAttachment(Integer id, Integer mortgageId, String filename, String contentType, long size) {
        this.id = id;
        this.mortgageId = mortgageId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PublicMortgageAttachment that = (PublicMortgageAttachment) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMortgageId(), that.getMortgageId()) && Objects.equals(getFilename(), that.getFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMortgageId(), getFilename());
    }
}
