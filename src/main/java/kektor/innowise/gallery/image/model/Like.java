package kektor.innowise.gallery.image.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "likes", schema = "gallery",
        uniqueConstraints = @UniqueConstraint(columnNames = {"image_id", "user_id"}))
public class Like {

    static final String ID_GENERATOR_LIKES = "ID_GENERATOR_LIKES";
    static final String ID_GENERATOR_SEQUENCE_NAME = "id_likes_sequence_generator";

    @Id
    @GeneratedValue(generator = ID_GENERATOR_LIKES)
    @SequenceGenerator(name = ID_GENERATOR_LIKES,
            sequenceName = ID_GENERATOR_SEQUENCE_NAME,
            schema = "gallery",
            allocationSize = 100,
            initialValue = 5000
    )
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    Image image;

}
