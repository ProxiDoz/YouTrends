CREATE TABLE User (
  id          INT PRIMARY KEY,
  firstName   TEXT,
  lastName    TEXT,
  userName    TEXT,
  language    TEXT,
  password    TEXT,
  isBot       BOOL,
  isBanned    BOOL DEFAULT FALSE,
  isSubscribe BOOL DEFAULT FALSE
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;

CREATE TABLE BannedChannel (
  name VARCHAR(64) PRIMARY KEY
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;

CREATE TABLE BannedTag (
  name VARCHAR(64) PRIMARY KEY
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;

CREATE TABLE UserBannedChannel (
  id        INTEGER PRIMARY KEY AUTO_INCREMENT,
  userId    INTEGER     NOT NULL,
  channelId VARCHAR(64) NOT NULL,
  FOREIGN KEY (userId) REFERENCES User (id),
  FOREIGN KEY (channelId) REFERENCES BannedChannel (name)
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;

CREATE TABLE UserBannedTag (
  id     INTEGER PRIMARY KEY AUTO_INCREMENT,
  userId INTEGER     NOT NULL,
  tagId  VARCHAR(64) NOT NULL,
  FOREIGN KEY (userId) REFERENCES User (id),
  FOREIGN KEY (tagId) REFERENCES BannedTag (name)
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;