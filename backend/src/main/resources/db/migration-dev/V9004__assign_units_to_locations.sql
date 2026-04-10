-- Assign existing seed temperature units to locations.
-- Without this, units seeded before V14 have location_id = NULL and
-- are invisible when a location filter is applied.

UPDATE temperature_units SET location_id = 1 WHERE id IN (5001, 5002, 5005, 5006);
UPDATE temperature_units SET location_id = 2 WHERE id IN (5003, 5007, 5009);
UPDATE temperature_units SET location_id = 3 WHERE id IN (5004, 5010, 5011);
UPDATE temperature_units SET location_id = 4 WHERE id IN (5008, 5012);
