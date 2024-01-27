package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PictureDto {

    @NotNull
    private long id;

    @NotNull
    private byte[] data;

    private String description;

    public long getId() {
        return id;
    }

    public PictureDto setId(long id) {
        this.id = id;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public PictureDto setData(byte[] data) {
        this.data = data;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PictureDto setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PictureDto that = (PictureDto) o;
        return id == that.id && Arrays.equals(data, that.data) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, description);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
