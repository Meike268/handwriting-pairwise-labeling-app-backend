# Handwriting Labeling App Backend

## What is this project for?
This is the Kotlin Spring backend of the Handwriting Labeling App.
This app serves to collect labels on handwriting legibility from annotators.
This project is part of research in the direction of automatic handwriting assessment.
It was developed by Aaron Lukas Pieger and Erik Jonathan Schmidt as part of our Master Thesis projects.

## Functionality of the Application
This app was developed to carry out the data labeling for a handwriting legibility dataset.
We collect scores from "very good" 1 to "very bad" 5 for four questions about the legibility of
handwritten sentences.  
This application provides the REST API the [frontend web app](https://github.com/LukasPieger1/handwriting-labeling-app-frontend) operates on.   

### User/Frontend Application endpoints
- POST **/users/login** The backend provides an authentication endpoint that verifies users against user accounts saved in the project MySQL DB.
- GET **/batch** Provides authenticated users with a batch of tasks `(sampleId, questionId)`. The backend sends the question and the images(samples) to annotate.
- POST **/answers** Answer `(sampleId, questionId, userId, label)` is sent to the backend to persist new answer in the DB.
- PUT **/answers** Answer `(sampleId, questionId, userId, label)` is sent to the backend to update existing answer in DB.
- POST **/reports** Report `(sampleId, userId, message` is sent when report window of frontend is used.
### Admin operational endpoints
The application provides endpoints to carry out different operational tasks with admin credentials.

- GET **/users** Get a list of all users and their roles
- POST **/users** Create and persist a new user with the given `username`, `password` and `userRoles`
- GET **/config** Retrieve the current batch_service_config.
- POST **/config** Update(overwrite) the batch_service_config. This changes the behaviour of the application.
- GET **/answers** Export answer entries from the DB in json format.
- DELETE **/answers/ofSample/{id}** Deletes all answers to questions about the give sample/image. 
- GET **/reports** Get exports stored in DB.

#### batch_service_config
The [batch_service_config](src/main/resources/batch_service_config.json) determines:
- how many answers to collect per task (question and image)
- how many answers from experts are needed per task
- the number of tasks per batch
- which question to prioritize
- samples of which sentence to prioritize
This allows to adjust the labeling process while the application is running.
The config is excluded from Git. The [provide_defaults](provide-defaults.sh) script creates
a default config if non is available.


## How to run
To run the backend spring application
1. Setup MySQL database
2. Setup project and provide env variables
3. Put images in
4. Build and run

### Setup MySQL database
By default, the backend operates on a MySql database with name `handwriting_labeling_app`.  
Refer to https://dev.mysql.com/doc/mysql-getting-started/en/ to set up MySQL on your machine.
Once installed run MySQL `mysql.server start`.
Login as root `mysql -u root -p`. Your terminal is now prefixed with `mysql` to denote you're in the mysql CLI.
Create the empty database `mysql> CREATE database handwriting_labeling_app;`.
Verify mysql serves on the right port `mysql> SHOW GLOBAL VARIABLES LIKE 'PORT';`.
By default, the backend expects mysql on port `3306`.

### Setup project
With the mysql database created we can initialize/setup the project.  
On first set up you need to adjust the [provide_defaults](provide-defaults.sh) and enter
username and password for your MySQL database.
Then run [provide_defaults](provide-defaults.sh) to create a `.env` that stores:
- MYSQL_HOST
- MYSQL_PORT
- APP_URL_ROOTS
- MYSQL_DATABASE
- MYSQL_USERNAME _no default -> needs to be set_
- MYSQL_PASSWORD _no default -> needs to be set_

### Add images
The backend expects a folder structure with subfolders of images to operate on.
The image names are used as `sampleId`s and are expected to be unique.
Images at:
"./src/main/resources/public/files/images/samples"

The content of `./src/main/resources/public/files/` is excluded from Git. So the folder structure needs to be created
(or better the dataset folder is pasted in here).

```
./src/main/resources/public/files/
└── images
    ├── examples
    │   ├── example_image_letter_alignment.png
    │   ├── example_image_letter_size_ad.png
    │   ├── example_image_letter_size_rnh.png
    │   └── example_image_overall_legibility.png
    └── samples
        └── xai_sentences
            ├── 1
            │   ├── <image1_sentence1_id>.png
            │   ├── <image2_sentence1_id>.png
            │   ├── ...
            │   └── <imageN_sentence1_id>.png
            ├── 2
            │   ├── <image1_sentence2_id>.png
            │   ├── ...
            │   └── <imageN_sentence2_id>.png
            └── ...
```

### Build and run application
- Make sure you're running JDK-version 21.
- Build and run the application with maven 
```bash
source .env && ./mvnw spring-boot:run
```

By default, there are two users created when the backend is first started:
```
username: "admin" password: "LyVvnz1a"
username: "testuser" password: "kDpRNnrI"
```
If you want to make the app available to others it would be a good idea to remove these default users from the database.