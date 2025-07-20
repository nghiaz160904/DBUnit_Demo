-- src/main/resources/sql/triggers.sql

-- Nếu đã có trigger/function cũ thì xoá đi
DROP TRIGGER IF EXISTS before_insert_posts ON posts;
DROP FUNCTION IF EXISTS trg_set_created_at();

-- Tạo lại trigger function
CREATE OR REPLACE FUNCTION trg_set_created_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at := COALESCE(NEW.created_at, CURRENT_TIMESTAMP);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger mới
CREATE TRIGGER before_insert_posts
  BEFORE INSERT ON posts
  FOR EACH ROW
  EXECUTE FUNCTION trg_set_created_at();
