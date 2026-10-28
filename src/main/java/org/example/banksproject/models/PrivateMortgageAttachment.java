package org.example.banksproject.models;

import java.util.Objects;
import java.util.Optional;

public class PrivateMortgageAttachment extends PublicMortgageAttachment{

    private String path;
    private TypeOfAttachment type;

    public PrivateMortgageAttachment() {
        super();
    }

    public PrivateMortgageAttachment(Integer id, Integer mortgageId, String filename, String contentType, long size, String path, Optional<TypeOfAttachment> type) {
        super(id, mortgageId, filename, contentType, size);
        this.path = path;
        this.type = type.orElse(TypeOfAttachment.OTHER);
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrivateMortgageAttachment that = (PrivateMortgageAttachment) o;
        return Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPath());
    }

    public TypeOfAttachment getType() {
        return type;
    }
}
