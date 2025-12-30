-- Create admin user (same as DataInitializer uses: admin@example.com / admin)
-- BCrypt hash for "admin" password
INSERT INTO users (
    email,
    password,
    role,
    first_name,
    last_name,
    enabled
  )
SELECT 'admin@example.com',
  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
  'ADMIN',
  'Admin',
  'User',
  TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@example.com'
  );

-- Create a test comment that is 48 hours old (for testing "too old to delete" scenario)
INSERT INTO comments (book_id, user_id, text, created_at)
SELECT 1,
  (
    SELECT id
    FROM users
    WHERE email = 'admin@example.com'
  ),
  'This is an OLD test comment (48 hours ago) - should NOT be deletable',
  CURRENT_TIMESTAMP - INTERVAL '48 hours';

-- Create a test comment that is 2 hours old (for testing successful deletion)
INSERT INTO comments (book_id, user_id, text, created_at)
SELECT 1,
  (
    SELECT id
    FROM users
    WHERE email = 'admin@example.com'
  ),
  'This is a RECENT test comment (2 hours ago) - should be deletable',
  CURRENT_TIMESTAMP - INTERVAL '2 hours';
