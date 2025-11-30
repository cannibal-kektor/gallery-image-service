CREATE SCHEMA if NOT EXISTS gallery;

SET search_path TO gallery;

CREATE TABLE images
(
    id          bigint PRIMARY KEY,
    user_id     bigint       NOT NULL,
    s3key       VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(1000),
    uploaded_at timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes_count INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE likes
(
    id       bigint PRIMARY KEY,
    image_id bigint NOT NULL,
    user_id  bigint NOT NULL,

    CONSTRAINT fk_like_image FOREIGN KEY (image_id) REFERENCES images (id) ON DELETE CASCADE,
    UNIQUE (image_id, user_id)
);