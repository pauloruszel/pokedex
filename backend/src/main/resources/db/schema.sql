CREATE TABLE IF NOT EXISTS app_metadata (
    metadata_key VARCHAR(80) PRIMARY KEY,
    metadata_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pokemon (
    id INT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    sprite_url VARCHAR(500),
    height INT,
    weight INT,
    source_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pokemon_name ON pokemon(name);

CREATE TABLE IF NOT EXISTS pokemon_species (
    pokemon_id INT PRIMARY KEY,
    genus VARCHAR(255),
    flavor_text CLOB,
    text_locale VARCHAR(20),
    color VARCHAR(80),
    habitat VARCHAR(80),
    generation VARCHAR(80),
    CONSTRAINT fk_species_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE
);

ALTER TABLE pokemon_species ADD COLUMN IF NOT EXISTS text_locale VARCHAR(20);

CREATE TABLE IF NOT EXISTS pokemon_text_translation (
    translation_key VARCHAR(80) PRIMARY KEY,
    source_text CLOB NOT NULL,
    text_kind VARCHAR(40) NOT NULL,
    locale VARCHAR(20) NOT NULL,
    translated_text CLOB NOT NULL,
    translation_source VARCHAR(40) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE pokemon_text_translation ADD COLUMN IF NOT EXISTS source_locale VARCHAR(20);
ALTER TABLE pokemon_text_translation ADD COLUMN IF NOT EXISTS target_locale VARCHAR(20);
ALTER TABLE pokemon_text_translation ADD COLUMN IF NOT EXISTS text_hash VARCHAR(80);
ALTER TABLE pokemon_text_translation ADD COLUMN IF NOT EXISTS entity_type VARCHAR(40);
ALTER TABLE pokemon_text_translation ADD COLUMN IF NOT EXISTS entity_id VARCHAR(120);

CREATE INDEX IF NOT EXISTS idx_pokemon_text_translation_lookup
    ON pokemon_text_translation(text_kind, locale);

CREATE INDEX IF NOT EXISTS idx_pokemon_text_translation_locale_hash
    ON pokemon_text_translation(text_kind, source_locale, target_locale, text_hash);

CREATE INDEX IF NOT EXISTS idx_pokemon_text_translation_entity
    ON pokemon_text_translation(entity_type, entity_id, target_locale);

CREATE TABLE IF NOT EXISTS translation_job_status (
    job_name VARCHAR(80) PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    total INT NOT NULL DEFAULT 0,
    processed INT NOT NULL DEFAULT 0,
    failures INT NOT NULL DEFAULT 0,
    last_error CLOB,
    started_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pokemon_type (
    pokemon_id INT NOT NULL,
    type_name VARCHAR(80) NOT NULL,
    slot_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (pokemon_id, type_name),
    CONSTRAINT fk_type_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_pokemon_type_name ON pokemon_type(type_name);

CREATE TABLE IF NOT EXISTS pokemon_ability (
    pokemon_id INT NOT NULL,
    ability_name VARCHAR(120) NOT NULL,
    slot_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (pokemon_id, ability_name),
    CONSTRAINT fk_ability_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pokemon_stat (
    pokemon_id INT NOT NULL,
    stat_name VARCHAR(120) NOT NULL,
    stat_value INT NOT NULL,
    PRIMARY KEY (pokemon_id, stat_name),
    CONSTRAINT fk_stat_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pokemon_evolution (
    pokemon_id INT NOT NULL,
    evolution_name VARCHAR(120) NOT NULL,
    chain_order INT NOT NULL,
    PRIMARY KEY (pokemon_id, evolution_name, chain_order),
    CONSTRAINT fk_evolution_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pokemon_reference_type (
    name VARCHAR(80) PRIMARY KEY,
    display_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS pokemon_image (
    pokemon_id INT NOT NULL,
    image_type VARCHAR(80) NOT NULL,
    source_url VARCHAR(1000),
    local_path VARCHAR(1000),
    public_url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(120),
    size_bytes BIGINT,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pokemon_id, image_type)
);

CREATE INDEX IF NOT EXISTS idx_pokemon_image_pokemon ON pokemon_image(pokemon_id);

CREATE TABLE IF NOT EXISTS trunfo_room (
    code VARCHAR(12) PRIMARY KEY,
    state VARCHAR(40) NOT NULL,
    mode VARCHAR(40) NOT NULL,
    difficulty VARCHAR(40) NOT NULL,
    deck_size INT NOT NULL DEFAULT 8,
    type_name VARCHAR(80),
    player_one_name VARCHAR(80) NOT NULL,
    player_two_name VARCHAR(80),
    player_one_token VARCHAR(80) NOT NULL,
    player_two_token VARCHAR(80),
    player_one_deck CLOB,
    player_two_deck CLOB,
    dispute_pile CLOB,
    history CLOB,
    current_turn VARCHAR(20) NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    winner VARCHAR(80),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

ALTER TABLE trunfo_room ADD COLUMN IF NOT EXISTS deck_size INT NOT NULL DEFAULT 8;
