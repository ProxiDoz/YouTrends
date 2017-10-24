CREATE TABLE Feed(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  dateCollect TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE Video(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  feedId INTEGER NOT NULL,
  name TEXT,
  description TEXT,
  channel TEXT,
  videoId TEXT,
  imageUrl TEXT,
  old TEXT,
  viewCount INTEGER,
  FOREIGN KEY (feedId) REFERENCES Feed(id)
)CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE BannedTag(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name TEXT NOT NULL
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE BannedTagsStat(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  tagId INTEGER,
  date TIMESTAMP,
  count INTEGER,
  FOREIGN KEY (tagId) REFERENCES BannedTag(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE BannedChannel(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name TEXT NOT NULL
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE User(
  chatId VARCHAR (16) PRIMARY KEY,
  isBanned BOOL DEFAULT FALSE,
  isSubscribed BOOL DEFAULT FALSE
) CHARACTER SET utf8 COLLATE utf8_general_ci;