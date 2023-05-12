-- UUID soll immer zu binary convertiert werden
-- -> Insert UUID_TO_BIN(uuid)
-- -> Query BIN_TO_UUID(id)

CREATE TABLE IF NOT EXISTS players(
    uuid binary(16) NOT NULL,
    username char(16) NOT NULL,
    last_used_profile_id char(16),

    PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS worlds(
    world_id binary(16) NOT NULL, 
    owner_id binary(16) NOT NULL,
    spawn_x DOUBLE NOT NULL,
    spawn_y DOUBLE NOT NULL,
    spawn_z DOUBLE NOT NULL,
    spawn_yaw FLOAT NOT NULL,
    spawn_pitch FLOAT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES players(uuid) ON DELETE CASCADE,
    PRIMARY KEY (world_id)
);

CREATE TABLE IF NOT EXISTS roles(
    role_id binary(16) NOT NULL,
    role_name char(255) NOT NULL,

    UNIQUE(role_name),
    PRIMARY KEY (role_id)
);

CREATE TABLE IF NOT EXISTS skills(
    skill_id binary(16) NOT NULL,
    skill_name char(255) NOT NULL,

    UNIQUE(skill_name),
    PRIMARY KEY (skill_id)
);

CREATE TABLE IF NOT EXISTS attributes(
    attribute_id binary(16) NOT NULL,
    attribute_name char(255) NOT NULL,
    start_level INT NOT NULL,

    UNIQUE(attribute_name),
    PRIMARY KEY (attribute_id)
);

CREATE TABLE IF NOT EXISTS npcs(
    npc_id binary(16) NOT NULL,
    npc_name char(255) NOT NULL,
    npc_skin_name char(36) NOT NULL,

    PRIMARY KEY (npc_id)
);

CREATE TABLE IF NOT EXISTS rewards(
    reward_id binary(16) NOT NULL,
    reward_type char(255),
    xp_reward INT,

    PRIMARY KEY (reward_id)
);

CREATE TABLE IF NOT EXISTS quests(
    quest_id binary(16) NOT NULL,
    quest_name char(255) NOT NULL,
    reward_id binary(16),

    FOREIGN KEY (reward_id) REFERENCES rewards(reward_id) ON DELETE SET NULL,
    PRIMARY KEY (quest_id)
);

CREATE TABLE IF NOT EXISTS quest_stages(
    quest_stage_id binary(16) NOT NULL,
    stage_name char(255) NOT NULL,
    stage_number INT NOT NULL,
    quest_id binary(16) NOT NULL,
    reward_id binary(16),

    FOREIGN KEY (quest_id) REFERENCES quests(quest_id) ON DELETE CASCADE,
    FOREIGN KEY (reward_id) REFERENCES rewards(reward_id) ON DELETE SET NULL,
    PRIMARY KEY (quest_stage_id)
);

CREATE TABLE IF NOT EXISTS dungeons(
    dungeon_id binary(16) NOT NULL,
    dungeon_name char(255) NOT NULL,
    recommendedLevel INT NOT NULL,

    PRIMARY KEY (dungeon_id)
);

CREATE TABLE IF NOT EXISTS player_data(
    id binary(16) NOT NULL,
    player_id binary(16) NOT NULL,
    world_id binary(16) NOT NULL,
    role_id binary(16),
    lvl INT DEFAULT 1,
    attribute_points INT DEFAULT 0,
    chunk_claim_points INT DEFAULT 0,
    logout_x DOUBLE,
    logout_y DOUBLE,
    logout_z DOUBLE,
    logout_pitch FLOAT,
    logout_yaw FLOAT,

    FOREIGN KEY (player_id) REFERENCES players(uuid) ON DELETE CASCADE,
    FOREIGN KEY (world_id) REFERENCES worlds(world_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS player_skills(
    id binary(16) NOT NULL,
    player_data_id binary(16) NOT NULL,
    skill_id binary(16) NOT NULL,

    FOREIGN KEY (player_data_id) REFERENCES player_data(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS player_attributes(
    id binary(16) NOT NULL,
    player_data_id binary(16) NOT NULL,
    attribute_id binary(16) NOT NULL,
    lvl INT NOT NULL,

    FOREIGN KEY (player_data_id) REFERENCES player_data(id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS player_quests(
    id binary(16) NOT NULL,
    player_data_id binary(16) NOT NULL,
    quest_id binary(16) NOT NULL,
    curr_stage binary(16),
    completed boolean NOT NULL,
    begun boolean NOT NULL,

    FOREIGN KEY (player_data_id) REFERENCES player_data(id) ON DELETE CASCADE,
    FOREIGN KEY (quest_id) REFERENCES quests(quest_id) ON DELETE CASCADE,
    FOREIGN KEY (curr_stage) REFERENCES quest_stages(quest_stage_id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS player_dungeons(
    id binary(16) NOT NULL,
    player_data_id binary(16) NOT NULL,
    dungeon_id binary(16) NOT NULL,
    cleared boolean NOT NULL,

    FOREIGN KEY (player_data_id) REFERENCES player_data(id) ON DELETE CASCADE,
    FOREIGN KEY (dungeon_id) REFERENCES dungeons(dungeon_id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

ALTER TABLE players ADD FOREIGN KEY (last_used_profile_id) REFERENCES player_data(id) ON DELETE SET NULL;