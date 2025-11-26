package adventureworks.production.unitmeasure;

import adventureworks.WithConnection;
import adventureworks.customtypes.TypoLocalDateTime;
import adventureworks.public_.Name;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests upsertStreaming and upsertBatch functionality - equivalent to Scala RepoTest.
 */
public class RepoTest {

    private void upsertStreaming(UnitmeasureRepo unitmeasureRepo) {
        WithConnection.run(c -> {
            var um1 = new UnitmeasureRow(new UnitmeasureId("kg1"), new Name("name1"), TypoLocalDateTime.now());
            var um2 = new UnitmeasureRow(new UnitmeasureId("kg2"), new Name("name2"), TypoLocalDateTime.now());
            unitmeasureRepo.upsertStreaming(List.of(um1, um2).iterator(), 1000, c);

            var all1 = unitmeasureRepo.selectAll(c).stream()
                    .sorted(Comparator.comparing(r -> r.name().value()))
                    .toList();
            assertEquals(List.of(um1, um2), all1);

            var um1a = um1.withName(new Name("name1a"));
            var um2a = um2.withName(new Name("name2a"));
            unitmeasureRepo.upsertStreaming(List.of(um1a, um2a).iterator(), 1000, c);

            var all2 = unitmeasureRepo.selectAll(c).stream()
                    .sorted(Comparator.comparing(r -> r.name().value()))
                    .toList();
            assertEquals(List.of(um1a, um2a), all2);
        });
    }

    private void upsertBatch(UnitmeasureRepo unitmeasureRepo) {
        WithConnection.run(c -> {
            var um1 = new UnitmeasureRow(new UnitmeasureId("kg1"), new Name("name1"), TypoLocalDateTime.now());
            var um2 = new UnitmeasureRow(new UnitmeasureId("kg2"), new Name("name2"), TypoLocalDateTime.now());
            var initial = unitmeasureRepo.upsertBatch(List.of(um1, um2).iterator(), c).stream()
                    .sorted(Comparator.comparing(r -> r.name().value()))
                    .toList();
            assertEquals(List.of(um1, um2), initial);

            var um1a = um1.withName(new Name("name1a"));
            var um2a = um2.withName(new Name("name2a"));
            var returned = unitmeasureRepo.upsertBatch(List.of(um1a, um2a).iterator(), c).stream()
                    .sorted(Comparator.comparing(r -> r.name().value()))
                    .toList();
            assertEquals(List.of(um1a, um2a), returned);

            var all = unitmeasureRepo.selectAll(c).stream()
                    .sorted(Comparator.comparing(r -> r.name().value()))
                    .toList();
            assertEquals(List.of(um1a, um2a), all);
        });
    }

    @Test
    public void upsertStreamingInMemory() {
        upsertStreaming(new UnitmeasureRepoMock(unsaved -> unsaved.toRow(() -> TypoLocalDateTime.now())));
    }

    @Test
    public void upsertStreamingPg() {
        upsertStreaming(new UnitmeasureRepoImpl());
    }

    @Test
    public void upsertBatchInMemory() {
        upsertBatch(new UnitmeasureRepoMock(unsaved -> unsaved.toRow(() -> TypoLocalDateTime.now())));
    }

    @Test
    public void upsertBatchPg() {
        upsertBatch(new UnitmeasureRepoImpl());
    }
}
