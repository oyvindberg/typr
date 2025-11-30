import React from "react";
import Link from "@docusaurus/Link";
import CodeBlock from "@theme/CodeBlock";
import { useLanguage } from "../LanguageContext";
import styles from "./styles.module.css";

const features = [
  {
    category: "All The Boilerplate, None Of The Work",
    items: [
      {
        title: "From Database Schema to Complete Code",
        description: "Point Typr at your PostgreSQL database and watch it generate everything: data classes, repositories, type-safe IDs, JSON codecs, and test helpers. No manual mapping code ever again.",
        sqlCode: `-- Your PostgreSQL schema
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  department_id UUID REFERENCES departments(id)
);`,
        code: {
          scala: `// Generated automatically:
case class UserId(value: TyprUUID)
case class UserRow(
  id: UserId,
  email: String,
  name: String,
  createdAt: Option[TyprLocalDateTime],
  departmentId: Option[DepartmentsId]
)

trait UserRepo {
  def selectAll(implicit c: Connection): List[UserRow]
  def selectById(id: UserId)(implicit c: Connection): Option[UserRow]
  def insert(unsaved: UserRowUnsaved)(implicit c: Connection): UserRow
  def update(row: UserRow)(implicit c: Connection): Boolean
  def deleteById(id: UserId)(implicit c: Connection): Boolean
  // + 20 more methods
}`,
          java: `// Generated automatically:
public record UserId(UUID value) {}
public record UserRow(
  UserId id,
  String email,
  String name,
  Optional<LocalDateTime> createdAt,
  Optional<DepartmentsId> departmentId
) {}

public interface UserRepo {
  List<UserRow> selectAll(Connection c);
  Optional<UserRow> selectById(UserId id, Connection c);
  UserRow insert(UserRowUnsaved unsaved, Connection c);
  Boolean update(UserRow row, Connection c);
  Boolean deleteById(UserId id, Connection c);
  // + 20 more methods
}`,
          kotlin: `// Generated automatically:
@JvmInline
value class UserId(val value: UUID)
data class UserRow(
  val id: UserId,
  val email: String,
  val name: String,
  val createdAt: LocalDateTime?,
  val departmentId: DepartmentsId?
)

interface UserRepo {
  fun selectAll(c: Connection): List<UserRow>
  fun selectById(id: UserId, c: Connection): UserRow?
  fun insert(unsaved: UserRowUnsaved, c: Connection): UserRow
  fun update(row: UserRow, c: Connection): Boolean
  fun deleteById(id: UserId, c: Connection): Boolean
  // + 20 more methods
}`
        },
        docs: "/docs/setup"
      },
      {
        title: "Complete CRUD + Advanced Operations",
        description: "Get full repositories with not just basic CRUD, but batch operations, upserts, streaming inserts, and optional tracking methods. All generated, all type-safe.",
        code: {
          scala: `// All generated automatically from your schema:

// Basic operations
userRepo.selectById(UserId(uuid))
userRepo.insert(unsavedUser)
userRepo.update(user.copy(name = "New Name"))
userRepo.deleteById(userId)

// Batch operations
userRepo.upsertBatch(users)  // Returns the upserted rows

// Advanced operations
userRepo.selectByIds(userIds)
userRepo.selectByIdsTracked(userIds) // tracks found/missing
userRepo.insertStreaming(userStream)  // PostgreSQL COPY API`,
          java: `// All generated automatically from your schema:

// Basic operations
userRepo.selectById(userId, connection);
userRepo.insert(unsavedUser, connection);
userRepo.update(user.withName("New Name"), connection);
userRepo.deleteById(userId, connection);

// Batch operations
userRepo.upsertBatch(users.iterator(), connection);

// Advanced operations
userRepo.selectByIds(userIds, connection);
userRepo.selectByIdsTracked(userIds, connection);
userRepo.insertStreaming(userIterator, batchSize, connection);`,
          kotlin: `// All generated automatically from your schema:

// Basic operations
userRepo.selectById(userId, connection)
userRepo.insert(unsavedUser, connection)
userRepo.update(user.copy(name = "New Name"), connection)
userRepo.deleteById(userId, connection)

// Batch operations
userRepo.upsertBatch(users.iterator(), connection)

// Advanced operations
userRepo.selectByIds(userIds, connection)
userRepo.selectByIdsTracked(userIds, connection)
userRepo.insertStreaming(userIterator, batchSize, connection)`
        },
        docs: "/docs/what-is/relations"
      }
    ]
  },
  {
    category: "Relationships Become Navigation",
    items: [
      {
        title: "Foreign Keys Drive Everything",
        description: "Every foreign key in your database automatically generates navigation methods, type-safe joins, and reverse lookups. Your schema relationships become first-class code citizens.",
        sqlCode: `-- Database relationships
CREATE TABLE orders (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  product_id UUID REFERENCES products(id)
);`,
        code: {
          scala: `// Generated from foreign keys:
case class OrderRow(
  id: OrderId,
  userId: Option[UserId],    // Type flows through relationships
  productId: Option[ProductId]
)

// Type-safe DSL with automatic foreign key joins:
orderRepo.select
  .joinFk(_.fkUser)(userRepo.select)  // Auto-joins via foreign key
  .where { case (_, user) => user.email === Email("admin@company.com") }

// joinFk knows the relationship from your schema!
// Your IDE will autocomplete available foreign keys`,
          java: `// Generated from foreign keys:
public record OrderRow(
  OrderId id,
  Optional<UserId> userId,    // Type flows through relationships
  Optional<ProductId> productId
) {}

// Type-safe DSL with automatic foreign key joins:
orderRepo.select()
  .joinFk(OrderFields::fkUser, userRepo.select())
  .where((o, user) -> user.email().eq(Email.of("admin@company.com")))

// joinFk knows the relationship from your schema!
// Your IDE will autocomplete available foreign keys`,
          kotlin: `// Generated from foreign keys:
data class OrderRow(
  val id: OrderId,
  val userId: UserId?,    // Type flows through relationships
  val productId: ProductId?
)

// Type-safe DSL with automatic foreign key joins:
orderRepo.select()
  .joinFk(OrderFields::fkUser, userRepo.select())
  .where { o, user -> user.email.eq(Email("admin@company.com")) }

// joinFk knows the relationship from your schema!
// Your IDE will autocomplete available foreign keys`
        },
        docs: "/docs/what-is/relations"
      },
      {
        title: "Type-Safe Foreign Key Navigation",
        description: "Typr's DSL provides joinFk for easy type-safe navigation through foreign key relationships. Your IDE knows exactly what's available at each level.",
        sqlCode: `-- Database with foreign key relationships
CREATE TABLE products (
  id UUID PRIMARY KEY,
  model_id UUID REFERENCES product_models(id),
  subcategory_id UUID REFERENCES product_subcategories(id)
);
CREATE TABLE product_subcategories (
  id UUID PRIMARY KEY,
  category_id UUID REFERENCES product_categories(id)
);`,
        code: {
          scala: `// Navigate through multiple foreign keys with perfect type safety:
val query = productRepo.select
  .joinFk(_.fkProductModel)(productModelRepo.select)
  .joinFk { case (p, _) => p.fkProductSubcategory }(productSubcategoryRepo.select)
  .joinFk { case ((_, _), ps) => ps.fkProductCategory }(productCategoryRepo.select)
  .where { case (((product, model), subcategory), category) =>
    product.inStock === true &&
    category.name === "Electronics"
  }

// Each joinFk automatically uses the foreign key constraint
// No manual ON clauses needed - Typr knows the relationships!`,
          java: `// Navigate through multiple foreign keys with perfect type safety:
var query = productRepo.select()
  .joinFk(ProductFields::fkProductModel, productModelRepo.select())
  .joinFk((p, m) -> p.fkProductSubcategory(), productSubcategoryRepo.select())
  .joinFk((pm, ps) -> ps.fkProductCategory(), productCategoryRepo.select())
  .where((product, model, subcategory, category) ->
    product.inStock().eq(true).and(
    category.name().eq("Electronics"))
  );

// Each joinFk automatically uses the foreign key constraint
// No manual ON clauses needed - Typr knows the relationships!`,
          kotlin: `// Navigate through multiple foreign keys with perfect type safety:
val query = productRepo.select()
  .joinFk(ProductFields::fkProductModel, productModelRepo.select())
  .joinFk({ p, m -> p.fkProductSubcategory }, productSubcategoryRepo.select())
  .joinFk({ pm, ps -> ps.fkProductCategory }, productCategoryRepo.select())
  .where { product, model, subcategory, category ->
    product.inStock.eq(true) and
    category.name.eq("Electronics")
  }

// Each joinFk automatically uses the foreign key constraint
// No manual ON clauses needed - Typr knows the relationships!`
        },
        docs: "/docs/other-features/dsl-in-depth"
      }
    ]
  },
  {
    category: "Type Safety Revolution",
    items: [
      {
        title: "Strongly-Typed Primary Keys",
        description: "Every table gets its own ID type that flows through foreign key relationships. No more mixing up User IDs and Product IDs.",
        code: {
          scala: `case class UserId(value: TyprUUID)
case class ProductId(value: TyprUUID)

// Compile error if you mix them up!
def getUserOrders(userId: UserId): List[OrderRow] = {
  orderRepo.select
    .where(_.userId === userId.?)
    .toList
  // orderRepo.select.where(_.userId === productId.?) // Won't compile
}`,
          java: `public record UserId(UUID value) {}
public record ProductId(UUID value) {}

// Compile error if you mix them up!
List<OrderRow> getUserOrders(UserId userId, Connection c) {
  return orderRepo.select()
    .where(o -> o.userId().eq(Optional.of(userId)))
    .toList(c);
  // .where(o -> o.userId().eq(productId)) // Won't compile
}`,
          kotlin: `@JvmInline value class UserId(val value: UUID)
@JvmInline value class ProductId(val value: UUID)

// Compile error if you mix them up!
fun getUserOrders(userId: UserId, c: Connection): List<OrderRow> {
  return orderRepo.select()
    .where { it.userId.eq(userId) }
    .toList(c)
  // .where { it.userId.eq(productId) } // Won't compile
}`
        },
        docs: "/docs/type-safety/id-types"
      },
      {
        title: "Type Flow Through Relationships",
        description: "Foreign key relationships automatically propagate specific types throughout your domain model.",
        sqlCode: `-- Database schema creates type flow
CREATE TABLE users (
  id UUID PRIMARY KEY,
  name TEXT
);

CREATE TABLE orders (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id)
);`,
        code: {
          scala: `// Generated code maintains relationships
case class UserRow(id: UserId, name: String)
case class OrderRow(id: OrderId, userId: Option[UserId]) // Specific type, not just UUID`,
          java: `// Generated code maintains relationships
public record UserRow(UserId id, String name) {}
public record OrderRow(OrderId id, Optional<UserId> userId) {} // Specific type, not just UUID`,
          kotlin: `// Generated code maintains relationships
data class UserRow(val id: UserId, val name: String)
data class OrderRow(val id: OrderId, val userId: UserId?) // Specific type, not just UUID`
        },
        docs: "/docs/type-safety/type-flow"
      },
      {
        title: "PostgreSQL Domain Types",
        description: "Full support for PostgreSQL domains with constraint documentation in your generated code.",
        sqlCode: `-- Database domain
CREATE DOMAIN email AS TEXT CHECK (VALUE ~ '^[^@]+@[^@]+\\.[^@]+$');`,
        code: {
          scala: `/** Domain: frontpage.email
  * Constraint: CHECK ((VALUE ~ '^[^@]+@[^@]+\\.[^@]+$'::text))
  */
case class Email(value: String)

// Usage in generated types:
case class UserRow(id: UserId, email: Email) // Type preserved`,
          java: `/** Domain: frontpage.email
  * Constraint: CHECK ((VALUE ~ '^[^@]+@[^@]+\\.[^@]+$'::text))
  */
public record Email(String value) {}

// Usage in generated types:
public record UserRow(UserId id, Email email) {} // Type preserved`,
          kotlin: `/** Domain: frontpage.email
  * Constraint: CHECK ((VALUE ~ '^[^@]+@[^@]+\\.[^@]+$'::text))
  */
@JvmInline value class Email(val value: String)

// Usage in generated types:
data class UserRow(val id: UserId, val email: Email) // Type preserved`
        },
        docs: "/docs/type-safety/domains"
      },
      {
        title: "Composite Primary Keys",
        description: "First-class support for composite primary keys with generated helper types and methods.",
        sqlCode: `-- Composite key table
CREATE TABLE user_permissions (
  user_id UUID REFERENCES users(id),
  permission_id UUID REFERENCES permissions(id),
  granted_at TIMESTAMP,
  PRIMARY KEY (user_id, permission_id)
);`,
        code: {
          scala: `// Generated composite key row:
case class UserPermissionRow(
  userId: UserId,
  permissionId: PermissionId,
  grantedAt: Option[TyprLocalDateTime]
)

// Repository uses composite key directly:
userPermissionRepo.insert(UserPermissionRowUnsaved(
  userId = userId,
  permissionId = permissionId
))`,
          java: `// Generated composite key row:
public record UserPermissionRow(
  UserId userId,
  PermissionId permissionId,
  Optional<LocalDateTime> grantedAt
) {}

// Repository uses composite key directly:
userPermissionRepo.insert(new UserPermissionRowUnsaved(
  userId,
  permissionId
), connection);`,
          kotlin: `// Generated composite key row:
data class UserPermissionRow(
  val userId: UserId,
  val permissionId: PermissionId,
  val grantedAt: LocalDateTime?
)

// Repository uses composite key directly:
userPermissionRepo.insert(UserPermissionRowUnsaved(
  userId = userId,
  permissionId = permissionId
), connection)`
        },
        docs: "/docs/type-safety/id-types#composite-keys"
      }
    ]
  },
  {
    category: "The Perfect DSL For Real-World Data Access",
    items: [
      {
        title: "Incredibly Easy To Work With",
        description: "A pragmatic DSL that makes everyday data operations a breeze. Perfect IDE support with autocomplete, inline documentation, and compile-time validation. Focused on what you do most: fetching, updating, and deleting data with complex joins and filters.",
        code: {
          scala: `// Fetch exactly the data you need with type-safe joins
val activeOrdersWithDetails = orderRepo.select
  .join(customerRepo.select)
  .on((o, c) => o.userId === c.userId)
  .join(productRepo.select)
  .on { case ((o, _), p) => o.productId === p.id.? }
  .where { case ((order, _), _) => order.status === "active".? }
  .where { case ((_, customer), _) => customer.verified === true.? }
  .where { case (_, product) => product.inStock === true.? }
  .orderBy { case ((order, _), _) => order.createdAt.desc }
  .limit(100)
  .toList  // Execute and get results

// Update with complex conditions
productRepo.update
  .set(_.inStock, Some(false))
  .set(_.lastModified, Some(TyprLocalDateTime.now))
  .where(_.quantity === 0.?)
  .where(_.lastRestocked < thirtyDaysAgo.?)
  .execute

// Delete with conditions
orderItemRepo.delete
  .where(_.orderId.in(cancelledOrderIds))
  .where(_.shippedAt.isNull)
  .execute`,
          java: `// Fetch exactly the data you need with type-safe joins
var activeOrdersWithDetails = orderRepo.select()
  .join(customerRepo.select())
  .on((o, c) -> o.userId().eq(c.userId()))
  .join(productRepo.select())
  .on((oc, p) -> oc._1().productId().eq(Optional.of(p.id())))
  .where((order, customer, product) -> order.status().eq(Optional.of("active")))
  .where((order, customer, product) -> customer.verified().eq(Optional.of(true)))
  .where((order, customer, product) -> product.inStock().eq(Optional.of(true)))
  .orderBy((order, customer, product) -> order.createdAt().desc())
  .limit(100)
  .toList(connection);

// Update with complex conditions
productRepo.update()
  .set(ProductFields::inStock, Optional.of(false))
  .set(ProductFields::lastModified, Optional.of(LocalDateTime.now()))
  .where(p -> p.quantity().eq(Optional.of(0)))
  .where(p -> p.lastRestocked().lt(Optional.of(thirtyDaysAgo)))
  .execute(connection);

// Delete with conditions
orderItemRepo.delete()
  .where(oi -> oi.orderId().in(cancelledOrderIds))
  .where(oi -> oi.shippedAt().isNull())
  .execute(connection);`,
          kotlin: `// Fetch exactly the data you need with type-safe joins
val activeOrdersWithDetails = orderRepo.select()
  .join(customerRepo.select())
  .on { o, c -> o.userId.eq(c.userId) }
  .join(productRepo.select())
  .on { oc, p -> oc.first.productId.eq(p.id) }
  .where { order, customer, product -> order.status.eq("active") }
  .where { order, customer, product -> customer.verified.eq(true) }
  .where { order, customer, product -> product.inStock.eq(true) }
  .orderBy { order, customer, product -> order.createdAt.desc() }
  .limit(100)
  .toList(connection)

// Update with complex conditions
productRepo.update()
  .set(ProductFields::inStock, false)
  .set(ProductFields::lastModified, LocalDateTime.now())
  .where { it.quantity.eq(0) }
  .where { it.lastRestocked.lt(thirtyDaysAgo) }
  .execute(connection)

// Delete with conditions
orderItemRepo.delete()
  .where { it.orderId.isIn(cancelledOrderIds) }
  .where { it.shippedAt.isNull() }
  .execute(connection)`
        },
        docs: "/docs/other-features/dsl-in-depth"
      }
    ]
  },
  {
    category: "Pure SQL Files as First-Class Citizens",
    items: [
      {
        title: "Write Real SQL For Complex Queries",
        description: "When you need aggregations, window functions, or complex analytics, write real SQL in dedicated .sql files. Typr analyzes your queries and generates perfectly typed methods - the best of both worlds.",
        sqlCode: `-- sql/user-analytics.sql
SELECT
  u.name,
  u.email,
  COUNT(o.id) as order_count,
  SUM(o.total) as lifetime_value,
  MAX(o.created_at) as last_order_date
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.created_at >= :start_date:LocalDate!
  AND u.status = :status:UserStatus?
  AND (:min_orders? IS NULL OR COUNT(o.id) >= :min_orders)
GROUP BY u.id, u.name, u.email
HAVING SUM(o.total) > :min_value:BigDecimal!
ORDER BY lifetime_value DESC
LIMIT :limit:Int!`,
        code: {
          scala: `// Generated automatically:
trait UserAnalyticsSqlRepo {
  def apply(
    startDate: LocalDate,
    status: Option[String] = None,
    minValue: BigDecimal,
    limit: Int
  )(implicit c: Connection): List[UserAnalyticsSqlRow]
}`,
          java: `// Generated automatically:
public interface UserAnalyticsSqlRepo {
  List<UserAnalyticsSqlRow> apply(
    LocalDate startDate,
    Optional<String> status,
    BigDecimal minValue,
    Integer limit,
    Connection c
  );
}`,
          kotlin: `// Generated automatically:
interface UserAnalyticsSqlRepo {
  fun invoke(
    startDate: LocalDate,
    status: String?,
    minValue: BigDecimal,
    limit: Int,
    c: Connection
  ): List<UserAnalyticsSqlRow>
}`
        },
        docs: "/docs/what-is/sql-is-king"
      },
      {
        title: "Smart Parameter Inference",
        description: "Typr analyzes your SQL parameters against the database schema to infer exact types. Override nullability and types as needed with simple annotations.",
        code: {
          sql: `-- Advanced parameter syntax
SELECT p.*, a.city, e.salary
FROM persons p
JOIN addresses a ON p.address_id = a.id
LEFT JOIN employees e ON p.id = e.person_id
WHERE p.id = :person_id!               -- Required parameter
  AND p.created_at >= :since!          -- Required parameter
  AND a.country = :country:String?     -- Optional string parameter
  AND (:max_salary? IS NULL OR e.salary <= :max_salary)

-- Dynamic filtering patterns work perfectly
-- Type inference follows foreign keys
-- Custom domain types are preserved`
        },
        docs: "/docs/what-is/sql-is-king"
      },
      {
        title: "Updates with RETURNING Support",
        description: "Write UPDATE, INSERT, and DELETE operations in SQL files. Full support for RETURNING clauses with type-safe result parsing.",
        sqlCode: `-- sql/update-user-status.sql
UPDATE users
SET
  status = :new_status:frontpage.user_status!,
  created_at = NOW()
WHERE id = :user_id!
  AND status != :new_status
RETURNING
  id,
  name,
  status,
  created_at as "modified_at:java.time.LocalDateTime!"`,
        code: {
          scala: `// Generated method returns updated rows:
trait UpdateUserStatusSqlRepo {
  def apply(
    newStatus: String,
    userId: TyprUUID
  )(implicit c: Connection): List[UpdateUserStatusSqlRow]
}

// Perfect for audit trails and optimistic locking`,
          java: `// Generated method returns updated rows:
public interface UpdateUserStatusSqlRepo {
  List<UpdateUserStatusSqlRow> apply(
    String newStatus,
    UUID userId,
    Connection c
  );
}

// Perfect for audit trails and optimistic locking`,
          kotlin: `// Generated method returns updated rows:
interface UpdateUserStatusSqlRepo {
  fun invoke(
    newStatus: String,
    userId: UUID,
    c: Connection
  ): List<UpdateUserStatusSqlRow>
}

// Perfect for audit trails and optimistic locking`
        },
        docs: "/docs/what-is/sql-is-king"
      }
    ]
  },
  {
    category: "Testing Excellence",
    items: [
      {
        title: "TestInsert: Build Valid Data Graphs",
        description: "Generate complete object graphs with valid foreign key relationships. All fields are random by default, but you override exactly what your test cares about. Eliminates the 'lingering test state' problem forever.",
        code: {
          scala: `val testInsert = new TestInsert(new Random(42))

// Build a complete, valid data graph
val company = testInsert.frontpageCompanies(name = "Acme Corp")
val department = testInsert.frontpageDepartments(companyId = Some(company.id))
val manager = testInsert.frontpageUsers(
  departmentId = Some(department.id),
  role = Defaulted.Provided(Some(UserRole.manager))
)
val employees = List.fill(5)(
  testInsert.frontpageUsers(
    departmentId = Some(department.id),
    managerId = Some(manager.id),
    role = Defaulted.Provided(Some(UserRole.employee))
  )
)

// Every foreign key is valid!
// All other fields are realistic random data!
// Zero lingering state between tests!`,
          java: `var testInsert = new TestInsert(new Random(42));

// Build a complete, valid data graph
var company = testInsert.frontpageCompanies(c, "Acme Corp");
var department = testInsert.frontpageDepartments(c, Optional.of(company.id()));
var manager = testInsert.frontpageUsers(c,
  Optional.of(department.id()),
  Defaulted.provided(Optional.of(UserRole.MANAGER))
);
var employees = IntStream.range(0, 5)
  .mapToObj(i -> testInsert.frontpageUsers(c,
    Optional.of(department.id()),
    Optional.of(manager.id()),
    Defaulted.provided(Optional.of(UserRole.EMPLOYEE))
  ))
  .toList();

// Every foreign key is valid!
// All other fields are realistic random data!
// Zero lingering state between tests!`,
          kotlin: `val testInsert = TestInsert(Random(42))

// Build a complete, valid data graph
val company = testInsert.frontpageCompanies(c, name = "Acme Corp")
val department = testInsert.frontpageDepartments(c, companyId = company.id)
val manager = testInsert.frontpageUsers(c,
  departmentId = department.id,
  role = Defaulted.provided(UserRole.MANAGER)
)
val employees = (1..5).map {
  testInsert.frontpageUsers(c,
    departmentId = department.id,
    managerId = manager.id,
    role = Defaulted.provided(UserRole.EMPLOYEE)
  )
}

// Every foreign key is valid!
// All other fields are realistic random data!
// Zero lingering state between tests!`
        },
        docs: "/docs/other-features/testing-with-random-values"
      },
      {
        title: "In-Memory Repository Stubs",
        description: "Drop-in repository replacements that work entirely in memory. Run huge parts of your application without a database - perfect for unit tests and development.",
        code: {
          scala: `// Replace real repos with in-memory stubs
val userRepo = UserRepoMock.empty
val orderRepo = OrdersRepoMock.empty
val productRepo = ProductsRepoMock.empty

// Seed with test data
userRepo.insertUnsaved(testUsers: _*)
orderRepo.insertUnsaved(testOrders: _*)
productRepo.insertUnsaved(testProducts: _*)

// Your entire business logic works!
val orderService = new OrderService(userRepo, orderRepo, productRepo)
val result = orderService.calculateMonthlyReport(userId)

// Runs instantly, no database needed
// Full DSL support including complex joins`,
          java: `// Replace real repos with in-memory stubs
var userRepo = UserRepoMock.empty();
var orderRepo = OrdersRepoMock.empty();
var productRepo = ProductsRepoMock.empty();

// Seed with test data
userRepo.insertUnsaved(testUsers);
orderRepo.insertUnsaved(testOrders);
productRepo.insertUnsaved(testProducts);

// Your entire business logic works!
var orderService = new OrderService(userRepo, orderRepo, productRepo);
var result = orderService.calculateMonthlyReport(userId);

// Runs instantly, no database needed
// Full DSL support including complex joins`,
          kotlin: `// Replace real repos with in-memory stubs
val userRepo = UserRepoMock.empty()
val orderRepo = OrdersRepoMock.empty()
val productRepo = ProductsRepoMock.empty()

// Seed with test data
userRepo.insertUnsaved(*testUsers)
orderRepo.insertUnsaved(*testOrders)
productRepo.insertUnsaved(*testProducts)

// Your entire business logic works!
val orderService = OrderService(userRepo, orderRepo, productRepo)
val result = orderService.calculateMonthlyReport(userId)

// Runs instantly, no database needed
// Full DSL support including complex joins`
        },
        docs: "/docs/other-features/testing-with-stubs"
      },
      {
        title: "Full DSL Support in Stubs",
        description: "Unlike other testing libraries, Typr's mocks support the complete DSL including complex joins and filtering. Your business logic runs unchanged.",
        code: {
          scala: `// Complex queries work in memory!
val topCustomers = userRepo.select
  .join(orderRepo.select)
  .on((u, o) => u.id === o.userId.?)
  .join(productRepo.select)
  .on { case ((_, o), p) => o.productId === p.id.? }
  .where { case ((user, _), _) => user.status === "active".? }
  .where { case (_, product) => product.price > BigDecimal("100") }
  .limit(50)
  .toList

// This runs instantly in memory!
// Same code as production database queries!`,
          java: `// Complex queries work in memory!
var topCustomers = userRepo.select()
  .join(orderRepo.select())
  .on((u, o) -> u.id().eq(o.userId()))
  .join(productRepo.select())
  .on((uo, p) -> uo._2().productId().eq(Optional.of(p.id())))
  .where((user, order, product) -> user.status().eq(Optional.of("active")))
  .where((user, order, product) -> product.price().gt(new BigDecimal("100")))
  .limit(50)
  .toList();

// This runs instantly in memory!
// Same code as production database queries!`,
          kotlin: `// Complex queries work in memory!
val topCustomers = userRepo.select()
  .join(orderRepo.select())
  .on { u, o -> u.id.eq(o.userId) }
  .join(productRepo.select())
  .on { uo, p -> uo.second.productId.eq(p.id) }
  .where { user, order, product -> user.status.eq("active") }
  .where { user, order, product -> product.price.gt(BigDecimal("100")) }
  .limit(50)
  .toList()

// This runs instantly in memory!
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
        title: "Unprecedented PostgreSQL Array Support",
        description: "First-class support for PostgreSQL arrays with type-safe operations. Use arrays naturally in queries with .in(), arrayOverlaps, arrayConcat, and array indexing.",
        code: {
          scala: `// Full array support for all PostgreSQL types
case class ProductRow(
  id: ProductsId,
  name: String,
  tags: Option[Array[String]],        // TEXT[]
  categories: Option[Array[Int]],     // INTEGER[]
  prices: Option[Array[BigDecimal]],  // NUMERIC[]
  attributes: Option[Array[TyprJsonb]] // JSONB[]
)

// Array operations in queries
productRepo.select
  .where(_.id.in(Array(id1, id2, id3)))
  .where(_.tags.getOrElse(Array.empty).contains("sale"))
  .toList`,
          java: `// Full array support for all PostgreSQL types
public record ProductRow(
  ProductsId id,
  String name,
  Optional<String[]> tags,           // TEXT[]
  Optional<Integer[]> categories,    // INTEGER[]
  Optional<BigDecimal[]> prices,     // NUMERIC[]
  Optional<Jsonb[]> attributes       // JSONB[]
) {}

// Array operations in queries
productRepo.select()
  .where(p -> p.id().in(new ProductsId[]{id1, id2, id3}))
  .where(p -> p.tags().contains("sale"))
  .toList(connection);`,
          kotlin: `// Full array support for all PostgreSQL types
data class ProductRow(
  val id: ProductsId,
  val name: String,
  val tags: Array<String>?,          // TEXT[]
  val categories: Array<Int>?,       // INTEGER[]
  val prices: Array<BigDecimal>?,    // NUMERIC[]
  val attributes: Array<Jsonb>?      // JSONB[]
)

// Array operations in queries
productRepo.select()
  .where { it.id.isIn(arrayOf(id1, id2, id3)) }
  .where { it.tags.contains("sale") }
  .toList(connection)`
        },
        docs: "/docs/type-safety/arrays"
      },
      {
        title: "Other PostgreSQL Types & Features",
        description: "Support for geometric types, network types, JSON/JSONB, XML, and more. If PostgreSQL has it, Typr supports it.",
        code: {
          scala: `// Geometric and network types
case class LocationRow(
  id: LocationsId,
  position: Option[TyprPoint],   // POINT
  area: Option[TyprPolygon],     // POLYGON
  ipRange: Option[TyprInet],     // INET
  metadata: Option[TyprJsonb]    // JSONB
)

// Types are preserved and can be used in queries
locationRepo.select
  .where(_.name === "Main Office")
  .toList`,
          java: `// Geometric and network types
public record LocationRow(
  LocationsId id,
  Optional<Point> position,    // POINT
  Optional<Polygon> area,      // POLYGON
  Optional<Inet> ipRange,      // INET
  Optional<Jsonb> metadata     // JSONB
) {}

// Types are preserved and can be used in queries
locationRepo.select()
  .where(l -> l.name().eq("Main Office"))
  .toList(connection);`,
          kotlin: `// Geometric and network types
data class LocationRow(
  val id: LocationsId,
  val position: Point?,    // POINT
  val area: Polygon?,      // POLYGON
  val ipRange: Inet?,      // INET
  val metadata: Jsonb?     // JSONB
)

// Types are preserved and can be used in queries
locationRepo.select()
  .where { it.name.eq("Main Office") }
  .toList(connection)`
        },
        docs: "/docs/type-safety/typo-types"
      }
    ]
  },
  {
    category: "Performance & Scalability",
    items: [
      {
        title: "Streaming Bulk Operations",
        description: "PostgreSQL COPY API integration for high-performance bulk inserts and updates.",
        code: {
          scala: `// Streaming insert using PostgreSQL COPY
val users = Iterator.range(1, 1000000).map(i =>
  UserRowUnsaved(
    name = s"User $i",
    email = s"user$i@example.com"
  )
)

// Streams directly to PostgreSQL COPY API
val inserted = userRepo.insertUnsavedStreaming(users)
println(s"Inserted $inserted records in seconds")

// Batch operations - returns the upserted rows
val upsertedRows = userRepo.upsertBatch(usersList)
println(s"Upserted \${upsertedRows.length} rows")`,
          java: `// Streaming insert using PostgreSQL COPY
var users = IntStream.range(1, 1000000)
  .mapToObj(i -> new UserRowUnsaved(
    "User " + i,
    "user" + i + "@example.com"
  ))
  .iterator();

// Streams directly to PostgreSQL COPY API
long inserted = userRepo.insertUnsavedStreaming(users, 10000, connection);
System.out.println("Inserted " + inserted + " records in seconds");

// Batch operations - returns the upserted rows
var upsertedRows = userRepo.upsertBatch(usersList.iterator(), connection);
System.out.println("Upserted " + upsertedRows.size() + " rows");`,
          kotlin: `// Streaming insert using PostgreSQL COPY
val users = (1 until 1000000).asSequence().map { i ->
  UserRowUnsaved(
    name = "User $i",
    email = "user$i@example.com"
  )
}.iterator()

// Streams directly to PostgreSQL COPY API
val inserted = userRepo.insertUnsavedStreaming(users, 10000, connection)
println("Inserted $inserted records in seconds")

// Batch operations - returns the upserted rows
val upsertedRows = userRepo.upsertBatch(usersList.iterator(), connection)
println("Upserted \${upsertedRows.size} rows")`
        },
        docs: "/blog/the-cost-of-implicits"
      },
      {
        title: "Efficient Batch Operations",
        description: "Optimized batch insert, update, and delete operations with detailed result tracking.",
        code: {
          scala: `// True batch operations - single database roundtrip!
val newUsers = List(
  UserRowUnsaved(email = Email("user1@example.com"), name = "User 1"),
  UserRowUnsaved(email = Email("user2@example.com"), name = "User 2"),
  UserRowUnsaved(email = Email("user3@example.com"), name = "User 3")
)

// Batch upsert - returns all upserted rows
val upsertedUsers = userRepo.upsertBatch(newUsers)
println(s"Upserted \${upsertedUsers.length} users")

// Batch delete by IDs
val deleted = userRepo.deleteByIds(Array(userId1, userId2, userId3))
println(s"Deleted $deleted rows")

// Streaming batch operations for huge datasets
val millionUsers = Iterator.range(1, 1000000).map(i =>
  UserRowUnsaved(email = Email(s"user$i@example.com"), name = s"User $i")
)
userRepo.insertUnsavedStreaming(millionUsers) // Uses PostgreSQL COPY`,
          java: `// True batch operations - single database roundtrip!
var newUsers = List.of(
  new UserRowUnsaved(new Email("user1@example.com"), "User 1"),
  new UserRowUnsaved(new Email("user2@example.com"), "User 2"),
  new UserRowUnsaved(new Email("user3@example.com"), "User 3")
);

// Batch upsert - returns all upserted rows
var upsertedUsers = userRepo.upsertBatch(newUsers.iterator(), connection);
System.out.println("Upserted " + upsertedUsers.size() + " users");

// Batch delete by IDs
int deleted = userRepo.deleteByIds(new UserId[]{userId1, userId2, userId3}, connection);
System.out.println("Deleted " + deleted + " rows");

// Streaming batch operations for huge datasets
var millionUsers = IntStream.range(1, 1000000)
  .mapToObj(i -> new UserRowUnsaved(new Email("user" + i + "@example.com"), "User " + i))
  .iterator();
userRepo.insertUnsavedStreaming(millionUsers, 10000, connection);`,
          kotlin: `// True batch operations - single database roundtrip!
val newUsers = listOf(
  UserRowUnsaved(email = Email("user1@example.com"), name = "User 1"),
  UserRowUnsaved(email = Email("user2@example.com"), name = "User 2"),
  UserRowUnsaved(email = Email("user3@example.com"), name = "User 3")
)

// Batch upsert - returns all upserted rows
val upsertedUsers = userRepo.upsertBatch(newUsers.iterator(), connection)
println("Upserted \${upsertedUsers.size} users")

// Batch delete by IDs
val deleted = userRepo.deleteByIds(arrayOf(userId1, userId2, userId3), connection)
println("Deleted $deleted rows")

// Streaming batch operations for huge datasets
val millionUsers = (1 until 1000000).asSequence().map { i ->
  UserRowUnsaved(email = Email("user$i@example.com"), name = "User $i")
}.iterator()
userRepo.insertUnsavedStreaming(millionUsers, 10000, connection)`
        },
        docs: "/blog/the-cost-of-implicits"
      }
    ]
  },
  {
    category: "Multi-Library Support",
    items: [
      {
        title: "Choose Your Database Library",
        description: "Full support for Anorm, Doobie, ZIO-JDBC for Scala, and plain JDBC for Java/Kotlin with library-specific optimizations.",
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
    .where(user => user.status === "active".?)
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
        .where(u -> u.status().eq(Optional.of("active")))
        .toList(connection);
    }
  }
}`,
          kotlin: `// Plain JDBC - works with any connection pool
class UserService(
  private val userRepo: UserRepo,
  private val dataSource: DataSource
) {
  fun getUser(id: UserId): UserRow? {
    dataSource.connection.use { connection ->
      return userRepo.selectById(id, connection)
    }
  }

  fun getActiveUsers(): List<UserRow> {
    dataSource.connection.use { connection ->
      return userRepo.select()
        .where { it.status.eq("active") }
        .toList(connection)
    }
  }
}`
        },
        docs: "/docs/customization/overview#database-libraries"
      },
      {
        title: "JSON Library Integration",
        description: "Typr generates JSON codecs for Play JSON, Circe, ZIO JSON (Scala), and Jackson (Java/Kotlin) - no manual derivation needed.",
        code: {
          scala: `// Typr generates all JSON codecs for you!

// Play JSON - generated in UserRow companion
implicit val usersReads: Reads[UserRow] = UserRow.reads
implicit val usersWrites: Writes[UserRow] = UserRow.writes

// Circe - generated in UserRow companion
implicit val usersDecoder: Decoder[UserRow] = UserRow.decoder
implicit val usersEncoder: Encoder[UserRow] = UserRow.encoder

// ZIO JSON - generated in UserRow companion
implicit val usersCodec: JsonCodec[UserRow] = UserRow.codec

// Just use them - handles all complex types, arrays, nested objects
val json = Json.toJson(user)
val decoded = json.as[UserRow]`,
          java: `// Typr generates Jackson annotations for you!

// Records come with full Jackson support
@JsonProperty("id")
public UserId id() { return id; }

@JsonProperty("email")
public String email() { return email; }

// ObjectMapper handles everything automatically
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);
UserRow decoded = mapper.readValue(json, UserRow.class);

