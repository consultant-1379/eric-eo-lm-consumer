CREATE TABLE app_license_key (
  license_key_id VARCHAR(30) NOT NULL UNIQUE,
  application VARCHAR(10) NOT NULL,
  PRIMARY KEY (license_key_id, application)
);