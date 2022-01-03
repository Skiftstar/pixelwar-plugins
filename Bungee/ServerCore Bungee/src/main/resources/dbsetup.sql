CREATE TABLE IF NOT EXISTS bans(
    banUUID varchar(50) NOT NULL,
    uuid char(36) NOT NULL,
    banType char(255) NOT NULL,
    banReasonKey char(255) NOT NULL,
    bannedBy char(36) NOT NULL,
    bannedOn BIGINT NOT NULL,
    unbanOn BIGINT NOT NULL,
    CONSTRAINT banID
        UNIQUE (banUUID, uuid)
);

CREATE TABLE IF NOT EXISTS banlogs(
    banUUID varchar(50) NOT NULL ,
    uuid char(36) NOT NULL,
    banType char(255) NOT NULL,
    banReasonKey char(255) NOT NULL,
    bannedBy char(36) NOT NULL,
    bannedOn BIGINT NOT NULL,
    unbanOn BIGINT NOT NULL,
    CONSTRAINT banID
        UNIQUE (banUUID, uuid)
);