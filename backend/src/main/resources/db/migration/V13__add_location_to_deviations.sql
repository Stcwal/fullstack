ALTER TABLE deviations
    ADD COLUMN location_id BIGINT NULL;

ALTER TABLE deviations
    ADD CONSTRAINT fk_deviations_location
        FOREIGN KEY (location_id) REFERENCES locations(id);

CREATE INDEX idx_deviations_location_id ON deviations(location_id);
