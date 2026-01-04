package testdb;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import testdb.customers.CustomersRepoImpl;
import testdb.customers.CustomersRow;
import testdb.customers.CustomersRowUnsaved;
import testdb.order_items.OrderItemsId;
import testdb.order_items.OrderItemsRepoImpl;
import testdb.order_items.OrderItemsRow;
import testdb.orders.OrdersRepoImpl;
import testdb.orders.OrdersRow;
import testdb.orders.OrdersRowUnsaved;

public class CrudTest {
  private final CustomersRepoImpl customersRepo = new CustomersRepoImpl();
  private final OrdersRepoImpl ordersRepo = new OrdersRepoImpl();
  private final OrderItemsRepoImpl orderItemsRepo = new OrderItemsRepoImpl();

  @Test
  public void testCustomerInsert() {
    WithConnection.run(
        c -> {
          var unsaved = new CustomersRowUnsaved("Test Customer", "test@example.com");
          CustomersRow saved = customersRepo.insert(unsaved, c);

          Assert.assertEquals("Test Customer", saved.name());
          Assert.assertEquals("test@example.com", saved.email());
          Assert.assertNotNull(saved.customerId());
        });
  }

  @Test
  public void testCustomerSelectById() {
    WithConnection.run(
        c -> {
          var unsaved = new CustomersRowUnsaved("Select Test", "select@example.com");
          CustomersRow saved = customersRepo.insert(unsaved, c);

          Optional<CustomersRow> found = customersRepo.selectById(saved.customerId(), c);
          Assert.assertTrue(found.isPresent());
          Assert.assertEquals("Select Test", found.get().name());
        });
  }

  @Test
  public void testCustomerUpdate() {
    WithConnection.run(
        c -> {
          var unsaved = new CustomersRowUnsaved("Original Name", "update@example.com");
          CustomersRow saved = customersRepo.insert(unsaved, c);

          CustomersRow updated = saved.withName("Updated Name");
          Boolean success = customersRepo.update(updated, c);
          Assert.assertTrue(success);

          Optional<CustomersRow> found = customersRepo.selectById(saved.customerId(), c);
          Assert.assertTrue(found.isPresent());
          Assert.assertEquals("Updated Name", found.get().name());
        });
  }

  @Test
  public void testCustomerDelete() {
    WithConnection.run(
        c -> {
          var unsaved = new CustomersRowUnsaved("Delete Test", "delete@example.com");
          CustomersRow saved = customersRepo.insert(unsaved, c);

          Boolean deleted = customersRepo.deleteById(saved.customerId(), c);
          Assert.assertTrue(deleted);

          Optional<CustomersRow> found = customersRepo.selectById(saved.customerId(), c);
          Assert.assertFalse(found.isPresent());
        });
  }

  @Test
  public void testOrderWithCustomer() {
    WithConnection.run(
        c -> {
          // First create a customer
          var customer =
              customersRepo.insert(
                  new CustomersRowUnsaved("Order Customer", "order@example.com"), c);

          // Then create an order for this customer
          var orderUnsaved =
              new OrdersRowUnsaved(customer.customerId())
                  .withTotalAmount(Optional.of(new BigDecimal("99.99")));
          OrdersRow order = ordersRepo.insert(orderUnsaved, c);

          Assert.assertEquals(customer.customerId(), order.customerId());
          Assert.assertEquals(Optional.of(new BigDecimal("99.99")), order.totalAmount());
        });
  }

  @Test
  public void testOrderItemsWithCompositeKey() {
    WithConnection.run(
        c -> {
          // Create customer
          var customer =
              customersRepo.insert(
                  new CustomersRowUnsaved("Item Customer", "items@example.com"), c);

          // Create order
          var order = ordersRepo.insert(new OrdersRowUnsaved(customer.customerId()), c);

          // Create order items
          var item1 =
              new OrderItemsRow(order.orderId(), 1, "Product A", 2, new BigDecimal("10.00"));
          var item2 =
              new OrderItemsRow(order.orderId(), 2, "Product B", 1, new BigDecimal("25.50"));

          var savedItem1 = orderItemsRepo.insert(item1, c);
          var savedItem2 = orderItemsRepo.insert(item2, c);

          Assert.assertEquals("Product A", savedItem1.productName());
          Assert.assertEquals("Product B", savedItem2.productName());

          // Test composite ID lookup
          var compositeId = new OrderItemsId(order.orderId(), 1);
          Optional<OrderItemsRow> found = orderItemsRepo.selectById(compositeId, c);
          Assert.assertTrue(found.isPresent());
          Assert.assertEquals("Product A", found.get().productName());
        });
  }

  @Test
  public void testSelectAll() {
    WithConnection.run(
        c -> {
          // Insert some customers
          customersRepo.insert(new CustomersRowUnsaved("All Test 1", "all1@example.com"), c);
          customersRepo.insert(new CustomersRowUnsaved("All Test 2", "all2@example.com"), c);

          var all = customersRepo.selectAll(c);
          Assert.assertTrue(all.size() >= 2);
        });
  }

  @Test
  public void testUpsert() {
    WithConnection.run(
        c -> {
          // Insert a customer
          var inserted =
              customersRepo.insert(new CustomersRowUnsaved("Upsert Test", "upsert@example.com"), c);

          // Upsert with same ID but different name
          // Note: DB2 upsert returns void because MERGE doesn't support RETURNING
          var toUpsert = inserted.withName("Upserted Name");
          customersRepo.upsert(toUpsert, c);

          // Verify the upsert worked by selecting the row
          var upserted = customersRepo.selectById(inserted.customerId(), c);
          Assert.assertTrue(upserted.isPresent());
          Assert.assertEquals("Upserted Name", upserted.get().name());
          Assert.assertEquals(inserted.customerId(), upserted.get().customerId());
        });
  }

  @Test
  public void testSelectByUniqueEmail() {
    WithConnection.run(
        c -> {
          var email = "unique@example.com";
          customersRepo.insert(new CustomersRowUnsaved("Unique Email", email), c);

          Optional<CustomersRow> found = customersRepo.selectByUniqueEmail(email, c);
          Assert.assertTrue(found.isPresent());
          Assert.assertEquals("Unique Email", found.get().name());
        });
  }
}
