DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS comments;

CREATE TABLE users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(512)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE requests
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description   VARCHAR                                 NOT NULL,
    requester_id  BIGINT REFERENCES users (id),
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY (id)
);

CREATE TABLE items
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name         VARCHAR(255)                            NOT NULL,
    description  VARCHAR                                 NOT NULL,
    is_available BOOLEAN                                 NOT NULL,
    owner_id     BIGINT REFERENCES users (id),
    request_id   BIGINT REFERENCES requests (id),
    CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE bookings
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id    BIGINT REFERENCES items (id),
    booker_id  BIGINT REFERENCES users (id),
    status     VARCHAR(9),
    CONSTRAINT pk_booking PRIMARY KEY (id)
);

CREATE TABLE comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text      VARCHAR                                 NOT NULL,
    item_id   BIGINT REFERENCES items (id),
    author_id BIGINT REFERENCES users (id),
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);