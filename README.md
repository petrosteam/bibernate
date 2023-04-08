<p align="center">
  <img src="https://user-images.githubusercontent.com/43843525/224983901-7faa5077-6b41-4c9a-90ff-0567184b4e1a.png" />
</p>

# Bibernate User Guide
### Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Usage](#usage)
    - [Define your entities](#1-define-your-entities)
    - [Initialize Bibernate and perform CRUD operations](#2-initialize-bibernate-and-perform-crud-operations)
- [Configuration](#configuration)
- [Mapping](#mapping)
- [Persistence Context](#persistence-context)
- [Exception Handling](#exception-handling)
- [Conclusion](#conclusion)


## Introduction
**Bibernate** is a lightweight, easy-to-use Object Relational Mapping (ORM) library for Java. It enables developers to interact with relational databases through a simple API, abstracting away the need for writing raw SQL queries.

## Features
- Annotation-based entity configuration
- Basic CRUD operations
- Support for One-to-One, Many-to-One, and One-to-Many relationships
- Lazy loading for related entities
- Transaction management
- Custom connection pool with customizable settings
- Automatic table and column name mapping
- Customizable table and column names using annotations
- Support for primary key auto-generation strategies
- Support for @OneToOne entity relationships with @MapsId annotation
- Detailed logging for troubleshooting and performance analysis
- Flexible entity field mapping and data type conversions
- Extensive error handling and custom exceptions
- Clean and intuitive API for easy integration with existing projects
- Comprehensive Javadoc documentation for each class and method
## Prerequisites
- Java 17 or higher
- Maven or Gradle (for dependency management)

## Getting Started
To use Bibernate in your Java application, follow these steps:


1. Add the Bibernate dependency to your project's *pom.xml* file:
    ```
    <dependency>
      <groupId>com.petros.bibernate</groupId>
      <artifactId>bibernate</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    ```
   Or, if you are using Gradle, add the following dependency:

    ```
    implementation 'com.petros.bibernate:bibernate:1.0-SNAPSHOT'
    ```

   Additionally, make sure to include the JDBC driver for your chosen database system as a dependency in your project. Bibernate's features have been thoroughly tested with *MySQL*, *Postgres*, *MS SQL*, and *H2* databases, but other database systems should work as well.
**Note**: In order to build the project, you need to have Docker running because the tests utilize Testcontainers for testing against different database systems. If you don't have Docker installed or you need to build the project without running the tests, you can execute the build command with the skipTests option:
For Maven:
    ```
    mvn clean install -DskipTests
    ```
For Gradle:
    ```
    ./gradlew build -x test
    ```
2. Before using Bibernate, you need to create a *SessionFactory* instance. The *SessionFactory* is a thread-safe object that creates *Session* instances. To create a *SessionFactory*, you can use one of the following approaches:

   * Calling the *Persistence.createSessionFactory()* method without arguments to use the default configuration specified in the *src/main/resources/application.properties* file:
   
   ```java
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    ```
   
    * Passing a custom configuration file path to the *Persistence.createSessionFactory(configPath)* method:
   
    ```java
    SessionFactory sessionFactory = Persistence.createSessionFactory("/path/to/custom/config.properties");
    ```
   
   * Providing the necessary parameters to build a *DataSource* object by calling the *Persistence.createSessionFactory(url, username, password)* method:
   
    ```java
    SessionFactory sessionFactory = Persistence.createSessionFactory("jdbc:mysql://localhost:3306/mydatabase", "myuser", "mypassword");
    ```
   
   For more details on configuration options, see the [Configuration](#configuration) section in the Features chapter.


## Usage
### 1. Define your entities
Create Java classes that represent your database tables and add the necessary annotations:

```java
@Entity
@Table("users")
@Data
public class User {

    @Id
    @GeneratedValue
    @Column("id")
    private Integer id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    // Getters and setters
}
```
**Note**: A no-argument constructor is required for Bibernate to work with your entities. Make sure to include one in each of your entity classes.
### 2. Initialize Bibernate and perform CRUD operations

```java
public class Main {
    public static void main(String[] args) {
        // Initialize Bibernate
        SessionFactory sessionFactory = Persistence.createSessionFactory();

        // Create a new user
        User user = new User();
        user.setUsername("JohnDoe");
        user.setEmail("john.doe@example.com");

        // Save the user to the database
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            session.persist(user);
            transaction.commit();
        }

        // Retrieve the user by ID
        try (Session session = sessionFactory.openSession()) {
            User retrievedUser = session.find(User.class, 1);
            System.out.println("Retrieved user: "  + retrievedUser.getUsername());
        }

        // Update the user's email
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            User retrievedUser = session.find(User.class, 1);
            retrievedUser.setEmail("updated.email@example.com");
            transaction.commit();
        }

        // Delete the user
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            User retrievedUser = session.find(User.class, 1);
            session.remove(retrievedUser);
            transaction.commit();
        }

        sessionFactory.close();
    }
}
```

## Configuration
Bibernate demands very little configuration effort. With a single line of code, you can create a *SessionFactory* and adjust it with the help of a properties file. The following properties can be used to configure Bibernate:

| Property Key                | Description                                 | Required | Default Value |
|-----------------------------|---------------------------------------------|----------|---------------|
| bibernate.jdbc.url          | The JDBC URL for the database connection.   | Yes      | -             |
| bibernate.jdbc.username     | The username for the database connection.   | Yes      | -             |
| bibernate.jdbc.password     | The password for the database connection.   | Yes      | -             |
| bibernate.show-sql          | Whether to show SQL statements in console.  | No       | true          |
| bibernate.jdbc.connection-pool.size | The size of the connection pool.    | No       | 10            |

## Mapping
Bibernate maps Java objects to database tables using annotations. Entities are defined using the *@Entity* annotation, and fields are mapped using the *@Column* and *@Id* annotations. Relationships between entities can be defined using *@OneToOne*, *@OneToMany*, and *@ManyToOne* annotations.
## Persistence Context
Bibernate manages the persistence context, which is the set of all entities associated with a *Session*. When you modify an entity, Bibernate automatically tracks the changes and synchronizes them with the database when necessary.
## Exception Handling
Bibernate provides two custom exception classes: *BibernateException* and *JDBCException*. The former is the base exception type for all Bibernate exceptions, while the latter wraps a *java.sql.SQLException* and indicates that an exception occurred during a JDBC call. The *JDBCException* class provides methods to retrieve the SQL error code and message associated with the wrapped *SQLException*.


## Conclusion

Bibernate offers a lightweight and efficient ORM solution for projects that require basic persistence services. Its lightweight nature makes it ideal for small to medium-sized projects. If you require advanced features or want to integrate with a specific database system, you can still consider using Hibernate or Spring Data. However, for those who seek a fast and easy-to-use ORM, Bibernate is the perfect choice.
