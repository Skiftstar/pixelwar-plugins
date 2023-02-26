CREATE TABLE IF NOT EXISTS paper_core_config(
    id char(1) NOT NULL,
    scoreboardRefreshDelay INT NOT NULL,
    serverName char(50) NOT NULL,
    filterFromPrefixForScoreboard char(200) NOT NULL,
    discordLink char(200) NOT NULL,
    cacheTimeout INT NOT NULL,
    PRIMARY KEY (id)
);