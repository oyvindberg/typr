package typo.dsl;

import typo.dsl.internal.DummyComparator;

import java.util.Comparator;
import java.util.function.BiFunction;

public record SqlOperator<T1, T2, O>(String op, BiFunction<T1, T2, O> eval) {
    
    // Standard operators
    public static <T> SqlOperator<T, T, Boolean> eq() {
        return new SqlOperator<>("=", Object::equals);
    }
    
    public static <T> SqlOperator<T, T, Boolean> neq() {
        return new SqlOperator<>("!=", (a, b) -> !a.equals(b));
    }
    
    public static <T> SqlOperator<T, T, Boolean> gt() {
        Comparator<T> comparator = DummyComparator.typed();
        return new SqlOperator<>(">", (a, b) -> comparator.compare(a, b) > 0);
    }
    
    public static <T> SqlOperator<T, T, Boolean> gte() {
        Comparator<T> comparator = DummyComparator.typed();
        return new SqlOperator<>(">=", (a, b) -> comparator.compare(a, b) >= 0);
    }
    
    public static <T> SqlOperator<T, T, Boolean> lt() {
        Comparator<T> comparator = DummyComparator.typed();
        return new SqlOperator<>("<", (a, b) -> comparator.compare(a, b) < 0);
    }
    
    public static <T> SqlOperator<T, T, Boolean> lte() {
        Comparator<T> comparator = DummyComparator.typed();
        return new SqlOperator<>("<=", (a, b) -> comparator.compare(a, b) <= 0);
    }
    
    public static <T> SqlOperator<T, T, T> or(Bijection<T, Boolean> bijection) {
        return new SqlOperator<>("OR", (a, b) -> {
            Boolean b1 = bijection.underlying(a);
            Boolean b2 = bijection.underlying(b);
            return bijection.from(b1 || b2);
        });
    }
    
    public static <T> SqlOperator<T, T, T> and(Bijection<T, Boolean> bijection) {
        return new SqlOperator<>("AND", (a, b) -> {
            Boolean b1 = bijection.underlying(a);
            Boolean b2 = bijection.underlying(b);
            return bijection.from(b1 && b2);
        });
    }
    
    // For SQL generation, we don't need the actual implementation
    // Mock implementations can provide their own operators with numeric instances
    public static <T> SqlOperator<T, T, T> plus() {
        return new SqlOperator<>("+", (a, b) -> {
            throw new UnsupportedOperationException(
                "Numeric evaluation not supported. Use mock-specific operators with Numeric instances."
            );
        });
    }
    
    public static <T> SqlOperator<T, T, T> minus() {
        return new SqlOperator<>("-", (a, b) -> {
            throw new UnsupportedOperationException(
                "Numeric evaluation not supported. Use mock-specific operators with Numeric instances."
            );
        });
    }
    
    public static <T> SqlOperator<T, T, T> mul() {
        return new SqlOperator<>("*", (a, b) -> {
            throw new UnsupportedOperationException(
                "Numeric evaluation not supported. Use mock-specific operators with Numeric instances."
            );
        });
    }
    
    public static <T> SqlOperator<T, String, Boolean> like(Bijection<T, String> bijection) {
        return new SqlOperator<>("LIKE", (value, pattern) -> 
            pattern.contains(bijection.underlying(value))
        );
    }
    
    public static <T> SqlOperator<T, T, T> strAdd(Bijection<T, String> bijection) {
        return new SqlOperator<>("||", (a, b) -> {
            String s1 = bijection.underlying(a);
            String s2 = bijection.underlying(b);
            return bijection.from(s1 + s2);
        });
    }
}