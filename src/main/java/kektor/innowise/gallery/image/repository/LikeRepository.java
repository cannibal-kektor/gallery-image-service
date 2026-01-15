package kektor.innowise.gallery.image.repository;

import kektor.innowise.gallery.image.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findIdByImageIdAndUserId(Long imageId, Long userId);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.id = :likeId")
    int deleteLikeById(@Param("likeId") Long id);

    @Query("select i.image.id from Like i where i.userId=:userId and i.image.id in :imageIds")
    Set<Long> findLikedImagesIdByUserFromSpecific(@Param("userId") Long userId, @Param("imageIds") List<Long> imageIds);

    boolean existsByImageIdAndUserId(Long imageId, Long userId);
}
