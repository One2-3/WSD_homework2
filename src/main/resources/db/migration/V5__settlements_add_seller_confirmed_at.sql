ALTER TABLE settlements
  ADD COLUMN seller_confirmed_at DATETIME NULL AFTER note;

CREATE INDEX idx_settlements_seller_confirmed_at ON settlements (seller_confirmed_at);
