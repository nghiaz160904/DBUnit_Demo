-- Stored procedure: tạo bài viết mới
CREATE OR REPLACE PROCEDURE sp_create_post(
    p_user_id INT,
    p_title VARCHAR,
    p_content TEXT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO posts(user_id, title, content)
    VALUES (p_user_id, p_title, p_content);
END;
$$;
