package typo.dsl;

import java.util.function.Function;

public record SqlFunction1<T1, O>(String name, Function<T1, O> eval) {
    
    public static <T> SqlFunction1<T, T> lower(Bijection<T, String> bijection) {
        return new SqlFunction1<>("lower", value -> 
            bijection.from(bijection.underlying(value).toLowerCase())
        );
    }
    
    public static <T> SqlFunction1<T, T> upper(Bijection<T, String> bijection) {
        return new SqlFunction1<>("upper", value -> 
            bijection.from(bijection.underlying(value).toUpperCase())
        );
    }
    
    public static <T> SqlFunction1<T, T> reverse(Bijection<T, String> bijection) {
        return new SqlFunction1<>("reverse", value -> 
            bijection.from(new StringBuilder(bijection.underlying(value)).reverse().toString())
        );
    }
    
    public static <T> SqlFunction1<T, Integer> length(Bijection<T, String> bijection) {
        return new SqlFunction1<>("length", value -> 
            bijection.underlying(value).length()
        );
    }
    
    public static <T> SqlFunction1<T[], Integer> arrayLength() {
        return new SqlFunction1<>("array_length", array -> array.length);
    }
}