package testdb;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import org.mariadb.jdbc.type.Geometry;
import org.mariadb.jdbc.type.GeometryCollection;
import org.mariadb.jdbc.type.LineString;
import org.mariadb.jdbc.type.MultiLineString;
import org.mariadb.jdbc.type.MultiPoint;
import org.mariadb.jdbc.type.MultiPolygon;
import org.mariadb.jdbc.type.Point;
import org.mariadb.jdbc.type.Polygon;
import testdb.customtypes.Defaulted.Provided;
import testdb.mariatest_spatial.*;
import testdb.mariatest_spatial_null.*;

/** Tests for spatial/geometry types in MariaDB. */
public class SpatialTypesTest {
  private final MariatestSpatialRepoImpl spatialRepo = new MariatestSpatialRepoImpl();
  private final MariatestSpatialNullRepoImpl spatialNullRepo = new MariatestSpatialNullRepoImpl();

  /** Create test point */
  private Point createPoint(double x, double y) {
    return new Point(x, y);
  }

  /** Create test linestring */
  private LineString createLineString() {
    Point[] points = new Point[] {createPoint(0, 0), createPoint(1, 1), createPoint(2, 2)};
    return new LineString(points, false);
  }

  /** Create test polygon (simple square) */
  private Polygon createPolygon() {
    Point[] ring =
        new Point[] {
          createPoint(0, 0),
          createPoint(0, 1),
          createPoint(1, 1),
          createPoint(1, 0),
          createPoint(0, 0)
        };
    LineString[] rings = new LineString[] {new LineString(ring, true)};
    return new Polygon(rings);
  }

  /** Create test multipoint */
  private MultiPoint createMultiPoint() {
    Point[] points = new Point[] {createPoint(0, 0), createPoint(1, 1), createPoint(2, 2)};
    return new MultiPoint(points);
  }

  /** Create test multilinestring */
  private MultiLineString createMultiLineString() {
    LineString[] lines = new LineString[] {createLineString()};
    return new MultiLineString(lines);
  }

  /** Create test multipolygon */
  private MultiPolygon createMultiPolygon() {
    Polygon[] polygons = new Polygon[] {createPolygon()};
    return new MultiPolygon(polygons);
  }

  /** Create test geometry collection */
  private GeometryCollection createGeometryCollection() {
    Geometry[] geometries = new Geometry[] {createPoint(5, 5), createPoint(6, 6)};
    return new GeometryCollection(geometries);
  }

  @Test
  public void testPointInsertAndSelect() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(1, 2), // geometry_col (Point is a subtype of Geometry)
                  createPoint(3, 4), // point_col
                  createLineString(),
                  createPolygon(),
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          assertNotNull(inserted);
          assertNotNull(inserted.id());
          assertNotNull(inserted.pointCol());

          // Verify point coordinates
          assertEquals(3.0, inserted.pointCol().getX(), 0.001);
          assertEquals(4.0, inserted.pointCol().getY(), 0.001);

