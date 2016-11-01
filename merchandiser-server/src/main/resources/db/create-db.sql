CREATE TABLE IF NOT EXISTS USER (
  id Int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  login VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Request (
  id Int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id Int NOT NULL,
  unique_code Int NOT NULL UNIQUE,
  amount Int,
  type VARCHAR(10) NOT NULL,                -- buy / book / get

  FOREIGN KEY (user_id) REFERENCES User(id)
);


CREATE TABLE IF NOT EXISTS RequestInfo (
  request_id Int NOT NULL,
  amount Int NOT NULL,                        -- number of submissions
  status VARCHAR(30) NOT NULL,
  date DATETIME,                              -- date and time of last submission

  FOREIGN KEY (request_id) REFERENCES Request(id)
);