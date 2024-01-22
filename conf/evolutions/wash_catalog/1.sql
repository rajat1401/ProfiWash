# --- !Downs
DROP TABLE station_i18n;
DROP TABLE city_i18n;
DROP TABLE station;
DROP TABLE language;
DROP TABLE city;



# --- !Ups
CREATE TABLE language (
    lang_code varchar(4) CHARACTER SET "ascii" NOT NULL,
    name_eng VARCHAR(255) NOT NULL,
    name_native VARCHAR(255) NOT NULL,
    PRIMARY KEY (lang_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE city (
    city_id INT AUTO_INCREMENT NOT NULL,
    PRIMARY KEY(city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE station (
    station_id INT NOT NULL,
    city_id INT NOT NULL,
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL,
    PRIMARY KEY (station_id),
    FOREIGN KEY (city_id) REFERENCES city(city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE station_i18n (
    station_id INT NOT NULL,
    lang_code VARCHAR(4) CHARACTER SET "ascii" NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    PRIMARY KEY (station_id,lang_code),
    FOREIGN KEY (`station_id`) REFERENCES `station`(`station_id`) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (`lang_code`) REFERENCES `language`(`lang_code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE city_i18n (
    city_id INT AUTO_INCREMENT NOT NULL,
    lang_code VARCHAR(4) CHARACTER SET "ascii" NOT NULL,
    name varchar(255) NOT NULL,
    PRIMARY KEY(city_id,lang_code),
    FOREIGN KEY (`city_id`) REFERENCES `city`(`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`lang_code`) REFERENCES `language`(`lang_code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO language (lang_code, name_eng, name_native) VALUES ('en','english','english'), ('ua','ukrainian','українська');
INSERT INTO city VALUES (1);
INSERT INTO city_i18n VALUES(1,'en','Kyiv');
INSERT INTO city_i18n VALUES(1,'ua','Київ');
INSERT INTO station (station_id, city_id, latitude, longitude) VALUES (215,1,50.52807385664755, 30.509626582669515);
INSERT INTO station_i18n (station_id, lang_code, name, address) VALUES (215,'ua','Мийка по вул. Північна',"вул. Північна");