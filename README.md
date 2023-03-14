![bibernate.png](..%2F..%2FDesktop%2Fbibernate.png)

# Bibernate
**Bibernate** is a custom and simplified analog of Hibernate, a popular Object-Relational Mapping (ORM) framework. It is written in plain Java and provides basic functionality for persistence services.

The main runtime interface between a Java application and Bibernate is the Session interface. The Session class abstracts the notion of a persistence service and offers create, read, and delete operations for instances of mapped entity classes. Instances may exist in one of three states: transient, persistent, or detached. Transient instances are never persistent and not associated with any Session. Persistent instances are associated with a unique Session. Detached instances were previously persistent but are not currently associated with any Session.

The *Session* interface includes several methods for manipulating persistent data. The *flush()* method synchronizes the in-memory state of an entity with the underlying persistent store. The *persist()* method makes a transient instance persistent. The *find()* method searches for an entity of the specified class and primary key. Finally, the *remove()* method removes the entity instance.

Bibernate also provides two custom exception classes: *BibernateException* and *JDBCException*. The former is the base exception type for all Bibernate exceptions, while the latter wraps a *java.sql.SQLException* and indicates that an exception occurred during a JDBC call. The *JDBCException* class provides methods to retrieve the SQL error code and message associated with the wrapped *SQLException*.

Overall, Bibernate provides a lightweight ORM solution for basic persistence services, and its simplicity makes it easy to use for small to medium-sized projects.
