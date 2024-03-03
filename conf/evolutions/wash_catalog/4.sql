# --- !Ups

CREATE TABLE account(
    account_id VARCHAR(255) NOT NULL,
    account_type VARCHAR(64) NOT NULL,
    balance FLOAT NOT NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- INSERT into account (account_type, balance) VALUES ('liability', -10.0)
-- INSERT into account (account_type, balance) VALUES ('revenue', 5000.0)
-- INSERT into account (account_type, balance) VALUES ('asset', 40.0)

# --- !Downs

DROP TABLE account;