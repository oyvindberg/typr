---
title: IoC Framework Integration
---

Typo can generate annotations for IoC (Inversion of Control) frameworks, making your repositories easy to inject with dependency injection.

## Configuration

Set the `iocFramework` option to enable annotation generation:

```scala
import typo.*

// For Spring
val options = Options(
  pkg = "org.foo",
  lang = LangScala(Dialect.Scala3, TypeSupportScala),
  dbLib = Some(DbLibName.Doobie),
  iocFramework = Some(IocFramework.Spring)
)

// For Jakarta CDI (Quarkus, WildFly, etc.)
val javaOptions = Options(
  pkg = "org.foo",
  lang = LangJava,
  dbLib = Some(DbLibName.Typo),
  iocFramework = Some(IocFramework.JakartaCdi)
)
```

## Supported Frameworks

### Spring

When `IocFramework.Spring` is configured, Typo adds:

- `@Repository` annotation on `RepoImpl` classes (main scope)
- `@Repository` annotation on `TestInsert` classes (test scope)

**Generated code example:**

```scala
import org.springframework.stereotype.Repository

@Repository
class AddressRepoImpl extends AddressRepo {
  // ...
}
```

This enables automatic component scanning and dependency injection:

```scala
@Service
class AddressService @Autowired() (addressRepo: AddressRepo) {
  // addressRepo is automatically injected
}
```

### Jakarta CDI (Quarkus)

When `IocFramework.JakartaCdi` is configured, Typo adds:

- `@ApplicationScoped` annotation on `RepoImpl` classes (main scope)
- `@Singleton` annotation on `TestInsert` classes (test scope)

**Generated code example:**

```java
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AddressRepoImpl implements AddressRepo {
  // ...
}
```

This enables CDI injection in Quarkus and other Jakarta EE environments:

```java
@ApplicationScoped
public class AddressService {
  @Inject
  AddressRepo addressRepo;

  // addressRepo is automatically injected
}
```

## Dependencies

You'll need to add the appropriate dependency to your project:

**Spring:**
```
org.springframework:spring-context:6.1.x
```

**Jakarta CDI:**
```
jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.x
```

## Notes

- Mock repositories do not currently receive IoC annotations due to their constructor requirements
- The annotations are only added when `iocFramework` is explicitly set (default is `None`)
- Works with both Scala and Java code generation
