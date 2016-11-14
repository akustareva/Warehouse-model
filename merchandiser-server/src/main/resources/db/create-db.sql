CREATE TABLE IF NOT EXISTS USER (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  login VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS OrderTypeList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(10) NOT NULL                 -- booked / paid / cancel
);

CREATE TABLE IF NOT EXISTS Request (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id INT NOT NULL,
  goods_id INT NOT NULL UNIQUE,
  quantity INT,
  type INT,

  FOREIGN KEY (user_id) REFERENCES User(id),
  FOREIGN KEY (type) REFERENCES OrderTypeList(id)
);

CREATE TABLE IF NOT EXISTS RequestLog (
  request_id BIGINT NOT NULL,
  date DATETIME,                              -- date and time of last submission
  attempts_count INT,                         -- number of submissions

  FOREIGN KEY (request_id) REFERENCES Request(id)
);