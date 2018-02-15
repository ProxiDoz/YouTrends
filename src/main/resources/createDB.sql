CREATE TABLE Users (
  id          INTEGER PRIMARY KEY,
  firstName   TEXT,
  lastName    TEXT,
  userName    TEXT,
  language    TEXT,
  password    TEXT,
  isBot       BOOL,
  isBanned    BOOL DEFAULT FALSE,
  isSubscribe BOOL DEFAULT FALSE,
  regDate     TIMESTAMP DEFAULT now()
);

CREATE TABLE BannedChannel (
  name VARCHAR(64) PRIMARY KEY
);

CREATE TABLE BannedTag (
  name VARCHAR(64) PRIMARY KEY
);

CREATE TABLE UserBannedChannel (
  id        SERIAL,
  userId    INTEGER     NOT NULL,
  channelId VARCHAR(64) NOT NULL,
  FOREIGN KEY (userId) REFERENCES Users (id),
  FOREIGN KEY (channelId) REFERENCES BannedChannel (name)
);

CREATE TABLE UserBannedTag (
  id     SERIAL,
  userId INTEGER     NOT NULL,
  tagId  VARCHAR(64) NOT NULL,
  FOREIGN KEY (userId) REFERENCES Users (id),
  FOREIGN KEY (tagId) REFERENCES BannedTag (name)
);

CREATE TABLE MessagesHistory (
  id        SERIAL,
  sender    TEXT,
  recepient TEXT,
  message   TEXT,
  date      TIMESTAMP
);

CREATE TABLE Video (
  id SERIAL,
  videoId TEXT,
  title TEXT,
  description TEXT,
  channel TEXT,
  imgUrl TEXT,
  old TEXT,
  viewCount INTEGER,
  date TIMESTAMP
);