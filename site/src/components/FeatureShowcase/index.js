import React from "react";
import Link from "@docusaurus/Link";
import CodeBlock from "@theme/CodeBlock";
import { useLanguage } from "../LanguageContext";
import { useGeneratedCode } from "../../hooks/useGeneratedCode";
import styles from "./styles.module.css";

// Helper to get code for current language, with Kotlin falling back to Java
function useCode(codeMap) {
  const { language } = useLanguage();
  if (!codeMap) return { code: null, lang: language };

  if (language === "kotlin") {
    // Fall back to Java for Kotlin since we don't generate Kotlin yet
    return { code: codeMap.java || codeMap.scala, lang: "java" };
  }
  return { code: codeMap[language] || codeMap.scala, lang: language };
}

// Component to display generated code with optional extraction
function GeneratedCodeBlock({ fileKey, extract }) {
  const { language } = useLanguage();
  const generatedCode = useGeneratedCode();

  const codeFile = generatedCode[fileKey];
  if (!codeFile) return null;

  let code = language === "kotlin" ? codeFile.java : codeFile[language];
  code = code || codeFile.scala;

  if (code && extract) {
    code = extract(code, language);
  }

  if (!code) return null;

  return (
    <CodeBlock language={language === "kotlin" ? "java" : language}>
      {code}
    </CodeBlock>
  );
}

// Extract case class/record definition only (without companion object)
function extractRowDefinition(code, language) {
  const lines = code.split('\n');
  const result = [];
  let inDefinition = false;
  let braceDepth = 0;
  let parenDepth = 0;

  for (const line of lines) {
    // Start capturing at case class, record, or data class
    if (!inDefinition) {
      if (line.includes('case class ') || line.includes('public record ') || line.includes('data class ')) {
        inDefinition = true;
      }
    }

    if (inDefinition) {
      result.push(line);

      // Track brace and paren depth
      for (const char of line) {
        if (char === '(') parenDepth++;
        if (char === ')') parenDepth--;
        if (char === '{') braceDepth++;
        if (char === '}') braceDepth--;
      }

      // For Scala case class without body, end at closing paren
      if (language === 'scala' && parenDepth === 0 && !line.includes('{')) {
        break;
      }

      // For Java record, end at closing brace after the record body starts
      if (language === 'java' && braceDepth === 0 && result.length > 1 && line.includes('}')) {
        break;
      }
    }
  }

  return result.join('\n');
}

// Extract trait/interface definition only
function extractRepoInterface(code, language) {
  const lines = code.split('\n');
  const result = [];
  let inDefinition = false;
  let braceDepth = 0;

  for (const line of lines) {
    if (!inDefinition) {
      if (line.includes('trait ') || line.includes('public interface ')) {
        inDefinition = true;
      }
    }

    if (inDefinition) {
      result.push(line);

      for (const char of line) {
        if (char === '{') braceDepth++;
        if (char === '}') braceDepth--;
      }

      if (braceDepth === 0 && result.length > 1) {
        break;
      }
    }
  }

  return result.join('\n');
}

// Component for features that show SQL + generated code
function FeatureWithGeneratedCode({ title, description, sqlCode, fileKey, extract, docs }) {
  return (
    <div className={styles.featureCard}>
      <div className={styles.featureHeader}>
        <h4 className={styles.featureTitle}>{title}</h4>
        <p className={styles.featureDescription}>{description}</p>
      </div>
      {sqlCode && (
        <CodeBlock language="sql">{sqlCode}</CodeBlock>
      )}
      <GeneratedCodeBlock fileKey={fileKey} extract={extract} />
      <div className={styles.featureFooter}>
        <Link className={styles.featureLink} to={docs}>Learn More</Link>
      </div>
    </div>
  );
}

// Component for features with inline code examples
function FeatureWithInlineCode({ title, description, sqlCode, code, docs }) {
  const { code: displayCode, lang } = useCode(code);

  return (
    <div className={styles.featureCard}>
      <div className={styles.featureHeader}>
        <h4 className={styles.featureTitle}>{title}</h4>
        <p className={styles.featureDescription}>{description}</p>
      </div>
      {sqlCode && (
        <CodeBlock language="sql">{sqlCode}</CodeBlock>
      )}
      {displayCode && (
        <CodeBlock language={lang}>{displayCode}</CodeBlock>
      )}
      <div className={styles.featureFooter}>
        <Link className={styles.featureLink} to={docs}>Learn More</Link>
      </div>
    </div>
  );
}

