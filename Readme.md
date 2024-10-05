# Supply Chain Management

This project is a comprehensive Supply Chain Management system designed to streamline and optimize the supply chain processes. It includes a backend built with Spring Boot and a frontend built with Angular. The system provides functionalities for user management, product management, and digital signature services.

## Table of Contents

- [Backend](#backend)
  - [Key Features](#key-features)
  - [Setup](#setup)
  - [Key Files](#key-files)
- [Frontend](#frontend)
  - [Key Features](#key-features-1)
  - [Setup](#setup-1)
  - [Key Files](#key-files-1)
- [Resources](#resources)
- [License](#license)
- [Contributing](#contributing)
- [Contact](#contact)

### Backend

The backend is a Spring Boot application that provides RESTful APIs for user and product management. It also includes services for digital signatures.

#### Key Features

- **User Management**: Create, update, delete, and retrieve users.
- **Product Management**: Create, update, delete, and retrieve products.
- **Digital Signature**: Generate and verify digital signatures for products.

#### Setup

1. Navigate to the `backendd` directory.
2. Build the project using Maven:
    ```sh
    ./mvnw clean install
    ```
3. Run the application:
    ```sh
    ./mvnw spring-boot:run
    ```

#### Key Files

- [`SecurityConfig.java`](../../../e:/paid_project/hashing/backendd/src/main/java/com/hashing/backend/config/SecurityConfig.java): Configuration for security, including JWT authentication and CORS settings.
- `BackendApplication.java`: Main entry point of the Spring Boot application.
- `UserService.java`: Service class for user-related operations.
- `ProductService.java`: Service class for product-related operations.

### Frontend

The frontend is an Angular application that interacts with the backend APIs to provide a user interface for managing users and products.

#### Key Features

- **User Interface**: Manage users and products through a web interface.
- **Real-time Updates**: Automatically reloads when source files change.

#### Setup

1. Navigate to the `frontend` directory.
2. Install dependencies:
    ```sh
    npm install
    ```
3. Run the development server:
    ```sh
    npm start
    ```
4. Open your browser and navigate to `http://localhost:4200/`.

#### Key Files

- `angular.json`: Angular CLI configuration file.
- `package.json`: Node.js dependencies and scripts.
- `src/app`: Main source code for the Angular application.

### Resources

- `resources/uploaded`: Directory for storing uploaded files.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any changes.

## Contact

For any questions or inquiries, please contact the project maintainer at [a.akanoun@edu.umi.ac.ma].