INSERT INTO gallery.images (id, user_id, description, s3key, uploaded_at, likes_count)
VALUES (1, 1, 'Test description 1', 'users/1/test-s3-key-1.jpeg', NOW() - INTERVAL '10 hour', 3),
       (2, 2, 'Test description 2', 'users/2/test-s3-key-2.jpeg', NOW() - INTERVAL '9 hour', 2),
       (3, 1, 'Test description 3', 'users/1/test-s3-key-3.jpeg', NOW() - INTERVAL '8 hour', 0),
       (4, 2, 'Test description 4', 'users/2/test-s3-key-4.jpeg', NOW() - INTERVAL '7 hour', 1),
       (5, 2, 'Test description 5', 'users/2/test-s3-key-5.jpeg', NOW() - INTERVAL '10 hour', 3),
       (6, 3, 'Test description 6', 'users/3/test-s3-key-6.jpeg', NOW(), 3);

INSERT INTO gallery.likes (id, image_id, user_id)
VALUES (1, 1, 4),
       (2, 1, 2),
       (3, 1, 0),
       (4, 1, 5),
       (5, 2, 3),
       (6, 2, 1),
       (7, 4, 1),
       (8, 5, 1),
       (9, 5, 4),
       (10, 5, 2),
       (11, 6, 2),
       (12, 6, 3),
       (13, 6, 4);

