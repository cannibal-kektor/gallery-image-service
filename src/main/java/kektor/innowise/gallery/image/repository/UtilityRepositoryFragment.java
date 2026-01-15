package kektor.innowise.gallery.image.repository;

import kektor.innowise.gallery.image.model.Image;

public interface UtilityRepositoryFragment {

    Image findByIdExceptionally(Long imageId);

    Image findByIdAuthorized(Long imageId, Long userId);

}
