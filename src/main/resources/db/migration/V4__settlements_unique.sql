-- V4: prevent duplicate settlements for the same seller + period
ALTER TABLE settlements
  ADD UNIQUE KEY uk_settlements_seller_period (seller_id, period_start, period_end);
