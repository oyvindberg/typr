---
title: OpenAPI Code Generator
sidebar_position: 1
---

# Typo OpenAPI Code Generator

Typo includes a powerful OpenAPI code generator that produces **type-safe, idiomatic code** for multiple languages and frameworks from a single OpenAPI specification.

## Cross-Language, Cross-Framework

Unlike most OpenAPI generators that target a single language, Typo generates **semantically equivalent code** across:

| Language | Server Frameworks | Client |
|----------|-------------------|--------|
| **Java** | JAX-RS, Spring Boot, Quarkus (reactive) | JDK HttpClient |
| **Kotlin** | JAX-RS, Spring Boot, Quarkus (reactive) | JDK HttpClient |
| **Scala** | Http4s, Spring Boot | Http4s, JDK HttpClient |

All generated code shares the same API contract - the same type-safe interfaces, response types, and ID wrappers work identically across all targets.

## Key Features

- **Type-safe ID wrappers** - No more primitive strings for identifiers
- **Sealed response types** - Exhaustive pattern matching for HTTP status codes
- **Server + Client generation** - Both sides share the same interface
- **Framework-native code** - Idiomatic annotations and patterns for each framework
- **Reactive support** - Mutiny `Uni<T>` for Quarkus, Cats Effect `IO` for Http4s
