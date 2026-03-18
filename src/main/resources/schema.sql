CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS balances (
    user_id BIGINT PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL DEFAULT 5000.00,
    CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS lotto_games (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    max_number INT NOT NULL,
    draw_time VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS bets (
    id VARCHAR(80) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id VARCHAR(50) NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    numbers VARCHAR(100) NOT NULL,
    stake DECIMAL(10,2) NOT NULL,
    draw_date_key VARCHAR(20) NOT NULL,
    placed_at VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    matches INT,
    payout DECIMAL(15,2),
    official_numbers VARCHAR(100),
    CONSTRAINT fk_bet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed demo user (password: pcso2026 BCrypt hashed)
INSERT IGNORE INTO users (id, username, password_hash, display_name) VALUES
    (1, 'demo-player', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo Account');

INSERT IGNORE INTO balances (user_id, amount) VALUES (1, 5000.00);

-- Seed PCSO games
INSERT IGNORE INTO lotto_games (id, name, max_number, draw_time) VALUES
    ('lotto-642',  'Lotto 6/42',       42, '9:00 PM Daily'),
    ('mega-645',   'Mega Lotto 6/45',  45, '9:00 PM Daily'),
    ('super-649',  'Super Lotto 6/49', 49, '9:00 PM Daily'),
    ('grand-655',  'Grand Lotto 6/55', 55, '9:00 PM Daily'),
    ('ultra-658',  'Ultra Lotto 6/58', 58, '9:00 PM Daily');
