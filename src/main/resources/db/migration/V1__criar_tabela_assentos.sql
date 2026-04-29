CREATE TABLE assentos (id BIGSERIAL PRIMARY KEY,
                       fileira VARCHAR(5) NOT NULL,
                       numero INTEGER NOT NULL,
                       tipo VARCHAR(20) NOT NULL,
                       valor DECIMAL(10, 2) NOT NULL,
                       ocupado BOOLEAN DEFAULT FALSE,
                       version BIGINT DEFAULT 0
);