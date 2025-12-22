package testdb;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;
import testdb.customers.*;
import testdb.customtypes.Defaulted.*;
import testdb.orders.*;
import testdb.products.*;

/**
 * Comprehensive repository tests covering CRUD operations, DSL usage, and foreign key
 * relationships. Tests the full repository API including: - Basic CRUD (insert, select, update,
 * delete) - Batch operations (upsert, deleteByIds) - DSL select/update/delete - Foreign key
 * relationships - Type-safe ID handling
 */
public class RepositoryTest {
  private final CustomersRepoImpl customersRepo = new CustomersRepoImpl();
  private final OrdersRepoImpl ordersRepo = new OrdersRepoImpl();
  private final ProductsRepoImpl productsRepo = new ProductsRepoImpl();

  @Test
  public void testBasicInsertAndSelect() {
    WithConnection.run(
        c -> {
          var unsaved = new CustomersRowUnsaved(new CustomersId(8032), "Basic User");
          var inserted = customersRepo.insert(unsaved, c);

          assertNotNull(inserted.customerId());
          assertEquals("Basic User", inserted.name());

          var selected = customersRepo.selectById(inserted.customerId(), c);
          assertTrue(selected.isPresent());
          assertEquals(inserted.customerId(), selected.get().customerId());
        });
  }

