-- Creates one database per microservice inside the single Postgres container.
-- Runs automatically the FIRST time the postgres volume is created (empty data dir).
-- If you change this file later, you must recreate the volume:
--   docker compose down -v   (WARNING: deletes all data)

CREATE DATABASE auths_db;
CREATE DATABASE inventory_db;
CREATE DATABASE search_db;
CREATE DATABASE prescription_db;
CREATE DATABASE notification_db;
CREATE DATABASE report_db;
CREATE DATABASE payment_db;
CREATE DATABASE sales_db;

-- PostGIS extension for services that need geospatial queries (search / inventory).
\connect search_db
CREATE EXTENSION IF NOT EXISTS postgis;
\connect inventory_db
CREATE EXTENSION IF NOT EXISTS postgis;
