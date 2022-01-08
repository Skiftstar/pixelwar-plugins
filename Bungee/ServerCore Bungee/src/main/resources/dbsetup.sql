CREATE TABLE IF NOT EXISTS bans(
    banUUID char(50) NOT NULL,
    uuid char(36) NOT NULL,
    banType char(255) NOT NULL,
    banReasonKey char(255) NOT NULL,
    bannedBy char(36) NOT NULL,
    bannedOn BIGINT NOT NULL,
    unbanOn BIGINT NOT NULL,
    PRIMARY KEY (banUUID)
);

CREATE TABLE IF NOT EXISTS banlogs(
    banUUID char(50) NOT NULL ,
    uuid char(36) NOT NULL,
    banType char(255) NOT NULL,
    banReasonKey char(255) NOT NULL,
    bannedBy char(36) NOT NULL,
    bannedOn BIGINT NOT NULL,
    unbanOn BIGINT NOT NULL,
    earlyUnban BOOL NOT NULL,
    earlyUnbanByUUID char(36),
    earlyUnbanOn BIGINT,
    combinedIntoNew char(36),
    PRIMARY KEY (banUUID)
);