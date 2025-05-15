CREATE TABLE `user`
(
    `id`       bigint      NOT NULL AUTO_INCREMENT,
    `username` varchar(64) NOT NULL,
    `password` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `user`
VALUES (1, 'admin', '$2a$12$sLfWzlAxlno0VydU5xD4ZuOyWoqn/RbdlM0aRoZtabAgxisGn1fly'),
       (2, 'testuser', '$2a$12$z27DG7CIIUZEAaasLap1Ge9qiK9YOWUa168UHSrgNr.sHm4TXUx.W');

CREATE TABLE `role`
(
    `id`   bigint       NOT NULL AUTO_INCREMENT,
    `name` varchar(128) NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `role`
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER'),
       (3, 'ROLE_EXPERT');

CREATE TABLE `user_role`
(
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
);
INSERT INTO `user_role`(`user_id`, `role_id`)
VALUES (1, 1), (1, 2), (2, 2);

CREATE TABLE `question`
(
    `id`             bigint    NOT NULL AUTO_INCREMENT,
    `description`    text      NOT NULL,
    `example_image_name` text NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `question`(`id`, `description`, `example_image_name`)
VALUES (1, 'overall-legibility', 'example_image_overall_legibility.png'),
       (2, 'letter-alignment', 'example_image_letter_alignment.png'),
       (3, 'letter-size_rnh', 'example_image_letter_size_rnh.png'),
       (4, 'letter-size_ad', 'example_image_letter_size_ad.png');

CREATE TABLE `reference_sentence`
(
    `id`             bigint    NOT NULL AUTO_INCREMENT,
    `content`        text      NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `reference_sentence`(`id`, `content`)
VALUES (1, 'Der Hahn und der Hund tanzen.'),
       (2, 'Er stand da und lauschte.'),
       (3, 'Hannah hat ein Buch gelesen.'),
       (4, 'Quark ist besonders lecker.'),
       (5, 'David der Kater schnurrt sanft.'),
       (6, 'Die Pförtner lassen dich herein.'),
       (7, 'Sie wanderten richtung Strand.'),
       (8, 'Kinder spielen draußen.'),
       (9, 'Ein heftiger Blitz leuchtet.'),
       (10, 'Bellende Hunde beißen nicht.');

CREATE TABLE `applicable_question`
(
    `reference_sentence_id` bigint  NOT NULL,
    `question_id`           bigint  NOT NULL,
    PRIMARY KEY (`reference_sentence_id`, `question_id`),
    FOREIGN KEY (`reference_sentence_id`) REFERENCES `reference_sentence` (`id`),
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
);
INSERT INTO `applicable_question`(`reference_sentence_id`, `question_id`)
VALUES (1, 1), (2, 1), (3, 1),(4, 1),(6, 1),(7, 1),(8, 1),(9, 1),(10, 1),
       (1, 2),(2, 2),(3, 2),(4, 2),(6, 2),(7, 2),(8, 2),(9, 2),(10, 2),
       (1, 3),(2, 3),(3, 3),(4, 3),(6, 3),(7, 3),(8, 3),(9, 3),(10, 3),
       (1, 4),(2, 4),(6, 4),(7, 4),(8, 4);

CREATE TABLE `answer` (
    `user_id` bigint NOT NULL,
    `sample_id_1` bigint NOT NULL,
    `sample_id_2` bigint NOT NULL,
    `question_id` bigint NOT NULL,
    `score` tinyint NOT NULL,
    `submission_timestamp` timestamp NOT NULL ,
    PRIMARY KEY (`user_id`, `sample_id`, `question_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
);

CREATE TABLE `report` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id`  bigint NOT NULL,
    `sample_id_1`  bigint,
    `sample_id_2`  bigint,
    `question_id` bigint,
    `message`  bigint NOT NULL,
    `submission_timestamp` timestamp NOT NULL,
    PRIMARY KEY (`id`)
)