const features = [
  {
    category: "All The Boilerplate, None Of The Work",
    items: [
      {
        type: "generated",
        title: "From Database Schema to Complete Code",
        description: "Point Typr at your PostgreSQL database and watch it generate everything: data classes, repositories, type-safe IDs, JSON codecs, and test helpers. No manual mapping code ever again.",
        sqlCode: `-- Your PostgreSQL schema
CREATE TABLE frontpage.user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email frontpage.email NOT NULL UNIQUE,
  name TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  department_id UUID REFERENCES frontpage.department(id),
  status frontpage.user_status DEFAULT 'active',
  verified BOOLEAN DEFAULT false
);`,
        fileKey: "user/UserRow",
        extract: extractRowDefinition,
        docs: "/docs/setup"
      },
      {
        type: "generated",
        title: "Complete Repository Interface",
        description: "Get full repositories with CRUD operations, batch operations, upserts, streaming inserts, and DSL support. All generated, all type-safe.",
        fileKey: "user/UserRepo",
        extract: extractRepoInterface,
        docs: "/docs/what-is/relations"
      }
    ]
  },
  {
    category: "Relationships Become Navigation",
    items: [
      {
        type: "generated",
        title: "Foreign Keys Create Type-Safe References",
        description: "Every foreign key in your database automatically generates specific ID types. Your schema relationships become first-class code citizens.",
        sqlCode: `-- Database relationships
CREATE TABLE frontpage.order (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES frontpage.user(id),
  product_id UUID REFERENCES frontpage.product(id),
  status frontpage.order_status DEFAULT 'pending',
  total DECIMAL(10,2) NOT NULL
);`,
        fileKey: "order/OrderRow",
        extract: extractRowDefinition,
        docs: "/docs/what-is/relations"
      },
      {
        type: "inline",
        title: "Type-Safe Foreign Key Navigation with DSL",
        description: "Typr's DSL provides joinFk for easy type-safe navigation through foreign key relationships. Your IDE knows exactly what's available at each level.",
        code: {
          scala: `// Navigate through foreign keys with perfect type safety:
val query = orderRepo.select
  .joinFk(_.fkUser)(userRepo.select)
  .joinFk { case (o, _) => o.fkProduct }(productRepo.select)
  .where { case ((order, user), product) =>
    order.status === OrderStatus.active.? &&
    user.verified === true.? &&
    product.inStock === true.?
  }
  .orderBy { case ((order, _), _) => order.createdAt.desc }
  .limit(100)
  .toList

// Each joinFk automatically uses the foreign key constraint
// No manual ON clauses needed - Typr knows the relationships!`,
          java: `// Navigate through foreign keys with perfect type safety:
var query = orderRepo.select()
  .joinFk(OrderFields::fkUser, userRepo.select())
  .joinFk((o, u) -> o.fkProduct(), productRepo.select())
  .where((order, user, product) ->
    order.status().eq(Optional.of(OrderStatus.active)).and(
    user.verified().eq(Optional.of(true))).and(
    product.inStock().eq(Optional.of(true))))
  .orderBy((order, user, product) -> order.createdAt().desc())
  .limit(100)
  .toList(connection);`
        },
        docs: "/docs/other-features/dsl-in-depth"
      }
    ]
  },
  {
    category: "Type Safety Revolution",
    items: [
      {
        type: "generated",
        title: "Strongly-Typed Primary Keys",
        description: "Every table gets its own ID type. No more mixing up User IDs and Product IDs - the compiler catches mistakes.",
        fileKey: "user/UserId",
        docs: "/docs/type-safety/id-types"
      },
      {
        type: "generated",
        title: "PostgreSQL Domain Types",
        description: "Full support for PostgreSQL domains with constraint documentation in your generated code.",
        sqlCode: `-- Database domain
CREATE DOMAIN frontpage.email AS TEXT
  CHECK (VALUE ~ '^[^@]+@[^@]+\\.[^@]+$');`,
        fileKey: "Email",
        docs: "/docs/type-safety/domains"
      },
      {
        type: "generated",
        title: "Composite Primary Keys",
        description: "First-class support for composite primary keys with generated helper types.",
        sqlCode: `-- Composite key table
CREATE TABLE frontpage.user_permission (
  user_id UUID REFERENCES frontpage.user(id),
  permission_id UUID REFERENCES frontpage.permission(id),
  granted_at TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (user_id, permission_id)
);`,
        fileKey: "user_permission/UserPermissionRow",
        extract: extractRowDefinition,
        docs: "/docs/type-safety/id-types#composite-keys"
      }
    ]
  },
  {
    category: "The Perfect DSL For Real-World Data Access",
    items: [
      {
        type: "inline",
        title: "Incredibly Easy To Work With",
        description: "A pragmatic DSL that makes everyday data operations a breeze. Perfect IDE support with autocomplete, inline documentation, and compile-time validation.",
        code: {
          scala: `// Fetch exactly the data you need with type-safe joins
val activeOrders = orderRepo.select
  .join(customerRepo.select)
  .on((o, c) => o.userId === c.userId.?)
  .join(productRepo.select)
  .on { case ((o, _), p) => o.productId === p.id.? }
  .where { case ((order, _), _) => order.status === OrderStatus.active.? }
  .where { case ((_, customer), _) => customer.verified === true.? }
  .orderBy { case ((order, _), _) => order.createdAt.desc }
  .limit(100)
  .toList

// Update with complex conditions
productRepo.update
  .set(_.inStock, Some(false))
  .where(_.quantity === 0.?)
  .execute

// Delete with conditions
orderItemRepo.delete
  .where(_.orderId.in(cancelledOrderIds))
  .where(_.shippedAt.isNull)
  .execute`,
          java: `// Fetch exactly the data you need with type-safe joins
var activeOrders = orderRepo.select()
  .join(customerRepo.select())
  .on((o, c) -> o.userId().eq(c.userId()))
  .join(productRepo.select())
  .on((oc, p) -> oc.first().productId().eq(Optional.of(p.id())))
  .where((order, customer, product) ->
    order.status().eq(Optional.of(OrderStatus.active)))
  .where((order, customer, product) ->
    customer.verified().eq(Optional.of(true)))
  .orderBy((order, customer, product) -> order.createdAt().desc())
  .limit(100)
  .toList(connection);

// Update with complex conditions
productRepo.update()
  .set(ProductFields::inStock, Optional.of(false))
  .where(p -> p.quantity().eq(Optional.of(0)))
  .execute(connection);

// Delete with conditions
orderItemRepo.delete()
  .where(oi -> oi.orderId().in(cancelledOrderIds))
  .where(oi -> oi.shippedAt().isNull())
  .execute(connection);`
        },
        docs: "/docs/other-features/dsl-in-depth"
      }
    ]
  },
  {
    category: "Testing Excellence",
    items: [
      {
        type: "inline",
        title: "TestInsert: Build Valid Data Graphs",
        description: "Generate complete object graphs with valid foreign key relationships. All fields are random by default, but you override exactly what your test cares about.",
        code: {
          scala: `val testInsert = new TestInsert(new Random(42), domainInsert)

// Build a complete, valid data graph
val company = testInsert.frontpageCompany()
val department = testInsert.frontpageDepartment(companyId = company.id)
val manager = testInsert.frontpageUser(
  departmentId = Some(department.id),
  role = Defaulted.Provided(Some(UserRole.manager))
)
val employees = List.fill(5)(
  testInsert.frontpageUser(
    departmentId = Some(department.id),
    managerId = Some(manager.id),
    role = Defaulted.Provided(Some(UserRole.employee))
  )
)

// Every foreign key is valid!
// All other fields are realistic random data!`,
          java: `var testInsert = new TestInsert(new Random(42), domainInsert);

// Build a complete, valid data graph
var company = testInsert.frontpageCompany(connection);
var department = testInsert.frontpageDepartment(connection,
  Optional.of(company.id()));
var manager = testInsert.frontpageUser(connection,
  Optional.of(department.id()),
  Defaulted.provided(Optional.of(UserRole.manager)));
var employees = IntStream.range(0, 5)
  .mapToObj(i -> testInsert.frontpageUser(connection,
    Optional.of(department.id()),
    Optional.of(manager.id()),
    Defaulted.provided(Optional.of(UserRole.employee))))
  .toList();

// Every foreign key is valid!
// All other fields are realistic random data!`
        },
        docs: "/docs/other-features/testing-with-random-values"
      },
      {
        type: "inline",
        title: "In-Memory Repository Stubs",
        description: "Drop-in repository replacements that work entirely in memory. Full DSL support including complex joins - your business logic runs unchanged.",
        code: {
          scala: `// Replace real repos with in-memory stubs
val userRepo = UserRepoMock.empty
val orderRepo = OrderRepoMock.empty
val productRepo = ProductRepoMock.empty

// Seed with test data
userRepo.insertUnsaved(testUsers*)
orderRepo.insertUnsaved(testOrders*)

// Complex queries work in memory!
val topCustomers = userRepo.select
  .join(orderRepo.select)
  .on((u, o) => u.id === o.userId.?)
  .where { case (user, _) => user.status === UserStatus.active.? }
  .limit(50)
  .toList

// Runs instantly, no database needed!
// Same code as production database queries!`,
          java: `// Replace real repos with in-memory stubs
var userRepo = UserRepoMock.empty();
var orderRepo = OrderRepoMock.empty();
var productRepo = ProductRepoMock.empty();

// Seed with test data
userRepo.insertUnsaved(testUsers);
orderRepo.insertUnsaved(testOrders);

// Complex queries work in memory!
var topCustomers = userRepo.select()
  .join(orderRepo.select())
  .on((u, o) -> u.id().eq(o.userId()))
  .where((user, order) -> user.status().eq(Optional.of(UserStatus.active)))
  .limit(50)
  .toList();

// Runs instantly, no database needed!
// Same code as production database queries!`
        },
        docs: "/docs/other-features/testing-with-stubs"
      }
    ]
  },
  {
    category: "Advanced PostgreSQL Integration",
    items: [
      {
        type: "generated",
        title: "PostgreSQL Array Support",
        description: "First-class support for PostgreSQL arrays with type-safe operations.",
        sqlCode: `-- Arrays in PostgreSQL
CREATE TABLE frontpage.product (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  tags TEXT[] DEFAULT '{}',
  categories INTEGER[] DEFAULT '{}',
  prices DECIMAL[] DEFAULT '{}',
  attributes JSONB[] DEFAULT '{}'
);`,
        fileKey: "product/ProductRow",
        extract: extractRowDefinition,
        docs: "/docs/type-safety/arrays"
      },
      {
        type: "generated",
        title: "Geometric and Network Types",
        description: "Support for POINT, POLYGON, INET, JSONB and more. If PostgreSQL has it, Typr supports it.",
        sqlCode: `-- Advanced PostgreSQL types
CREATE TABLE frontpage.location (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  position POINT,
  area POLYGON,
  ip_range INET,
  metadata JSONB DEFAULT '{}'
);`,
        fileKey: "location/LocationRow",
        extract: extractRowDefinition,
        docs: "/docs/type-safety/typo-types"
      }
    ]
  },
  {
    category: "Performance & Scalability",
    items: [
      {
        type: "inline",
        title: "Streaming Bulk Operations",
        description: "PostgreSQL COPY API integration for high-performance bulk inserts. Process millions of rows efficiently.",
        code: {
          scala: `// Streaming insert using PostgreSQL COPY
val users = Iterator.range(1, 1000000).map(i =>
  UserRowUnsaved(
    email = Email(s"user$i@example.com"),
    name = s"User $i"
  )
)

// Streams directly to PostgreSQL COPY API
val inserted = userRepo.insertUnsavedStreaming(users, batchSize = 10000)
println(s"Inserted $inserted records in seconds")

// Batch upsert - returns all upserted rows
val upsertedRows = userRepo.upsertBatch(usersList)`,
          java: `// Streaming insert using PostgreSQL COPY
var users = IntStream.range(1, 1000000)
  .mapToObj(i -> new UserRowUnsaved(
    new Email("user" + i + "@example.com"),
    "User " + i))
  .iterator();

// Streams directly to PostgreSQL COPY API
long inserted = userRepo.insertUnsavedStreaming(users, 10000, connection);
System.out.println("Inserted " + inserted + " records in seconds");

// Batch upsert - returns all upserted rows
var upsertedRows = userRepo.upsertBatch(usersList.iterator(), connection);`
        },
        docs: "/blog/the-cost-of-implicits"
      }
    ]
  },
  {
    category: "Multi-Library Support",
    items: [
      {
        type: "inline",
        title: "Choose Your Database Library",
        description: "Full support for Anorm, Doobie, ZIO-JDBC for Scala, and plain JDBC for Java/Kotlin.",
        code: {
          scala: `// Anorm (Play Framework)
class UserController @Inject()(userRepo: UserRepo, db: Database) {
  def getUser(id: UserId) = Action {
    db.withConnection { implicit c =>
      userRepo.selectById(id) match {
        case Some(user) => Ok(Json.toJson(user))
        case None => NotFound
      }
    }
  }
}

// Doobie (Cats Effect)
def getActiveUsers: ConnectionIO[List[UserRow]] =
  userRepo.select
    .where(_.status === UserStatus.active.?)
    .toList

// ZIO-JDBC
def getUsersZIO: ZIO[Connection, Throwable, List[UserRow]] =
  ZIO.serviceWithZIO[Connection](userRepo.selectAll(_))`,
          java: `// Plain JDBC - works with any connection pool
public class UserService {
  private final UserRepo userRepo;
  private final DataSource dataSource;

  public Optional<UserRow> getUser(UserId id) {
    try (var connection = dataSource.getConnection()) {
      return userRepo.selectById(id, connection);
    }
  }

  public List<UserRow> getActiveUsers() {
    try (var connection = dataSource.getConnection()) {
      return userRepo.select()
        .where(u -> u.status().eq(Optional.of(UserStatus.active)))
        .toList(connection);
    }
  }
}`
        },
        docs: "/docs/customization/overview#database-libraries"
      }
    ]
  }
];

