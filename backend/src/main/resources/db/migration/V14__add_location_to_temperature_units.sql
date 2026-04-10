ALTER TABLE temperature_units
    ADD COLUMN location_id BIGINT NULL;

ALTER TABLE temperature_units
    ADD CONSTRAINT fk_temperature_units_location
        FOREIGN KEY (location_id) REFERENCES locations(id);

CREATE INDEX idx_temperature_units_location_id ON temperature_units(location_id);
