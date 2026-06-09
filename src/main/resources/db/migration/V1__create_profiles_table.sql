CREATE TABLE profiles (
id BIGSERIAL PRIMARY KEY,
code VARCHAR(10) NOT NULL UNIQUE,
name VARCHAR(100) NOT NULL,
description VARCHAR(255),
active BOOLEAN NOT NULL DEFAULT TRUE,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO profiles (code, name, description) VALUES
('36', '36 front', 'Perfil 36 front'),
('37', '37 rear', 'Perfil 37 rear'),
('38', '38 front', 'Perfil 38 front'),
('39', '39 rear con liga', 'Perfil 39 rear con liga');