  @Test
  public void testSelectAll() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(2000));

          for (int i = 0; i < 5; i++) {
            testInsert.Customers(
                new CustomersId(8000 + i),
                "First" + i + " Last" + i,
                Optional.of("email" + i + "@example.com"),
                new UseDefault<>(),
                new UseDefault<>(),
                c);
          }

          var all = customersRepo.selectAll(c);
          assertTrue(all.size() >= 5);
        });
  }

  @Test
  public void testUpdate() {
    WithConnection.run(
        c -> {
          var inserted =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(7080), "Before Update"), c);

          var updated =
              inserted
                  .withName("After")
                  .withName("Changed")
                  .withEmail(Optional.of("changed@example.com"));

          assertTrue(customersRepo.update(updated, c));

          var selected = customersRepo.selectById(inserted.customerId(), c);
          assertTrue(selected.isPresent());
          assertEquals("Changed", selected.get().name());
          assertEquals("changed@example.com", selected.get().email().get());
        });
  }

  @Test
  public void testDelete() {
    WithConnection.run(
        c -> {
          var inserted =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8106), "Delete Me"), c);

          assertTrue(customersRepo.deleteById(inserted.customerId(), c));
          assertFalse(customersRepo.selectById(inserted.customerId(), c).isPresent());
        });
  }

  @Test
  public void testUpsertInsert() {
    WithConnection.run(
        c -> {
          // Create a row that doesn't exist yet
          var newRow =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7120), "New User"), c);

          // Delete it
          customersRepo.deleteById(newRow.customerId(), c);

          // Upsert should insert with updated email
          var rowWithEmail = newRow.withEmail(java.util.Optional.of("upsert.new@example.com"));
          var upserted = customersRepo.upsert(rowWithEmail, c);
          assertEquals("upsert.new@example.com", upserted.email().get());

          var selected = customersRepo.selectById(newRow.customerId(), c);
          assertTrue(selected.isPresent());
        });
  }

  @Test
  public void testUpsertUpdate() {
    WithConnection.run(
        c -> {
          var inserted =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(7141), "Before Upsert"), c);

          var modified = inserted.withName("UpsertUpdate");

          var upserted = customersRepo.upsert(modified, c);
          assertEquals("UpsertUpdate", upserted.name());
          assertEquals(inserted.customerId(), upserted.customerId());
        });
  }

  @Test
  public void testUpsertBatch() {
    WithConnection.run(
        c -> {
          var c1 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8162), "Batch One"), c);
          var c2 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8166), "Batch Two"), c);

          var m1 = c1.withName("Modified1");
          var m2 = c2.withName("Modified2");

          var upserted = customersRepo.upsertBatch(List.of(m1, m2).iterator(), c);
          assertEquals(2, upserted.size());

          assertEquals("Modified1", customersRepo.selectById(c1.customerId(), c).get().name());
          assertEquals("Modified2", customersRepo.selectById(c2.customerId(), c).get().name());
        });
  }

  @Test
  public void testSelectByIds() {
    WithConnection.run(
        c -> {
          var c1 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8186), "Ids One"), c);
          var c2 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8189), "Ids Two"), c);
          var c3 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8192), "Ids Three"), c);

          var ids = new CustomersId[] {c1.customerId(), c3.customerId()};
          var selected = customersRepo.selectByIds(ids, c);

          assertEquals(2, selected.size());
          assertTrue(selected.stream().anyMatch(row -> row.customerId().equals(c1.customerId())));
          assertTrue(selected.stream().anyMatch(row -> row.customerId().equals(c3.customerId())));
        });
  }

  @Test
  public void testSelectByIdsTracked() {
    WithConnection.run(
        c -> {
          var c1 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7209), "Track One"), c);
          var c2 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7213), "Track Two"), c);

          var ids = new CustomersId[] {c1.customerId(), c2.customerId()};
          var tracked = customersRepo.selectByIdsTracked(ids, c);

          assertEquals(2, tracked.size());
          assertEquals("Track One", tracked.get(c1.customerId()).name());
          assertEquals("Track Two", tracked.get(c2.customerId()).name());
        });
  }

  @Test
  public void testDeleteByIds() {
    WithConnection.run(
        c -> {
          var c1 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8237), "Del One"), c);
          var c2 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8240), "Del Two"), c);
          var c3 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8243), "Del Three"), c);

          var ids = new CustomersId[] {c1.customerId(), c3.customerId()};
          var deleteCount = customersRepo.deleteByIds(ids, c);

          assertEquals(Integer.valueOf(2), deleteCount);

          assertFalse(customersRepo.selectById(c1.customerId(), c).isPresent());
          assertTrue(customersRepo.selectById(c2.customerId(), c).isPresent());
          assertFalse(customersRepo.selectById(c3.customerId(), c).isPresent());
        });
  }

  @Test
  public void testDSLSelect() {
    WithConnection.run(
        c -> {
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8262), "Alice Smith"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8264), "Bob Jones"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8266), "Alice Brown"), c);

          var alices =
              customersRepo
                  .select()
                  .where(f -> f.name().like("Alice%", typo.dsl.Bijection.asString()))
                  .toList(c);
          assertTrue(alices.size() >= 2);

          var specific =
              customersRepo.select().where(f -> f.name().isEqual("Alice Smith")).toList(c);
          assertEquals(1, specific.size());
          assertEquals("Alice Smith", specific.get(0).name());
        });
  }

  @Test
  public void testDSLSelectWithLike() {
    WithConnection.run(
        c -> {
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7283), "Alice Test"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7286), "Bob Test"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8296), "Other User"), c);

          var testNames =
              customersRepo
                  .select()
                  .where(f -> f.name().like("% Test", typo.dsl.Bijection.asString()))
                  .toList(c);
          assertTrue(testNames.size() >= 2);
          assertTrue(testNames.stream().allMatch(c2 -> c2.name().endsWith(" Test")));
        });
  }

  @Test
  public void testDSLUpdate() {
    WithConnection.run(
        c -> {
          var customer =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7305), "Before DSL"), c);

          customersRepo
              .update()
              .setValue(f -> f.name(), "Updated")
              .where(f -> f.customerId().isEqual(customer.customerId()))
              .execute(c);

          var updated = customersRepo.selectById(customer.customerId(), c);
          assertTrue(updated.isPresent());
          assertEquals("Updated", updated.get().name());
        });
  }

  @Test
  public void testDSLUpdateMultipleRows() {
    WithConnection.run(
        c -> {
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7327), "BulkUpdate One"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(7330), "BulkUpdate Two"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8343), "Other User"), c);

          customersRepo
              .update()
              .setValue(f -> f.name(), "Updated")
              .where(f -> f.name().like("BulkUpdate%", typo.dsl.Bijection.asString()))
              .execute(c);

          var updated = customersRepo.select().where(f -> f.name().isEqual("Updated")).toList(c);
          assertTrue(updated.size() >= 2);
          assertTrue(updated.stream().allMatch(c2 -> c2.name().equals("Updated")));
        });
  }

  @Test
  public void testDSLDelete() {
    WithConnection.run(
        c -> {
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8363), "ToDelete One"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8366), "ToDelete Two"), c);
          customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8369), "ToKeep One"), c);

          customersRepo
              .delete()
              .where(f -> f.name().like("ToDelete%", typo.dsl.Bijection.asString()))
              .execute(c);

          var remaining = customersRepo.selectAll(c);
          assertTrue(remaining.stream().noneMatch(c2 -> c2.name().startsWith("ToDelete")));
          assertTrue(remaining.stream().anyMatch(c2 -> c2.name().startsWith("ToKeep")));
        });
  }

  @Test
  public void testForeignKeyRelationship() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(8388));
          var customer =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(8388), "FK Customer"), c);

          var order =
              testInsert.Orders(
                  new OrdersId(9388),
                  customer.customerId().value(),
                  Optional.of(new BigDecimal("100.00")),
                  new Provided<>(LocalDate.now()),
                  new Provided<>(Optional.of("pending")),
                  c);

          assertEquals(customer.customerId().value(), order.customerId());

          // Verify we can query orders by customer
          var ordersForCustomer =
              ordersRepo
                  .select()
                  .where(f -> f.customerId().isEqual(customer.customerId().value()))
                  .toList(c);
          assertTrue(ordersForCustomer.size() >= 1);
          assertTrue(ordersForCustomer.stream().anyMatch(o -> o.orderId().equals(order.orderId())));
        });
  }

  @Test
  public void testMultipleForeignKeys() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(3000));

          var customer =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(8420), "Multi FK"), c);
          var product =
              testInsert.Products(
                  new ProductsId(9420),
                  "SKU-FK",
                  "FK Product",
                  new BigDecimal("50.00"),
                  Optional.empty(),
                  c);

          var order =
              testInsert.Orders(
                  new OrdersId(9421),
                  customer.customerId().value(),
                  Optional.empty(),
                  new UseDefault<>(),
                  new UseDefault<>(),
                  c);

          var orderItem =
              testInsert.OrderItems(
                  order.orderId().value(),
                  product.productId().value(),
                  product.price(),
                  new Provided<>(3),
                  c);

          // Verify all foreign keys
          assertEquals(customer.customerId().value(), order.customerId());
          assertEquals(order.orderId().value(), orderItem.orderId());
          assertEquals(product.productId().value(), orderItem.productId());
        });
  }

  @Test
  public void testComplexDSLQuery() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(4000));

          var customer1 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(7445), "Complex One"), c);
          var customer2 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(7449), "Complex Two"), c);

          // Customer1: 3 orders
          for (int i = 0; i < 3; i++) {
            testInsert.Orders(
                new OrdersId(9445 + i),
                customer1.customerId().value(),
                Optional.of(new BigDecimal("100.00")),
                new UseDefault<>(),
                new UseDefault<>(),
                c);
          }

          // Customer2: 1 order
          testInsert.Orders(
              new OrdersId(9450),
              customer2.customerId().value(),
              Optional.of(new BigDecimal("200.00")),
              new UseDefault<>(),
              new UseDefault<>(),
              c);

          // Complex query: customers whose name starts with "Complex"
          var complexCustomers =
              customersRepo
                  .select()
                  .where(f -> f.name().like("Complex%", typo.dsl.Bijection.asString()))
                  .toList(c);
          assertTrue(complexCustomers.size() >= 2);

          // Get their orders
          var customerIds = complexCustomers.stream().map(c2 -> c2.customerId().value()).toList();
          var orders = ordersRepo.selectAll(c);
          var relevantOrders =
              orders.stream().filter(o -> customerIds.contains(o.customerId())).toList();

          assertTrue(relevantOrders.size() >= 4); // 3 + 1
        });
  }

  @Test
  public void testMockRepo() {
    // Test that mock repo works (doesn't need connection)
    java.util.function.Function<CustomersRowUnsaved, CustomersRow> toRow =
        (CustomersRowUnsaved unsaved) ->
            new CustomersRow(
                unsaved.customerId(),
                unsaved.name(),
                Optional.of("mock@example.com"),
                java.time.LocalDateTime.now(),
                Optional.of(Priority.medium));
    var mockRepo = new CustomersRepoMock(toRow);

    var unsaved = new CustomersRowUnsaved(new CustomersId(999), "Mock Customer");
    var customer = mockRepo.insert(unsaved, null);

    var selected = mockRepo.selectById(new CustomersId(999), null);
    assertTrue(selected.isPresent());
    assertEquals("Mock Customer", selected.get().name());
  }
}