// Works with all complex types, arrays, nested objects`,
          kotlin: `// Typr generates Jackson annotations for you!

// Data classes come with full Jackson support
data class UserRow(
  @JsonProperty("id") val id: UserId,
  @JsonProperty("email") val email: String,
  @JsonProperty("name") val name: String?
)

// ObjectMapper handles everything automatically
val mapper = ObjectMapper().registerKotlinModule()
val json = mapper.writeValueAsString(user)
val decoded = mapper.readValue<UserRow>(json)

// Works with all complex types, arrays, nested objects`
        },
        docs: "/docs/other-features/json"
      }
    ]
  }
];

function FeatureCode({ code, sqlCode }) {
  const { language } = useLanguage();

  // Handle SQL-only features
  if (sqlCode && !code) {
    return (
      <CodeBlock language="sql">
        {sqlCode}
      </CodeBlock>
    );
  }

  // Determine language code content
  let codeContent;
  let codeLanguage;

  if (typeof code === 'string') {
    // Legacy: single code string (treat as Scala)
    codeContent = code;
    codeLanguage = "scala";
  } else if (code && code[language]) {
    codeContent = code[language];
    codeLanguage = language;
  } else if (code && code.sql) {
    codeContent = code.sql;
    codeLanguage = "sql";
  } else if (code && code.scala) {
    codeContent = code.scala;
    codeLanguage = "scala";
  }

  return (
    <>
      {sqlCode && (
        <CodeBlock language="sql">
          {sqlCode}
        </CodeBlock>
      )}
      {codeContent && (
        <CodeBlock language={codeLanguage}>
          {codeContent}
        </CodeBlock>
      )}
    </>
  );
}

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
                <div key={featureIndex} className={styles.featureCard}>
                  <div className={styles.featureHeader}>
                    <h4 className={styles.featureTitle}>{feature.title}</h4>
                    <p className={styles.featureDescription}>{feature.description}</p>
                  </div>
                  <FeatureCode code={feature.code} sqlCode={feature.sqlCode} />
                  <div className={styles.featureFooter}>
                    <Link
                      className={styles.featureLink}
                      to={feature.docs}
                    >
                      Learn More
                    </Link>
                  </div>
                </div>
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
