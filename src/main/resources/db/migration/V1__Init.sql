CREATE TABLE `user`
(
    `id`       bigint      NOT NULL AUTO_INCREMENT,
    `username` varchar(64) NOT NULL,
    `password` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `user`
VALUES (1, 'admin', '$2a$12$/ROqUVGeB02S6s6/UoDHGum6wsRaNLjxrwsKVr6EttysAGXXYtB7.'),
       (2, 'user', '$2a$12$Fcuwcf6c96qMRn/kgmnBKunye.DSR3gk74NucVEUu0DpJRR6g6rt6'),
       (3, 'expert', '$2a$12$xuAaet2fbf51Neucdh0HseTf4IGLOGJhxR.qAQ413uCTQFVSPg44W');

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
VALUES (1, 1), (1, 2), (2, 2), (3, 2), (3, 3);

CREATE TABLE `question`
(
    `id`             bigint    NOT NULL AUTO_INCREMENT,
    `description`    text      NOT NULL,
    PRIMARY KEY (`id`)
);
INSERT INTO `question`(`id`, `description`)
VALUES (1, 'overall legibility'),
       (2, 'inclination'),
       (3, 'r, n, h different'),
       (4, 'a, d different'),
       (5, 'e, l different');

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
VALUES (1, 1), (2, 1), (3, 1),(4, 1),(5, 1),(6, 1),(7, 1),(8, 1),(9, 1),(10, 1),
       (1, 2),(2, 2),(3, 2),(4, 2),(5, 2),(6, 2),(7, 2),(8, 2),(9, 2),(10, 2),
       (1, 3),(2, 3),(3, 3),(4, 3),(5, 3),(6, 3),(7, 3),(8, 3),(9, 3),(10, 3),
       (1, 4),(2, 4),(5, 4),(6, 4),(7, 4),(8, 4),
       (2, 5),(3, 5),(4, 5),(6, 5),(8, 5),(9, 5),(10, 5);

CREATE TABLE `examples`
(
    `positive_image_path`    text    NOT NULL,
    `negative_image_path`    text    NOT NULL,
    `reference_sentence_id`  bigint  NOT NULL,
    `question_id`            bigint  NOT NULL,
    PRIMARY KEY (`reference_sentence_id`, `question_id`),
    FOREIGN KEY (`reference_sentence_id`) REFERENCES `reference_sentence` (`id`),
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
);

CREATE TABLE `sample`
(
    `id`            bigint  NOT NULL AUTO_INCREMENT,
    `sentence_id`   bigint  NOT NULL,
    `image_path`    text    NOT NULL,
    `student_id`    bigint  NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `answer` (
    `user_id` bigint NOT NULL,
    `sample_id` bigint NOT NULL,
    `question_id` bigint NOT NULL,
    `score` tinyint NOT NULL,
    PRIMARY KEY (`user_id`, `sample_id`, `question_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`sample_id`) REFERENCES `sample` (`id`),
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
);