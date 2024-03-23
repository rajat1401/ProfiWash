# --- !Ups

CREATE TABLE account_transaction (
     transaction_id VARCHAR(255) NOT NULL,
     account_dr_id VARCHAR(255) NOT NULL,
     account_cr_id VARCHAR(255) NOT NULL,
     timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     amount FLOAT NOT NULL,
     state VARCHAR(64) not null,
     PRIMARY KEY(transaction_id),
     INDEX (timestamp,account_dr_id),
     INDEX (timestamp,account_cr_id),
     FOREIGN KEY (account_dr_id) references account(account_id) ON UPDATE CASCADE ON DELETE CASCADE,
     FOREIGN KEY (account_cr_id) references account(account_id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


# --- !Downs

DROP TABLE account_transaction;