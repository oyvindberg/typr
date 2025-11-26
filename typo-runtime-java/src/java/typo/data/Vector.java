package typo.data;

import java.util.Arrays;

public record Vector(short[] values) {
    static Vector parse(String value) {
        var values = value.split(" ");
        var ret = new short[values.length];
        for (var i = 0; i < values.length; i++) {
            ret[i] = Short.parseShort(values[i]);
        }
        return new Vector(ret);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector other) {
            if (values.length != other.values.length) {
                return false;
            }
            for (var i = 0; i < values.length; i++) {
                if (values[i] != other.values[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String value() {
        var sb = new StringBuilder();
        for (var i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(values[i]);
        }
        return sb.toString();
    }

    public Vector(String value) {
        this(Vector.parse(value).values);
    }
}
