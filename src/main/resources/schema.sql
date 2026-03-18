CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
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

CREATE TABLE IF NOT EXISTS official_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id VARCHAR(50) NOT NULL,
    draw_date_key VARCHAR(20) NOT NULL,
    numbers VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_game_draw (game_id, draw_date_key)
);

-- Seed demo user (password: pcso2026 BCrypt hashed)
INSERT IGNORE INTO users (username, password_hash, display_name, role) VALUES
    ('demo-player', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo Account', 'user');

-- Seed admin user (username: admin, password: admin123)
INSERT IGNORE INTO users (username, password_hash, display_name, role) VALUES
    ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', 'admin');

-- Seed balances for demo and admin users
INSERT IGNORE INTO balances (user_id, amount) 
SELECT id, 5000.00 FROM users WHERE username = 'demo-player' AND NOT EXISTS (SELECT 1 FROM balances WHERE user_id = (SELECT id FROM users WHERE username = 'demo-player'));

INSERT IGNORE INTO balances (user_id, amount) 
SELECT id, 999999.00 FROM users WHERE username = 'admin' AND NOT EXISTS (SELECT 1 FROM balances WHERE user_id = (SELECT id FROM users WHERE username = 'admin'));

-- Seed PCSO games
INSERT IGNORE INTO lotto_games (id, name, max_number, draw_time) VALUES
    ('lotto-642',  'Lotto 6/42',       42, '9:00 PM Daily'),
    ('mega-645',   'Mega Lotto 6/45',  45, '9:00 PM Daily'),
    ('super-649',  'Super Lotto 6/49', 49, '9:00 PM Daily'),
    ('grand-655',  'Grand Lotto 6/55', 55, '9:00 PM Daily'),
    ('ultra-658',  'Ultra Lotto 6/58', 58, '9:00 PM Daily');
