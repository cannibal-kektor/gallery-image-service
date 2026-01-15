package kektor.innowise.gallery.image.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kektor.innowise.gallery.image.exception.ImageNotFoundException;
import kektor.innowise.gallery.image.exception.NonAuthorizedImageAccessException;
import kektor.innowise.gallery.image.model.Image;

public class UtilityRepositoryFragmentImpl implements UtilityRepositoryFragment {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Image findByIdExceptionally(Long imageId) {
        Image image = entityManager.find(Image.class, imageId);
        if (image == null) {
            throw new ImageNotFoundException(imageId);
        }
        return image;
    }

    @Override
    public Image findByIdAuthorized(Long imageId, Long userId) {
        Image image = findByIdExceptionally(imageId);
        if (!userId.equals(image.getUserId())) {
            throw new NonAuthorizedImageAccessException(userId, imageId);
        }
        return image;
    }
}
