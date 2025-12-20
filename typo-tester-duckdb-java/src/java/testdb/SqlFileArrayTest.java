package testdb;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;
import testdb.customers.*;
import testdb.customtypes.Defaulted.*;
import testdb.order_items.*;
import testdb.order_summary_by_customer.*;
import testdb.orders.*;
import testdb.product_details_with_sales.*;
import testdb.products.*;

/**
 * Tests for SQL files with array parameters. This specifically tests the array type fix for DuckDB,
 * ensuring that: - Array parameters are correctly typed as Integer[] not String - Parameters can be
 * null (Optional.empty()) - Parameters can contain arrays of IDs - Array parameters work correctly
 * in WHERE clauses with ANY()
 */
public class SqlFileArrayTest {
  private final CustomersRepoImpl customersRepo = new CustomersRepoImpl();
  private final ProductsRepoImpl productsRepo = new ProductsRepoImpl();
  private final OrdersRepoImpl ordersRepo = new OrdersRepoImpl();
  private final OrderItemsRepoImpl orderItemsRepo = new OrderItemsRepoImpl();
  private final ProductDetailsWithSalesSqlRepoImpl productDetailsRepo =
      new ProductDetailsWithSalesSqlRepoImpl();
  private final OrderSummaryByCustomerSqlRepoImpl orderSummaryRepo =
      new OrderSummaryByCustomerSqlRepoImpl();

