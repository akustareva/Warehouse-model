CREATE TABLE IF NOT EXISTS Goods (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  quantity INT
);

CREATE TABLE IF NOT EXISTS OrderTypeList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(10) NOT NULL                 -- buy / book / cancel
);

CREATE TABLE IF NOT EXISTS StatusList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  status VARCHAR(20) NOT NULL                 -- done/ in progress / canceled
);

CREATE TABLE IF NOT EXISTS Request (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id Int NOT NULL,
  goods_id Int NOT NULL,
  quantity INT,
  type INT,
  date DATETIME,                               -- date and time of last submission
  attempts_count INT,                          -- number of submissions
  status INT,

  FOREIGN KEY (goods_id) REFERENCES Goods(id),
  FOREIGN KEY (type) REFERENCES OrderTypeList(id),
  FOREIGN KEY (status) REFERENCES StatusList(id)
);