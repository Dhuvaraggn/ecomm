# Ecomm Project

## Overview
Ecomm is a comprehensive e-commerce platform designed to facilitate online shopping experiences. The project is structured into multiple modules, each serving a specific purpose within the e-commerce ecosystem. This README provides an overview of the project structure, its components, and instructions for setting up the environment variables.

## Project Structure

```
/Users/ajithrajs/Java/ecomm
├── ecommadmin
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   └── resources
│   │   └── test
│   └── pom.xml
├── ecommauth
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   └── resources
│   │   └── test
│   └── pom.xml
├── ecommbuyer
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   └── resources
│   │   └── test
│   └── pom.xml
└── pom.xml
```

### Modules
1. **ecommadmin**: This module handles the administrative functionalities of the e-commerce platform, including user management, product management, and order processing.
2. **ecommauth**: This module is responsible for authentication and authorization, ensuring secure access to the platform.
3. **ecommbuyer**: This module focuses on the buyer's experience, including product browsing, cart management, and checkout processes.

## Environment Variables
To configure the application, you need to set up the following environment variables:

- `DATABASE_URL`: The URL for the database connection.
- `JWT_SECRET`: The secret key used for signing JSON Web Tokens.
- `MAIL_SERVER`: The SMTP server for sending emails.
- `MAIL_PORT`: The port for the mail server.
- `MAIL_USERNAME`: The username for the mail server.
- `MAIL_PASSWORD`: The password for the mail server.

### Updating Environment Variables
1. Create a `.env` file in the root directory of each module (e.g., `ecommadmin`, `ecommauth`, `ecommbuyer`).
2. Add the required environment variables in the following format:
   ```
   DATABASE_URL=your_database_url
   JWT_SECRET=your_jwt_secret
   MAIL_SERVER=your_mail_server
   MAIL_PORT=your_mail_port
   MAIL_USERNAME=your_mail_username
   MAIL_PASSWORD=your_mail_password
   ```
3. Ensure that the `.env` file is included in your `.gitignore` to prevent sensitive information from being committed to the repository.

## Running the Project
To run the project, navigate to the desired module directory and execute the following command:
```bash
./mvnw spring-boot:run
```

## Conclusion
This README provides a high-level overview of the Ecomm project and instructions for setting up the environment variables. For further details on each module, please refer to the respective module documentation.
