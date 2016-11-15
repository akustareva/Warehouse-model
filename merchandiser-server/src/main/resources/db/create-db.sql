CREATE TABLE IF NOT EXISTS USER (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  login VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS OrderTypeList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(10) NOT NULL                   -- booked / paid / cancel
);

CREATE TABLE IF NOT EXISTS StatusList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  status VARCHAR(20) NOT NULL                 -- done/ in progress / canceled
);

CREATE TABLE IF NOT EXISTS Request (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id INT NOT NULL,
  goods_id INT NOT NULL,
  quantity INT,
  type INT,
  date TIMESTAMP,                             -- date and time of last submission
  attempts_count INT,                         -- number of submissions
  status INT,

  FOREIGN KEY (user_id) REFERENCES User(id),
  FOREIGN KEY (type) REFERENCES OrderTypeList(id),
  FOREIGN KEY (status) REFERENCES StatusList(id)
);