CREATE TABLE IF NOT EXISTS player_ontimes(
    uuid char(36) NOT NULL,
    playtimeTotal BIGINT NOT NULL DEFAULT 0,
    playtimeWeek BIGINT NOT NULL DEFAULT 0,
    playtimeDay BIGINT NOT NULL  DEFAULT 0,
    playtimeMonth BIGINT NOT NULL DEFAULT 0,
    lastUpdate BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (uuid)
);