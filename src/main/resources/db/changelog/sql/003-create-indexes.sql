SET search_path TO gallery;

CREATE INDEX idx_images_recent ON images (uploaded_at DESC, id DESC);
CREATE INDEX idx_images_likes_count ON images (likes_count DESC, id DESC);
CREATE INDEX idx_images_user_recent ON images (user_id, uploaded_at DESC);
CREATE INDEX idx_images_user_likes_count ON images (user_id, likes_count DESC);

CREATE INDEX idx_likes_image_user ON likes (image_id, user_id);