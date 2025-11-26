package typo.dsl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Context for rendering SQL expressions to fragments.
 * Calculates aliases for all unique list of Paths in a select query.
 * This is used to evaluate expressions from an SqlExpr when we have joined relations.
 */
public class RenderCtx {

    // Map from path to alias
    private final Map<List<Path>, String> aliasMap;

    private RenderCtx(Map<List<Path>, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    // Empty render context singleton
    public static final RenderCtx EMPTY = new RenderCtx(Map.of());

    // Create context from a SelectBuilder
    public static RenderCtx from(SelectBuilder<?, ?> builder) {
        if (!(builder instanceof SelectBuilderSql<?, ?> sqlBuilder)) {
            return EMPTY;
        }
        return fromSql(sqlBuilder);
    }

    private static RenderCtx fromSql(SelectBuilderSql<?, ?> builder) {
        List<PathAndName> pathsAndNames = findPathsAndTableNames(builder);

        // Group by name and assign unique indexed aliases
        Map<String, List<List<Path>>> byName = pathsAndNames.stream()
            .collect(Collectors.groupingBy(
                PathAndName::name,
                LinkedHashMap::new,
                Collectors.mapping(PathAndName::path, Collectors.toList())
            ));

        Map<List<Path>, String> aliasMap = new HashMap<>();

        for (Map.Entry<String, List<List<Path>>> entry : byName.entrySet()) {
            String baseName = entry.getKey();
            List<List<Path>> paths = entry.getValue();

            // Sort paths for deterministic alias assignment
            paths.sort(RenderCtx::comparePaths);

            for (int i = 0; i < paths.size(); i++) {
                aliasMap.put(paths.get(i), baseName + i);
            }
        }

        return new RenderCtx(aliasMap);
    }

    private static List<PathAndName> findPathsAndTableNames(SelectBuilderSql<?, ?> builder) {
        List<PathAndName> result = new ArrayList<>();
        findPathsAndTableNamesRecursive(builder, result);
        return result;
    }

    private static void findPathsAndTableNamesRecursive(SelectBuilderSql<?, ?> builder, List<PathAndName> result) {
        if (builder instanceof SelectBuilderSql.Relation<?, ?> relation) {
            // Extract table name and filter to alphanumeric chars
            String tableName = filterAlphanumeric(relation.name());
            result.add(new PathAndName(relation.structure().path(), tableName));
        } else if (builder instanceof SelectBuilderSql.TableJoin<?, ?, ?, ?> join) {
            // Add entry for the join itself
            result.add(new PathAndName(join.structure().path(), "join_cte"));
            // Recursively process left and right
            findPathsAndTableNamesRecursive(join.left(), result);
            findPathsAndTableNamesRecursive(join.right(), result);
        } else if (builder instanceof SelectBuilderSql.TableLeftJoin<?, ?, ?, ?> leftJoin) {
            // Add entry for the left join itself
            result.add(new PathAndName(leftJoin.structure().path(), "left_join_cte"));
            // Recursively process left and right
            findPathsAndTableNamesRecursive(leftJoin.left(), result);
            findPathsAndTableNamesRecursive(leftJoin.right(), result);
        }
    }

    private static String filterAlphanumeric(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int comparePaths(List<Path> a, List<Path> b) {
        int minLen = Math.min(a.size(), b.size());
        for (int i = 0; i < minLen; i++) {
            int cmp = comparePath(a.get(i), b.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(a.size(), b.size());
    }

    private static int comparePath(Path a, Path b) {
        // Define ordering: LeftInJoin < Named < RightInJoin
        int aOrd = pathOrdinal(a);
        int bOrd = pathOrdinal(b);
        if (aOrd != bOrd) return Integer.compare(aOrd, bOrd);

        // If both are Named, compare by value
        if (a instanceof Path.Named(String value) && b instanceof Path.Named(String value1)) {
            return value.compareTo(value1);
        }
        return 0;
    }

    private static int pathOrdinal(Path p) {
        return switch (p) {
            case Path.LeftInJoin l -> 0;
            case Path.Named n -> 1;
            case Path.RightInJoin r -> 2;
        };
    }

    /**
     * Get alias for a path. Returns Optional.empty() if path not found.
     */
    public Optional<String> alias(List<Path> path) {
        return Optional.ofNullable(aliasMap.get(path));
    }

    /**
     * Get alias for a single path element.
     */
    public Optional<String> alias(Path path) {
        return alias(List.of(path));
    }

    // Internal record to hold path and table name pairs
    private record PathAndName(List<Path> path, String name) {}
}
