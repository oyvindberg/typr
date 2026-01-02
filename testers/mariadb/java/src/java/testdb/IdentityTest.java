package testdb;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import testdb.mariatest_identity.*;

/** Tests for AUTO_INCREMENT columns in MariaDB. */
public class IdentityTest {
  private final MariatestIdentityRepoImpl repo = new MariatestIdentityRepoImpl();

  @Test
  public void testAutoIncrementInsert() {
    MariaDbTestHelper.run(
        c -> {
          // Insert using unsaved row (without id)
          var unsaved = new MariatestIdentityRowUnsaved("First Row");
          var inserted = repo.insert(unsaved, c);

          assertNotNull(inserted);
          assertNotNull(inserted.id());
          assertEquals("First Row", inserted.name());

          // Insert another row - id should be different
          var unsaved2 = new MariatestIdentityRowUnsaved("Second Row");
          var inserted2 = repo.insert(unsaved2, c);

          assertNotNull(inserted2);
          assertNotNull(inserted2.id());
          assertNotEquals(inserted.id(), inserted2.id());
          assertEquals("Second Row", inserted2.name());
        });
  }

  @Test
  public void testSelectById() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved = new MariatestIdentityRowUnsaved("Select Test");
          var inserted = repo.insert(unsaved, c);

          var found = repo.selectById(inserted.id(), c);
          assertTrue(found.isPresent());
          assertEquals(inserted.id(), found.get().id());
          assertEquals("Select Test", found.get().name());
        });
  }

  @Test
  public void testUpdate() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved = new MariatestIdentityRowUnsaved("Original Name");
          var inserted = repo.insert(unsaved, c);

          var updated = inserted.withName("Updated Name");
          boolean wasUpdated = repo.update(updated, c);
          assertTrue(wasUpdated);

          var found = repo.selectById(inserted.id(), c).orElseThrow();
          assertEquals("Updated Name", found.name());
        });
  }

  @Test
  public void testDelete() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved = new MariatestIdentityRowUnsaved("To Delete");
          var inserted = repo.insert(unsaved, c);

          boolean deleted = repo.deleteById(inserted.id(), c);
          assertTrue(deleted);

          var found = repo.selectById(inserted.id(), c);
          assertFalse(found.isPresent());
        });
  }

  @Test
  public void testUpsert() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved = new MariatestIdentityRowUnsaved("Upsert Test");
          var inserted = repo.insert(unsaved, c);

          // Upsert same row with updated name
          var upsertRow = inserted.withName("Upserted Name");
          var upserted = repo.upsert(upsertRow, c);

          assertEquals(inserted.id(), upserted.id());
          assertEquals("Upserted Name", upserted.name());

          // Verify in database
          var found = repo.selectById(inserted.id(), c).orElseThrow();
          assertEquals("Upserted Name", found.name());
        });
  }

  @Test
  public void testSelectAll() {
    MariaDbTestHelper.run(
        c -> {
          repo.insert(new MariatestIdentityRowUnsaved("Row A"), c);
          repo.insert(new MariatestIdentityRowUnsaved("Row B"), c);
          repo.insert(new MariatestIdentityRowUnsaved("Row C"), c);

          List<MariatestIdentityRow> all = repo.selectAll(c);
          assertTrue(all.size() >= 3);
        });
  }

  @Test
  public void testToUnsavedRow() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved = new MariatestIdentityRowUnsaved("Test Name");
          var inserted = repo.insert(unsaved, c);

          // Convert back to unsaved row
          var backToUnsaved = inserted.toUnsavedRow();
          assertEquals("Test Name", backToUnsaved.name());

          // Insert the unsaved row again - should get a new ID
          var inserted2 = repo.insert(backToUnsaved, c);
          assertNotEquals(inserted.id(), inserted2.id());
          assertEquals(inserted.name(), inserted2.name());
        });
  }
}
