package testdb

import org.junit.Assert.*
import org.junit.Test
import org.mariadb.jdbc.type.*
import testdb.mariatest_spatial.*
import testdb.mariatest_spatial_null.*
import testdb.customtypes.Defaulted

/** Tests for spatial/geometry types in MariaDB. */
class SpatialTypesTest {
    private val spatialRepo = MariatestSpatialRepoImpl()
    private val spatialNullRepo = MariatestSpatialNullRepoImpl()

    /** Create test point */
    private fun createPoint(x: Double, y: Double): Point = Point(x, y)

    /** Create test linestring */
    private fun createLineString(): LineString {
        val points = arrayOf(createPoint(0.0, 0.0), createPoint(1.0, 1.0), createPoint(2.0, 2.0))
        return LineString(points, false)
    }

    /** Create test polygon (simple square) */
    private fun createPolygon(): Polygon {
        val ring = arrayOf(
            createPoint(0.0, 0.0),
            createPoint(0.0, 1.0),
            createPoint(1.0, 1.0),
            createPoint(1.0, 0.0),
            createPoint(0.0, 0.0)
        )
        val rings = arrayOf(LineString(ring, true))
        return Polygon(rings)
    }

    /** Create test multipoint */
    private fun createMultiPoint(): MultiPoint {
        val points = arrayOf(createPoint(0.0, 0.0), createPoint(1.0, 1.0), createPoint(2.0, 2.0))
        return MultiPoint(points)
    }

    /** Create test multilinestring */
    private fun createMultiLineString(): MultiLineString {
        val lines = arrayOf(createLineString())
        return MultiLineString(lines)
    }

    /** Create test multipolygon */
    private fun createMultiPolygon(): MultiPolygon {
        val polygons = arrayOf(createPolygon())
        return MultiPolygon(polygons)
    }

    /** Create test geometry collection */
    private fun createGeometryCollection(): GeometryCollection {
        val geometries: Array<Geometry> = arrayOf(createPoint(5.0, 5.0), createPoint(6.0, 6.0))
        return GeometryCollection(geometries)
    }

    @Test
    fun testPointInsertAndSelect() {
        MariaDbTestHelper.run { c ->
            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(1.0, 2.0), // geometry_col (Point is a subtype of Geometry)
                createPoint(3.0, 4.0), // point_col
                createLineString(),
                createPolygon(),
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            assertNotNull(inserted)
            assertNotNull(inserted.id)
            assertNotNull(inserted.pointCol)

            // Verify point coordinates
            assertEquals(3.0, inserted.pointCol.x, 0.001)
            assertEquals(4.0, inserted.pointCol.y, 0.001)

            // Select back
            val found = spatialRepo.selectById(inserted.id, c)
            assertNotNull(found)
            assertEquals(3.0, found!!.pointCol.x, 0.001)
            assertEquals(4.0, found.pointCol.y, 0.001)
        }
    }

    @Test
    fun testLineStringInsertAndSelect() {
        MariaDbTestHelper.run { c ->
            val linestring = createLineString()

            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(0.0, 0.0),
                linestring,
                createPolygon(),
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            assertNotNull(inserted.linestringCol)
            // LineString should have 3 points
            assertTrue(inserted.linestringCol.points.isNotEmpty())

            // Select back
            val found = spatialRepo.selectById(inserted.id, c)!!
            assertNotNull(found.linestringCol)
        }
    }

    @Test
    fun testPolygonInsertAndSelect() {
        MariaDbTestHelper.run { c ->
            val polygon = createPolygon()

            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(0.0, 0.0),
                createLineString(),
                polygon,
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            assertNotNull(inserted.polygonCol)

            // Select back
            val found = spatialRepo.selectById(inserted.id, c)!!
            assertNotNull(found.polygonCol)
        }
    }

    @Test
    fun testMultiPointInsertAndSelect() {
        MariaDbTestHelper.run { c ->
            val multipoint = createMultiPoint()

            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(0.0, 0.0),
                createLineString(),
                createPolygon(),
                multipoint,
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            assertNotNull(inserted.multipointCol)

            // Select back
            val found = spatialRepo.selectById(inserted.id, c)!!
            assertNotNull(found.multipointCol)
        }
    }

    @Test
    fun testGeometryCollectionInsertAndSelect() {
        MariaDbTestHelper.run { c ->
            val collection = createGeometryCollection()

            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(0.0, 0.0),
                createLineString(),
                createPolygon(),
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                collection
            )

            val inserted = spatialRepo.insert(unsaved, c)

            assertNotNull(inserted.geometrycollectionCol)

            // Select back
            val found = spatialRepo.selectById(inserted.id, c)!!
            assertNotNull(found.geometrycollectionCol)
        }
    }

    @Test
    fun testNullableSpatialWithAllNulls() {
        MariaDbTestHelper.run { c ->
            // Use short constructor that sets all fields to UseDefault
            val unsaved = MariatestSpatialNullRowUnsaved()

            val inserted = spatialNullRepo.insert(unsaved, c)

            assertNotNull(inserted)
            assertNotNull(inserted.id)

            // All spatial columns should be null
            assertNull(inserted.geometryCol)
            assertNull(inserted.pointCol)
            assertNull(inserted.linestringCol)
            assertNull(inserted.polygonCol)
            assertNull(inserted.multipointCol)
            assertNull(inserted.multilinestringCol)
            assertNull(inserted.multipolygonCol)
            assertNull(inserted.geometrycollectionCol)
        }
    }

    @Test
    fun testNullableSpatialWithValues() {
        MariaDbTestHelper.run { c ->
            val point = createPoint(10.0, 20.0)

            // Use short constructor and chain copy methods
            val unsaved = MariatestSpatialNullRowUnsaved(
                geometryCol = Defaulted.Provided(point),
                pointCol = Defaulted.Provided(point)
            )

            val inserted = spatialNullRepo.insert(unsaved, c)

            assertNotNull(inserted)
            assertNotNull(inserted.geometryCol)
            assertNotNull(inserted.pointCol)

            assertEquals(10.0, inserted.pointCol!!.x, 0.001)
            assertEquals(20.0, inserted.pointCol!!.y, 0.001)
        }
    }

    @Test
    fun testSpatialUpdate() {
        MariaDbTestHelper.run { c ->
            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(1.0, 1.0),
                createLineString(),
                createPolygon(),
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            // Update point
            val newPoint = createPoint(100.0, 200.0)
            val updated = inserted.copy(pointCol = newPoint)
            spatialRepo.update(updated, c)

            // Verify update
            val found = spatialRepo.selectById(inserted.id, c)!!
            assertEquals(100.0, found.pointCol.x, 0.001)
            assertEquals(200.0, found.pointCol.y, 0.001)
        }
    }

    @Test
    fun testSpatialDelete() {
        MariaDbTestHelper.run { c ->
            val unsaved = MariatestSpatialRowUnsaved(
                createPoint(0.0, 0.0),
                createPoint(1.0, 1.0),
                createLineString(),
                createPolygon(),
                createMultiPoint(),
                createMultiLineString(),
                createMultiPolygon(),
                createGeometryCollection()
            )

            val inserted = spatialRepo.insert(unsaved, c)

            val deleted = spatialRepo.deleteById(inserted.id, c)
            assertTrue(deleted)

            val found = spatialRepo.selectById(inserted.id, c)
            assertNull(found)
        }
    }
}