export default function FeatureShowcase() {
  return (
    <section className={styles.featureShowcase}>
      <div className="container">
        <div className={styles.header}>
          <h2 className={styles.title}>
            Every Feature You Need, Nothing You Don't
          </h2>
          <p className={styles.subtitle}>
            Typr delivers a comprehensive PostgreSQL development experience with unprecedented type safety,
            testing capabilities, and developer productivity features.
          </p>
        </div>

        {features.map((category, categoryIndex) => (
          <div key={categoryIndex} className={styles.categorySection}>
            <h3 className={styles.categoryTitle}>{category.category}</h3>
            <div className={styles.featuresGrid}>
              {category.items.map((feature, featureIndex) => (
                feature.type === "generated" ? (
                  <FeatureWithGeneratedCode
                    key={featureIndex}
                    title={feature.title}
                    description={feature.description}
                    sqlCode={feature.sqlCode}
                    fileKey={feature.fileKey}
                    extract={feature.extract}
                    docs={feature.docs}
                  />
                ) : (
                  <FeatureWithInlineCode
                    key={featureIndex}
                    title={feature.title}
                    description={feature.description}
                    sqlCode={feature.sqlCode}
                    code={feature.code}
                    docs={feature.docs}
                  />
                )
              ))}
            </div>
          </div>
        ))}

        <div className={styles.moreFeatures}>
          <h3 className={styles.moreFeaturesTitle}>And Much More...</h3>
          <div className={styles.moreFeaturesList}>
            <div className={styles.moreFeatureItem}>
              <strong>Advanced Customization:</strong> Type overrides, nullability control, custom naming conventions
            </div>
            <div className={styles.moreFeatureItem}>
              <strong>Enterprise Ready:</strong> Transaction support, CI/CD integration, version control friendly
            </div>
            <div className={styles.moreFeatureItem}>
              <strong>Developer Experience:</strong> Real-time code generation, IDE integration, comprehensive logging
            </div>
            <div className={styles.moreFeatureItem}>
              <strong>PostgreSQL Deep Integration:</strong> Comprehensive array support with operations, enums, domains, geometric types, network types
            </div>
          </div>
          <div className={styles.moreFeaturesCTA}>
            <Link className="button button--primary button--lg" to="/docs">
              Explore All Features
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
