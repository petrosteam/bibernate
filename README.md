<p align="center">
  <img src="https://user-images.githubusercontent.com/43843525/224983901-7faa5077-6b41-4c9a-90ff-0567184b4e1a.png" />
</p>

# Bibernate User Guide
### Table of Contents

- [Introduction](##introduction)
- [Getting Started](#getting-started)
- [Using Bibernate](#using-bibernate)
    - [Creating a Session](#creating-a-session)
    - [Creating Entities](#creating-entities)
    - [Inserting Entities](#inserting-entities)
    - [Retrieving Entities](#retrieving-entities)
    - [Updating Entities](#updating-entities)
    - [Deleting Entities](#deleting-entities)
    - [Closing the Session](#closing-the-session)
- [Features](#features)
    - [Configuration](#configuration)
    - [Mapping](#mapping)
    - [Persistence Context](#persistence-context)
    - [Exception Handling](#exception-handling)
- [Conclusion](#conclusion)


## Introduction
**Bibernate** is a custom and simplified analog of Hibernate, a popular Object-Relational Mapping (ORM) framework. It is written in plain Java and provides basic functionality for persistence services.

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
   Additionally, make sure to include the JDBC driver for your chosen database system as a dependency in your project's *pom.xml* file. Bibernate's features have been thoroughly tested with *MySQL*, *Postgres*, *MS SQL*, and *H2* databases, but other database systems should work as well.

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
   For more details on configuration options, see the Configuration section in the Features chapter.


## Using Bibernate
### Creating a Session
Once you have a *SessionFactory*, you can create a *Session* instance:

```java
Session session = sessionFactory.openSession();
```
The openSession() method creates a new *Session* instance. You should create a new *Session* instance for each unit of work.

### Creating Entities
To persist an object using Bibernate, you must first create an entity class that maps to a database table. The entity class must have a no-argument constructor and fields annotated with the @Column and @Id annotations.

Here's an example entity class Person:

```java
@Entity
@Table("person")
public class Person {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;

    // Getters and setters
}
```
### Inserting Entities
To insert a new entity into the database, use the *Session* class's *persist()* method:

```java
Person person = new Person();
person.setName("John Smith");
person.setAge(30);
session.persist(person);
```
### Retrieving Entities
To retrieve an entity from the database, use the *Session* class's *find()* method:

```java
Person person = session.find(Person.class, 1);
```
This retrieves the Person object with the primary key value of 1.

### Updating Entities

To update an entity, simply modify the fields of the object retrieved from the database, and then call the *Session* class's *flush()* method to synchronize the in-memory state of the entity with the underlying persistent store:

```java
Person person = session.find(Person.class, 1);
person.setAge(31);
session.flush();
```
### Deleting Entities
To delete an entity, use the *Session* class's *remove()* method:

```java
Person person = session.find(Person.class, 1);
session.remove(person);
```
### Closing the Session
When you are done with a *Session*, you should close it to release the resources associated with it. To close a *Session*, use the *Session* class's *close()* method:

```java 
session.close();
```

## Features
Bibernate provides the following features:

### Configuration
Bibernate demands very little configuration effort. With a single line of code, you can create a *SessionFactory* and adjust it with the help of a properties file. The following properties can be used to configure Bibernate:

| Property Key                | Description                                 | Required | Default Value |
|-----------------------------|---------------------------------------------|----------|---------------|
| bibernate.jdbc.url          | The JDBC URL for the database connection.   | Yes      | -             |
| bibernate.jdbc.username     | The username for the database connection.   | Yes      | -             |
| bibernate.jdbc.password     | The password for the database connection.   | Yes      | -             |
| bibernate.show-sql          | Whether to show SQL statements in console.  | No       | true          |
| bibernate.jdbc.connection-pool.size | The size of the connection pool.    | No       | 10            |

### Mapping
Bibernate maps Java objects to database tables using annotations. Entities are defined using the *@Entity* annotation, and fields are mapped using the *@Column* and *@Id* annotations.
TBD
### Persistence Context
Bibernate manages the persistence context, which is the set of all entities associated with a *Session*. When you modify an entity, Bibernate automatically tracks the changes and synchronizes them with the database when necessary.
### Exception Handling
Bibernate provides two custom exception classes: *BibernateException* and *JDBCException*. The former is the base exception type for all Bibernate exceptions, while the latter wraps a *java.sql.SQLException* and indicates that an exception occurred during a JDBC call. The *JDBCException* class provides methods to retrieve the SQL error code and message associated with the wrapped *SQLException*.


## Conclusion

Bibernate offers a lightweight and efficient ORM solution for projects that require basic persistence services. Its lightweight nature makes it ideal for small to medium-sized projects. If you require advanced features or want to integrate with a specific database system, you can still consider using Hibernate or Spring Data. However, for those who seek a fast and easy-to-use ORM, Bibernate is the perfect choice.
