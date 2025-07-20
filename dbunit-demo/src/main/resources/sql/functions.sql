-- src/main/resources/sql/functions.sql

-- Function: get_user_post_count
CREATE OR REPLACE FUNCTION get_user_post_count(u_id INT)
  RETURNS INT
AS $$
BEGIN
    -- Đếm số bài viết của user
    RETURN (
        SELECT COUNT(*)
        FROM posts
        WHERE user_id = u_id
    );
END;
$$ LANGUAGE plpgsql;