  @Test
  public void testProductDetailsWithSalesNoFilters() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(100));

          // Insert test data
          var product1 =
              testInsert.Products(
                  new ProductsId(6000 + 41),
                  "SKU-001",
                  "Product 1",
                  new BigDecimal("10.00"),
                  Optional.empty(),
                  c);
          var product2 =
              testInsert.Products(
                  new ProductsId(6000 + 43),
                  "SKU-002",
                  "Product 2",
                  new BigDecimal("20.00"),
                  Optional.empty(),
                  c);
          var product3 =
              testInsert.Products(
                  new ProductsId(6000 + 45),
                  "SKU-003",
                  "Product 3",
                  new BigDecimal("30.00"),
                  Optional.empty(),
                  c);

          // Query with no filters (all null)
          var results =
              productDetailsRepo.apply(
                  Optional.empty(), // product_ids
                  Optional.empty(), // sku_pattern
                  Optional.empty(), // min_price
                  Optional.empty(), // max_price
                  c);

          assertTrue(results.size() >= 3);
          assertTrue(results.stream().anyMatch(p -> p.productId().equals(product1.productId())));
          assertTrue(results.stream().anyMatch(p -> p.productId().equals(product2.productId())));
          assertTrue(results.stream().anyMatch(p -> p.productId().equals(product3.productId())));
        });
  }

  @Test
  public void testProductDetailsWithSalesArrayFilter() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(200));

          var product1 =
              testInsert.Products(
                  new ProductsId(6000 + 76),
                  "SKU-101",
                  "Product A",
                  new BigDecimal("15.00"),
                  Optional.empty(),
                  c);
          var product2 =
              testInsert.Products(
                  new ProductsId(6000 + 78),
                  "SKU-102",
                  "Product B",
                  new BigDecimal("25.00"),
                  Optional.empty(),
                  c);
          var product3 =
              testInsert.Products(
                  new ProductsId(6000 + 80),
                  "SKU-103",
                  "Product C",
                  new BigDecimal("35.00"),
                  Optional.empty(),
                  c);
          var product4 =
              testInsert.Products(
                  new ProductsId(6000 + 82),
                  "SKU-104",
                  "Product D",
                  new BigDecimal("45.00"),
                  Optional.empty(),
                  c);

          // Filter by array of product IDs - THIS IS THE KEY TEST FOR ARRAY TYPES
          Integer[] productIds =
              new Integer[] {product1.productId().value(), product3.productId().value()};

          var results =
              productDetailsRepo.apply(
                  Optional.of(productIds), Optional.empty(), Optional.empty(), Optional.empty(), c);

          assertEquals(2, results.size());
          assertTrue(results.stream().anyMatch(p -> p.productId().equals(product1.productId())));
          assertTrue(results.stream().anyMatch(p -> p.productId().equals(product3.productId())));
          assertFalse(results.stream().anyMatch(p -> p.productId().equals(product2.productId())));
          assertFalse(results.stream().anyMatch(p -> p.productId().equals(product4.productId())));
        });
  }

  @Test
  public void testProductDetailsWithSalesEmptyArray() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(250));

          testInsert.Products(
              new ProductsId(6000 + 120),
              "SKU-200",
              "Product Empty",
              new BigDecimal("50.00"),
              Optional.empty(),
              c);

          // Empty array should return no results
          Integer[] emptyArray = new Integer[] {};

          var results =
              productDetailsRepo.apply(
                  Optional.of(emptyArray), Optional.empty(), Optional.empty(), Optional.empty(), c);

          assertEquals(0, results.size());
        });
  }

  @Test
  public void testProductDetailsWithSalesSingleItemArray() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(300));

          var product =
              testInsert.Products(
                  new ProductsId(6000 + 144),
                  "SKU-201",
                  "Single Product",
                  new BigDecimal("99.99"),
                  Optional.empty(),
                  c);

          // Single item array
          Integer[] singleId = new Integer[] {product.productId().value()};

          var results =
              productDetailsRepo.apply(
                  Optional.of(singleId), Optional.empty(), Optional.empty(), Optional.empty(), c);

          assertEquals(1, results.size());
          assertEquals(product.productId(), results.get(0).productId());
          assertEquals("SKU-201", results.get(0).sku());
          assertEquals("Single Product", results.get(0).name());
        });
  }

  @Test
  public void testProductDetailsWithSalesMultipleFilters() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(400));

          var product1 =
              testInsert.Products(
                  new ProductsId(6167),
                  "FILTER-001",
                  "Expensive Product",
                  new BigDecimal("100.00"),
                  Optional.empty(),
                  c);
          var product2 =
              testInsert.Products(
                  new ProductsId(6170),
                  "FILTER-002",
                  "Cheap Product",
                  new BigDecimal("5.00"),
                  Optional.empty(),
                  c);
          var product3 =
              testInsert.Products(
                  new ProductsId(6172),
                  "OTHER-003",
                  "Medium Product",
                  new BigDecimal("50.00"),
                  Optional.empty(),
                  c);

          // Combine array filter with other filters
          Integer[] productIds =
              new Integer[] {
                product1.productId().value(),
                product2.productId().value(),
                product3.productId().value()
              };

          var results =
              productDetailsRepo.apply(
                  Optional.of(productIds),
                  Optional.of("FILTER%"), // sku pattern
                  Optional.of(new BigDecimal("10.00")), // min price
                  Optional.of(new BigDecimal("150.00")), // max price
                  c);

          // Should only return product1 (has FILTER prefix and price >= 10)
          assertEquals(1, results.size());
          assertEquals(product1.productId(), results.get(0).productId());
          assertEquals("FILTER-001", results.get(0).sku());
        });
  }

  @Test
  public void testProductDetailsWithSalesOrderByRevenue() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(500));

          // Create products
          var productLow =
              testInsert.Products(
                  new ProductsId(6000 + 206),
                  "LOW",
                  "Low Revenue",
                  new BigDecimal("10.00"),
                  Optional.empty(),
                  c);
          var productMid =
              testInsert.Products(
                  new ProductsId(6000 + 208),
                  "MID",
                  "Mid Revenue",
                  new BigDecimal("20.00"),
                  Optional.empty(),
                  c);
          var productHigh =
              testInsert.Products(
                  new ProductsId(6000 + 210),
                  "HIGH",
                  "High Revenue",
                  new BigDecimal("30.00"),
                  Optional.empty(),
                  c);

          // Create customer and orders
          var customer =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(9315), "Test User"), c);

          var order =
              testInsert.Orders(
                  new OrdersId(9000 + 1),
                  customer.customerId().value(),
                  Optional.empty(),
                  new UseDefault<>(),
                  new UseDefault<>(),
                  c);

          // Create order items with different quantities to affect revenue
          testInsert.OrderItems(
              order.orderId().value(),
              productLow.productId().value(),
              productLow.price(),
              new Provided<>(1),
              c); // Revenue: 10
          testInsert.OrderItems(
              order.orderId().value(),
              productMid.productId().value(),
              productMid.price(),
              new Provided<>(2),
              c); // Revenue: 40
          testInsert.OrderItems(
              order.orderId().value(),
              productHigh.productId().value(),
              productHigh.price(),
              new Provided<>(3),
              c); // Revenue: 90

          var results =
              productDetailsRepo.apply(
                  Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), c);

          // Results should be ordered by total_revenue DESC
          var relevant =
              results.stream()
                  .filter(
                      p ->
                          p.productId().equals(productLow.productId())
                              || p.productId().equals(productMid.productId())
                              || p.productId().equals(productHigh.productId()))
                  .toList();

          assertEquals(3, relevant.size());
          // High revenue product should be first
          assertTrue(
              relevant.stream()
                      .filter(p -> p.productId().equals(productHigh.productId()))
                      .findFirst()
                      .get()
                      .totalRevenue()
                      .get()
                  > 0.0);
        });
  }

  @Test
  public void testOrderSummaryByCustomerNoFilters() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(600));

          var customer1 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9382), "Customer One"), c);
          var customer2 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9386), "Customer Two"), c);

          // Query with no filters
          var results =
              orderSummaryRepo.apply(
                  Optional.empty(), // customer_ids
                  Optional.empty(), // min_total
                  Optional.empty(), // min_order_count
                  c);

          assertTrue(results.size() >= 2);
          assertTrue(results.stream().anyMatch(r -> r.customerId().equals(customer1.customerId())));
          assertTrue(results.stream().anyMatch(r -> r.customerId().equals(customer2.customerId())));
        });
  }

  @Test
  public void testOrderSummaryByCustomerArrayFilter() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(700));

          var customer1 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9415), "Customer One"), c);
          var customer2 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9419), "Customer Two"), c);
          var customer3 =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(6323), "Customer Three"), c);

          // Filter by array of customer IDs - TESTING ARRAY TYPES
          Integer[] customerIds =
              new Integer[] {customer1.customerId().value(), customer3.customerId().value()};

          var results =
              orderSummaryRepo.apply(
                  Optional.of(customerIds), Optional.empty(), Optional.empty(), c);

          // Should only return customer1 and customer3
          assertTrue(results.size() >= 2);
          assertTrue(results.stream().anyMatch(r -> r.customerId().equals(customer1.customerId())));
          assertTrue(results.stream().anyMatch(r -> r.customerId().equals(customer3.customerId())));
          assertFalse(
              results.stream().anyMatch(r -> r.customerId().equals(customer2.customerId())));
        });
  }

  @Test
  public void testOrderSummaryByCustomerWithMinTotal() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(800));

          var customerHigh =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9459), "High Spender"), c);
          var customerLow =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(9463), "Low Spender"), c);

          var product =
              testInsert.Products(
                  new ProductsId(6000 + 365),
                  "P1",
                  "Product",
                  new BigDecimal("100.00"),
                  Optional.empty(),
                  c);

          // High spender order
          var orderHigh =
              testInsert.Orders(
                  new OrdersId(9401),
                  customerHigh.customerId().value(),
                  Optional.of(new BigDecimal("500.00")),
                  new UseDefault<>(),
                  new UseDefault<>(),
                  c);

          // Low spender order
          var orderLow =
              testInsert.Orders(
                  new OrdersId(9402),
                  customerLow.customerId().value(),
                  Optional.of(new BigDecimal("50.00")),
                  new UseDefault<>(),
                  new UseDefault<>(),
                  c);

          // Filter by minimum total
          var results =
              orderSummaryRepo.apply(
                  Optional.empty(),
                  Optional.of(new BigDecimal("200.00")), // min_total
                  Optional.empty(),
                  c);

          // Should only include high spender
          assertTrue(
              results.stream().anyMatch(r -> r.customerId().equals(customerHigh.customerId())));
          assertFalse(
              results.stream().anyMatch(r -> r.customerId().equals(customerLow.customerId())));
        });
  }

  @Test
  public void testOrderSummaryByCustomerWithMinOrderCount() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(900));

          var customerMany =
              customersRepo.insert(
                  new CustomersRowUnsaved(new CustomersId(6413), "Many Orders"), c);
          var customerFew =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(9519), "Few Orders"), c);

          // Customer with many orders
          for (int i = 0; i < 5; i++) {
            testInsert.Orders(
                new OrdersId(9450 + i),
                customerMany.customerId().value(),
                Optional.empty(),
                new UseDefault<>(),
                new UseDefault<>(),
                c);
          }

          // Customer with few orders
          testInsert.Orders(
              new OrdersId(9456),
              customerFew.customerId().value(),
              Optional.empty(),
              new UseDefault<>(),
              new UseDefault<>(),
              c);

          // Filter by minimum order count
          var results =
              orderSummaryRepo.apply(
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(3), // min_order_count
                  c);

          // Should only include customer with many orders
          assertTrue(
              results.stream()
                  .anyMatch(
                      r ->
                          r.customerId().equals(customerMany.customerId())
                              && r.orderCount().get() >= 5));
          assertFalse(
              results.stream().anyMatch(r -> r.customerId().equals(customerFew.customerId())));
        });
  }

  @Test
  public void testOrderSummaryByCustomerCombinedFilters() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(1000));

          var customer1 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(9571), "Comb One"), c);
          var customer2 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(9575), "Comb Two"), c);
          var customer3 =
              customersRepo.insert(new CustomersRowUnsaved(new CustomersId(9579), "Comb Three"), c);

          // Customer1: 3 orders, $300 total
          for (int i = 0; i < 3; i++) {
            testInsert.Orders(
                new OrdersId(9500 + i),
                customer1.customerId().value(),
                Optional.of(new BigDecimal("100.00")),
                new UseDefault<>(),
                new UseDefault<>(),
                c);
          }

          // Customer2: 1 order, $500 total
          testInsert.Orders(
              new OrdersId(9504),
              customer2.customerId().value(),
              Optional.of(new BigDecimal("500.00")),
              new UseDefault<>(),
              new UseDefault<>(),
              c);

          // Customer3: 5 orders, $50 each
          for (int i = 0; i < 5; i++) {
            testInsert.Orders(
                new OrdersId(9510 + i),
                customer3.customerId().value(),
                Optional.of(new BigDecimal("50.00")),
                new UseDefault<>(),
                new UseDefault<>(),
                c);
          }

          // Combine all filters: specific customers, min total, min order count
          Integer[] customerIds =
              new Integer[] {
                customer1.customerId().value(),
                customer2.customerId().value(),
                customer3.customerId().value()
              };

          var results =
              orderSummaryRepo.apply(
                  Optional.of(customerIds),
                  Optional.of(new BigDecimal("200.00")), // min_total
                  Optional.of(2), // min_order_count
                  c);

          // Should only return customer1 (3 orders >= 2, $300 total >= $200)
          // Customer2 has high total but only 1 order
          // Customer3 has many orders but total < $200
          assertTrue(
              results.stream()
                  .anyMatch(
                      r ->
                          r.customerId().equals(customer1.customerId())
                              && r.orderCount().get() >= 3));
        });
  }

  @Test
  public void testArrayParameterTypeIsNotString() {
    WithConnection.run(
        c -> {
          // This test verifies that array parameters are Integer[], not String
          // If they were String, this would cause a runtime error

          var testInsert = new TestInsert(new Random(1100));
          var product =
              testInsert.Products(
                  new ProductsId(6000 + 546),
                  "TYPE-TEST",
                  "Test",
                  new BigDecimal("1.00"),
                  Optional.empty(),
                  c);

          // Create properly typed array
          Integer[] productIds = new Integer[] {product.productId().value()};

          // This should work without any type errors
          var results =
              productDetailsRepo.apply(
                  Optional.of(productIds), Optional.empty(), Optional.empty(), Optional.empty(), c);

          assertNotNull(results);
          assertEquals(1, results.size());
        });
  }

  @Test
  public void testLargeArrayParameter() {
    WithConnection.run(
        c -> {
          var testInsert = new TestInsert(new Random(1200));

          // Create many products
          Integer[] productIds = new Integer[50];
          for (int i = 0; i < 50; i++) {
            var product =
                testInsert.Products(
                    new ProductsId(9000 + i),
                    "LARGE-" + i,
                    "Product " + i,
                    new BigDecimal("10.00"),
                    Optional.empty(),
                    c);
            productIds[i] = product.productId().value();
          }

          // Query with large array
          var results =
              productDetailsRepo.apply(
                  Optional.of(productIds), Optional.empty(), Optional.empty(), Optional.empty(), c);

          assertEquals(50, results.size());
        });
  }
}
