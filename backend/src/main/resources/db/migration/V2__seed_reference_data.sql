INSERT INTO app_metadata (metadata_key, metadata_value) VALUES
('project', 'Pokedex Clean Architecture'),
('database', 'Flyway-managed relational database'),
('seed_strategy', 'Reference data is inserted by Flyway; Pokemon catalog is bootstrapped from PokeAPI and then cached locally.');

INSERT INTO pokemon_reference_type (name, display_order) VALUES
('normal', 1),
('fire', 2),
('water', 3),
('electric', 4),
('grass', 5),
('ice', 6),
('fighting', 7),
('poison', 8),
('ground', 9),
('flying', 10),
('psychic', 11),
('bug', 12),
('rock', 13),
('ghost', 14),
('dragon', 15),
('dark', 16),
('steel', 17),
('fairy', 18);
