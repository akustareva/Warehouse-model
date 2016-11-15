CREATE TABLE IF NOT EXISTS Goods (
  id INT NOT NULL PRIMARY KEY,
  quantity INT
);

CREATE TABLE IF NOT EXISTS OrderTypeList (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(10) NOT NULL                 -- booked / paid / cancel
);

CREATE TABLE IF NOT EXISTS Request (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id Int NOT NULL,
  goods_id Int NOT NULL,
  quantity INT,
  type INT,

  FOREIGN KEY (goods_id) REFERENCES Goods(id),
  FOREIGN KEY (type) REFERENCES OrderTypeList(id)
);