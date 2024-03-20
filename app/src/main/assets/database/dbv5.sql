-- Create the User table
CREATE TABLE IF NOT EXISTS `User` (
  `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `image` BLOB,
  `password` TEXT,
  `last_name` TEXT,
  `bio` TEXT,
  `first_name` TEXT,
  `email` TEXT,
  `username` TEXT,
  `phone_number` TEXT
);

-- Create the Event table
CREATE TABLE IF NOT EXISTS `Event` (
  `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `name` TEXT,
  `description` TEXT,
  `owner_user_id` INTEGER NOT NULL,
  `category` TEXT,
  `location` TEXT,
  `date` TEXT,
   `promoted` INTEGER NOT NULL,
  `image` BLOB,
  `visibility` TEXT
);

-- Create the Invitee table
CREATE TABLE IF NOT EXISTS `Invitee` (
  `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `event_id` INTEGER NOT NULL,
  `user_id` INTEGER NOT NULL
);

-- Create the Attendee table
CREATE TABLE IF NOT EXISTS `Attendee` (
  `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `event_id` INTEGER NOT NULL,
  `user_id` INTEGER NOT NULL
);

-- Create the Rating table
CREATE TABLE IF NOT EXISTS `Rating` (
  `rating` INTEGER NOT NULL,
  `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `comment` TEXT,
   `date` TEXT,
  `event_id` INTEGER NOT NULL,
  `user_id` INTEGER NOT NULL);


CREATE TABLE IF NOT EXISTS FriendRequest (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    sender INTEGER,
    receiver INTEGER,
    status TEXT
);

-- Create indices

-- Index for Invitee table
CREATE  UNIQUE INDEX IF NOT EXISTS `index_Invitee_event_id_user_id` ON `Invitee` (`event_id`, `user_id`);

-- Index for Attendee table
CREATE UNIQUE INDEX IF NOT EXISTS `index_Attendee_event_id_user_id` ON `Attendee` (`event_id`, `user_id`);

-- Index for Rating table
CREATE UNIQUE INDEX IF NOT EXISTS `index_Rating_event_id_user_id` ON `Rating` (`event_id`, `user_id`);

