package kektor.innowise.gallery.image.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "images", schema = "gallery")
public class Image {

    static final String ID_GENERATOR_IMAGES = "ID_GENERATOR_IMAGES";
    static final String ID_GENERATOR_IMAGES_SEQUENCE = "id_images_sequence_generator";

    @Id
    @GeneratedValue(generator = ID_GENERATOR_IMAGES)
    @SequenceGenerator(name = ID_GENERATOR_IMAGES,
            sequenceName = ID_GENERATOR_IMAGES_SEQUENCE,
            schema = "gallery",
            allocationSize = 100,
            initialValue = 5000
    )
    Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    Long userId;

    @Column(nullable = false, length = 100, unique = true)
    String s3key;

    @Column(length = 1000)
    String description;

    @Column(name = "uploaded_at", nullable = false)
    Instant uploadedAt = Instant.now();

    @Column(name = "likes_count", nullable = false)
    Integer likesCount = 0;

}