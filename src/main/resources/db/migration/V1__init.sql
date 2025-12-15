-- schema: bookstore (DB는 미리 생성해두기)

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password TEXT NOT NULL,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NULL,
  region VARCHAR(100) NULL,
  phone VARCHAR(30) NULL,
  gender ENUM('male','female','other','unknown') NULL,
  birthdate DATE NULL,
  role ENUM('user','admin') NOT NULL DEFAULT 'user',
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted_at TIMESTAMP(6) NULL,
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE sellers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  contact_email VARCHAR(255) NULL,
  phone VARCHAR(30) NULL,
  address VARCHAR(255) NULL,
  business_no VARCHAR(50) NULL,
  bank_name VARCHAR(100) NULL,
  bank_account_masked VARCHAR(100) NULL,
  commission_bps INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted_at TIMESTAMP(6) NULL
) ENGINE=InnoDB;

CREATE TABLE token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash TEXT NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  revoked_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_token_user (user_id),
  CONSTRAINT fk_token_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  price_cents INT NOT NULL,
  stock INT NOT NULL,
  average_rating FLOAT NULL,
  ratings_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted_at TIMESTAMP(6) NULL,
  INDEX idx_books_seller (seller_id),
  CONSTRAINT fk_books_seller FOREIGN KEY (seller_id)
    REFERENCES sellers(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_books_price CHECK (price_cents >= 0),
  CONSTRAINT ck_books_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE authors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB;

CREATE TABLE book_authors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  UNIQUE KEY uk_book_authors (book_id, author_id),
  INDEX idx_book_authors_author (author_id),
  CONSTRAINT fk_book_authors_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id)
    REFERENCES authors(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE book_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  UNIQUE KEY uk_book_categories (book_id, category_id),
  INDEX idx_book_categories_category (category_id),
  CONSTRAINT fk_book_categories_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_book_categories_category FOREIGN KEY (category_id)
    REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  book_id BIGINT NOT NULL,
  rating INT NOT NULL,
  body TEXT NOT NULL,
  like_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_reviews_user_book (user_id, book_id),
  INDEX idx_reviews_book_created (book_id, created_at),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_reviews_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE review_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  review_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_review_likes (user_id, review_id),
  CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id)
    REFERENCES reviews(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  user_id BIGINT NULL,
  body TEXT NOT NULL,
  like_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_comments_review FOREIGN KEY (review_id)
    REFERENCES reviews(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE comment_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  comment_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_comment_likes (user_id, comment_id),
  CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id)
    REFERENCES comments(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE wishlist_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_wishlist_items (user_id, book_id),
  CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_wishlist_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE library_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_library_items (user_id, book_id),
  CONSTRAINT fk_library_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_library_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE carts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_carts_user (user_id),
  CONSTRAINT fk_carts_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cart_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price_cents INT NOT NULL,
  UNIQUE KEY uk_cart_items (cart_id, book_id),
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id)
    REFERENCES carts(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_cart_items_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_cart_items_qty CHECK (quantity >= 1),
  CONSTRAINT ck_cart_items_price CHECK (unit_price_cents >= 0)
) ENGINE=InnoDB;

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  status ENUM('pending','paid','shipped','delivered','cancelled','refunded') NOT NULL,
  total_amount_cents INT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX idx_orders_user_created (user_id, created_at),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_orders_total CHECK (total_amount_cents >= 0)
) ENGINE=InnoDB;

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price_cents INT NOT NULL,
  subtotal_cents INT NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
    REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_seller FOREIGN KEY (seller_id)
    REFERENCES sellers(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_order_items_qty CHECK (quantity >= 1),
  CONSTRAINT ck_order_items_prices CHECK (unit_price_cents >= 0 AND subtotal_cents >= 0)
) ENGINE=InnoDB;

CREATE TABLE settlements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  status ENUM('draft','pending','approved','paid','cancelled') NOT NULL,
  total_gross_cents INT NOT NULL,
  total_commission_cents INT NOT NULL,
  total_net_cents INT NOT NULL,
  paid_at TIMESTAMP(6) NULL,
  note TEXT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX idx_settlements_seller_period (seller_id, period_start, period_end),
  CONSTRAINT fk_settlements_seller FOREIGN KEY (seller_id)
    REFERENCES sellers(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE settlement_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  gross_cents INT NOT NULL,
  commission_cents INT NOT NULL,
  net_cents INT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_settlement_items_settlement (settlement_id),
  INDEX idx_settlement_items_seller (seller_id),
  CONSTRAINT fk_settlement_items_settlement FOREIGN KEY (settlement_id)
    REFERENCES settlements(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_settlement_items_order_item FOREIGN KEY (order_item_id)
    REFERENCES order_items(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_settlement_items_seller FOREIGN KEY (seller_id)
    REFERENCES sellers(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE book_views (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  book_id BIGINT NOT NULL,
  ip_address VARCHAR(64) NULL,
  user_agent TEXT NULL,
  occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_book_views_book_time (book_id, occurred_at),
  INDEX idx_book_views_user_time (user_id, occurred_at),
  CONSTRAINT fk_book_views_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_book_views_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE wishlist_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  book_id BIGINT NOT NULL,
  action ENUM('add','remove') NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_wishlist_events_book_time (book_id, occurred_at),
  INDEX idx_wishlist_events_user_time (user_id, occurred_at),
  CONSTRAINT fk_wishlist_events_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_wishlist_events_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cart_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  book_id BIGINT NOT NULL,
  action ENUM('add','remove','update') NOT NULL,
  quantity INT NULL,
  occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_cart_events_book_time (book_id, occurred_at),
  INDEX idx_cart_events_user_time (user_id, occurred_at),
  CONSTRAINT fk_cart_events_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_cart_events_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE book_rankings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  date DATE NOT NULL,
  metric VARCHAR(30) NOT NULL,
  dimension VARCHAR(30) NOT NULL,
  dimension_value VARCHAR(100) NULL,
  book_id BIGINT NOT NULL,
  `rank` INT NOT NULL,
  `count` INT NOT NULL,
  created_at TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_book_rankings_lookup (date, metric, dimension, dimension_value, `rank`),
  CONSTRAINT fk_book_rankings_book FOREIGN KEY (book_id)
    REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
