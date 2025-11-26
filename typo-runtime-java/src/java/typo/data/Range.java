package typo.data;

import java.util.Optional;

public record Range<T extends Comparable<T>>(
        RangeBound<T> from,
        RangeBound<T> to
) {
    public Optional<RangeFinite<T>> finite() {
        if (from instanceof RangeBound.Finite<T> && to instanceof RangeBound.Finite<T>) {
            return Optional.of(new RangeFinite<>((RangeBound.Finite<T>) from, (RangeBound.Finite<T>) to));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        var left = switch (from) {
            case RangeBound.Infinite<T> x -> "(-";
            case RangeBound.Finite.Open<T> x -> "(" + x.value();
            case RangeBound.Finite.Closed<T> x -> "[" + x.value();
        };
        var right = switch (to) {
            case RangeBound.Infinite<T> x -> ")";
            case RangeBound.Finite.Open<T> x -> x.value() + ")";
            case RangeBound.Finite.Closed<T> x -> x.value() + "]";
        };
        return left + ", " + right;
    }

    public boolean contains(T value) {
        var ord = (Comparable<T>) value;
        var withinRangeLeft = switch (from) {
            case RangeBound.Infinite<T> x -> true;
            case RangeBound.Finite.Open<T> x -> ord.compareTo(x.value()) > 0;
            case RangeBound.Finite.Closed<T> x -> ord.compareTo(x.value()) >= 0;
        };
        var withRangeRight = switch (to) {
            case RangeBound.Infinite<T> x -> true;
            case RangeBound.Finite.Open<T> x -> ord.compareTo(x.value()) < 0;
            case RangeBound.Finite.Closed<T> x -> ord.compareTo(x.value()) <= 0;
        };
        return withinRangeLeft && withRangeRight;
    }
}


