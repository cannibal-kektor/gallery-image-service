package kektor.innowise.gallery.image.repository;

import kektor.innowise.gallery.image.model.Image;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>, UtilityRepositoryFragment {

    Window<Image> findAllFilteredBy(ScrollPosition scrollPosition, Sort sort, Limit limit);

    Window<Image> findAllFilteredByUploadedAtAfter(Instant till, ScrollPosition scrollPosition, Sort sort, Limit limit);

    Window<Image> findAllFilteredByUserId(Long userId, ScrollPosition scrollPosition, Sort sort, Limit limit);

    Window<Image> findAllFilteredByUserIdAndUploadedAtAfter(Long userId, Instant till, ScrollPosition scrollPosition, Sort sort, Limit limit);

    @Modifying
    @Query("UPDATE Image i SET i.likesCount = i.likesCount + 1 WHERE i.id = :imageId")
    void incrementLikes(@Param("imageId") Long imageId);

    @Modifying
    @Query("UPDATE Image i SET i.likesCount = i.likesCount - 1 WHERE i.id = :imageId")
    void decrementLikes(@Param("imageId") Long imageId);

}
