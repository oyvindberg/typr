import React from "react";
import clsx from "clsx";
import CodeBlock from "@theme/CodeBlock";
import { useLanguage } from "../LanguageContext";
import styles from "./styles.module.css";

const FeatureList = [
    {
        title: "SQL First",
        icon: "icon-sql",
        description: (
            <>
                Write SQL in <code>.sql</code> files with full IDE support. No ORMs or query builders required - just pure SQL with type-safe parameters.
            </>
        ),
        codeExample: {
            sql: `-- users.sql
SELECT * FROM users
WHERE email = :email!`
        },
        codeLanguage: "sql",
    },
    {
        title: "Type-Safe Everything",
        icon: "icon-lock",
        description: (
            <>
                Complete type safety from database to application. Foreign keys become specific ID types, nullable columns become optional types.
            </>
        ),
        codeExample: {
            scala: `case class UserId(value: Long)
case class User(
  id: UserId,
  email: String,
  name: Option[String]
)`,
            java: `public record UserId(Long value) {}
public record User(
  UserId id,
  String email,
  Optional<String> name
) {}`,
            kotlin: `@JvmInline
value class UserId(val value: Long)
data class User(
  val id: UserId,
  val email: String,
  val name: String?
)`
        },
    },
    {
        title: "Zero Boilerplate",
        icon: "icon-bolt",
        description: (
            <>
                Generates repositories with CRUD operations, streaming queries, and batch inserts. Works with Anorm, Doobie, ZIO-JDBC, and plain JDBC.
            </>
        ),
        codeExample: {
            scala: `UserRepo.insert(user)
UserRepo.selectById(userId)
UserRepo.updateEmail(userId, email)
UserRepo.selectAll.stream`,
            java: `userRepo.insert(user, connection);
userRepo.selectById(userId, connection);
userRepo.update(user, connection);
userRepo.selectAll(connection);`,
            kotlin: `userRepo.insert(user, connection)
userRepo.selectById(userId, connection)
userRepo.update(user, connection)
userRepo.selectAll(connection)`
        },
    },
    {
        title: "Functional Relational Mapping",
        icon: "icon-rocket",
        description: (
            <>
                Not an ORM - it's FRM. Maps your database schema to immutable data classes without runtime overhead or magic. Fast compilation, zero reflection.
            </>
        ),
        codeExample: {
            scala: `// FRM: Pure functions over data
// vs ORM: Complex object hierarchies
// vs hand-written SQL: Verbose boilerplate
// vs jOOQ: Better testing story`,
            java: `// FRM: Pure functions over records
// vs ORM: Complex object hierarchies
// vs hand-written SQL: Verbose boilerplate
// vs jOOQ: Better testing story`,
            kotlin: `// FRM: Pure functions over data classes
// vs ORM: Complex object hierarchies
// vs hand-written SQL: Verbose boilerplate
// vs jOOQ: Better testing story`
        },
    },
    {
        title: "Stream Like a Pro",
        icon: "icon-wave",
        description: (
            <>
                Built-in streaming support for large datasets. Process millions of rows without breaking a sweat using your favorite streaming library.
            </>
        ),
        codeExample: {
            scala: `// Stream millions of rows efficiently
UserRepo.selectAll.stream
  .filter(_.active)
  .mapAsync(enrichUser)
  .runWith(Sink.foreach(process))`,
            java: `// Stream millions of rows efficiently
userRepo.selectAll(connection).stream()
  .filter(User::active)
  .map(this::enrichUser)
  .forEach(this::process);`,
            kotlin: `// Stream millions of rows efficiently
userRepo.selectAll(connection)
  .filter { it.active }
  .map { enrichUser(it) }
  .forEach { process(it) }`
        },
    },
    {
        title: "Powerful Query DSL",
        icon: "icon-target",
        description: (
            <>
                Optional type-safe DSL for complex queries. Build dynamic queries with compile-time guarantees and autocomplete support.
            </>
        ),
        codeExample: {
            scala: `userRepo.select
  .join(postRepo.select)
  .on((u, p) => u.id === p.userId)
  .where { case (u, _) => u.email.like("%@typr%") }
  .orderBy { case (u, _) => u.createdAt.desc }
  .limit(10)`,
            java: `userRepo.select()
  .join(postRepo.select())
  .on((u, p) -> u.id().eq(p.userId()))
  .where((u, p) -> u.email().like("%@typr%"))
  .orderBy((u, p) -> u.createdAt().desc())
  .limit(10)`,
            kotlin: `userRepo.select()
  .join(postRepo.select())
  .on { u, p -> u.id.eq(p.userId) }
  .where { u, p -> u.email.like("%@typr%") }
  .orderBy { u, p -> u.createdAt.desc() }
  .limit(10)`
        },
    },
];

function Feature({icon, title, description, codeExample, codeLanguage, index}) {
    const { language } = useLanguage();

    // Determine what code and language to show
    let displayCode;
    let displayLanguage;

    if (codeLanguage === "sql") {
        // SQL code is language-agnostic
        displayCode = codeExample.sql;
        displayLanguage = "sql";
    } else if (codeExample[language]) {
        displayCode = codeExample[language];
        displayLanguage = language;
    } else {
        displayCode = codeExample.scala;
        displayLanguage = "scala";
    }

    return (
        <div className={styles.featureCard} style={{"--index": index}}>
            <div>
                <h3 className={styles.featureTitle}>{title}</h3>
                <div className={styles.featureDescription}>{description}</div>
                {displayCode && (
                    <div className={styles.codeExample}>
                        <CodeBlock language={displayLanguage} className={styles.codeBlock}>
                            {displayCode}
                        </CodeBlock>
                    </div>
                )}
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <h2 className={styles.featuresTitle}>Why Developers Love Typr</h2>
                <div className={styles.featureGrid}>
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} index={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
