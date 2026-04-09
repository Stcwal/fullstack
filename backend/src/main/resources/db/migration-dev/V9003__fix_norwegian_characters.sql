-- Fix Norwegian characters that were corrupted due to missing characterEncoding on the JDBC connection.
-- Repairs existing rows in the Railway/production database.

UPDATE temperature_units SET name = 'Kjøleskap Sushibar 1' WHERE id = 5005;
UPDATE temperature_units SET name = 'Kjøleskap Sushibar 2' WHERE id = 5006;
UPDATE temperature_units SET name = 'Kjølerom Lager'       WHERE id = 5007;
UPDATE temperature_units SET name = 'Kjølerom Drikke'      WHERE id = 5008;
UPDATE temperature_units SET name = 'Råvarer Kaldsone'     WHERE id = 5012;

UPDATE deviations SET
  description = 'Målt temperatur var for høy i 20 min.'
  WHERE id = 8001;

UPDATE deviations SET
  title       = 'Kjølerom Drikke ustabil',
  description = 'Store variasjoner i temperatur siste døgn.'
  WHERE id = 8002;

UPDATE deviation_comments SET
  comment_text = 'Venter på servicepartner for fysisk kontroll.'
  WHERE id = 8101;

UPDATE deviation_comments SET
  comment_text = 'Gjennomført samtale med involvert ansatt.'
  WHERE id = 8103;
