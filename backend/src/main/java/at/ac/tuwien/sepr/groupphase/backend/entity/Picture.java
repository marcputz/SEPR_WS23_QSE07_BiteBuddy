package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.util.Arrays;
import java.util.Objects;

@Entity
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    private String description;

    public Long getId() {
        return id;
    }

    public Picture setId(Long id) {
        this.id = id;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public Picture setData(byte[] data) {
        this.data = data;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Picture setDescription(String description) {
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
        Picture picture = (Picture) o;
        return Objects.equals(id, picture.id) && Arrays.equals(data, picture.data) && Objects.equals(description, picture.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, description);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
