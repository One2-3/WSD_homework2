-- V2: users.role에 seller 추가 + users.seller_id 추가 (판매자 계정 지원)

-- 1) role enum 확장 (기존 'user','admin' -> 'user','seller','admin')
ALTER TABLE users
  MODIFY COLUMN role ENUM('user','seller','admin') NOT NULL DEFAULT 'user';

-- 2) seller_id 컬럼 추가 + FK
ALTER TABLE users
  ADD COLUMN seller_id BIGINT NULL AFTER role,
  ADD INDEX idx_users_seller_id (seller_id),
  ADD CONSTRAINT fk_users_seller FOREIGN KEY (seller_id)
    REFERENCES sellers(id) ON DELETE SET NULL ON UPDATE CASCADE;