          // Select back
          var found = spatialRepo.selectById(inserted.id(), c);
          assertTrue(found.isPresent());
          assertEquals(3.0, found.get().pointCol().getX(), 0.001);
          assertEquals(4.0, found.get().pointCol().getY(), 0.001);
        });
  }

  @Test
  public void testLineStringInsertAndSelect() {
    MariaDbTestHelper.run(
        c -> {
          var linestring = createLineString();

          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(0, 0),
                  linestring,
                  createPolygon(),
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          assertNotNull(inserted.linestringCol());
          // LineString should have 3 points
          assertTrue(inserted.linestringCol().getPoints().length > 0);

          // Select back
          var found = spatialRepo.selectById(inserted.id(), c).orElseThrow();
          assertNotNull(found.linestringCol());
        });
  }

  @Test
  public void testPolygonInsertAndSelect() {
    MariaDbTestHelper.run(
        c -> {
          var polygon = createPolygon();

          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(0, 0),
                  createLineString(),
                  polygon,
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          assertNotNull(inserted.polygonCol());

          // Select back
          var found = spatialRepo.selectById(inserted.id(), c).orElseThrow();
          assertNotNull(found.polygonCol());
        });
  }

  @Test
  public void testMultiPointInsertAndSelect() {
    MariaDbTestHelper.run(
        c -> {
          var multipoint = createMultiPoint();

          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(0, 0),
                  createLineString(),
                  createPolygon(),
                  multipoint,
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          assertNotNull(inserted.multipointCol());

          // Select back
          var found = spatialRepo.selectById(inserted.id(), c).orElseThrow();
          assertNotNull(found.multipointCol());
        });
  }

  @Test
  public void testGeometryCollectionInsertAndSelect() {
    MariaDbTestHelper.run(
        c -> {
          var collection = createGeometryCollection();

          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(0, 0),
                  createLineString(),
                  createPolygon(),
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  collection);

          var inserted = spatialRepo.insert(unsaved, c);

          assertNotNull(inserted.geometrycollectionCol());

          // Select back
          var found = spatialRepo.selectById(inserted.id(), c).orElseThrow();
          assertNotNull(found.geometrycollectionCol());
        });
  }

  @Test
  public void testNullableSpatialWithAllNulls() {
    MariaDbTestHelper.run(
        c -> {
          // Use short constructor that sets all fields to UseDefault
          var unsaved = new MariatestSpatialNullRowUnsaved();

          var inserted = spatialNullRepo.insert(unsaved, c);

          assertNotNull(inserted);
          assertNotNull(inserted.id());

          // All spatial columns should be empty
          assertTrue(inserted.geometryCol().isEmpty());
          assertTrue(inserted.pointCol().isEmpty());
          assertTrue(inserted.linestringCol().isEmpty());
          assertTrue(inserted.polygonCol().isEmpty());
          assertTrue(inserted.multipointCol().isEmpty());
          assertTrue(inserted.multilinestringCol().isEmpty());
          assertTrue(inserted.multipolygonCol().isEmpty());
          assertTrue(inserted.geometrycollectionCol().isEmpty());
        });
  }

  @Test
  public void testNullableSpatialWithValues() {
    MariaDbTestHelper.run(
        c -> {
          var point = createPoint(10, 20);

          // Use short constructor and chain withXxx methods
          var unsaved =
              new MariatestSpatialNullRowUnsaved()
                  .withGeometryCol(new Provided<>(Optional.of(point)))
                  .withPointCol(new Provided<>(Optional.of(point)));

          var inserted = spatialNullRepo.insert(unsaved, c);

          assertNotNull(inserted);
          assertTrue(inserted.geometryCol().isPresent());
          assertTrue(inserted.pointCol().isPresent());

          assertEquals(10.0, inserted.pointCol().get().getX(), 0.001);
          assertEquals(20.0, inserted.pointCol().get().getY(), 0.001);
        });
  }

  @Test
  public void testSpatialUpdate() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(1, 1),
                  createLineString(),
                  createPolygon(),
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          // Update point
          var newPoint = createPoint(100, 200);
          var updated = inserted.withPointCol(newPoint);
          spatialRepo.update(updated, c);

          // Verify update
          var found = spatialRepo.selectById(inserted.id(), c).orElseThrow();
          assertEquals(100.0, found.pointCol().getX(), 0.001);
          assertEquals(200.0, found.pointCol().getY(), 0.001);
        });
  }

  @Test
  public void testSpatialDelete() {
    MariaDbTestHelper.run(
        c -> {
          var unsaved =
              new MariatestSpatialRowUnsaved(
                  createPoint(0, 0),
                  createPoint(1, 1),
                  createLineString(),
                  createPolygon(),
                  createMultiPoint(),
                  createMultiLineString(),
                  createMultiPolygon(),
                  createGeometryCollection());

          var inserted = spatialRepo.insert(unsaved, c);

          boolean deleted = spatialRepo.deleteById(inserted.id(), c);
          assertTrue(deleted);

          var found = spatialRepo.selectById(inserted.id(), c);
          assertFalse(found.isPresent());
        });
  }
}
