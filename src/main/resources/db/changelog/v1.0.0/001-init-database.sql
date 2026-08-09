--liquibase formatted sql

--changeset rhuan.resende:v1-001-init-database
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE SCHEMA auth;
CREATE SCHEMA users;