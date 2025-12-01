package testapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Generic response type for shape: 200, 404 */
public sealed interface Response200404<T200, T404> {
  record Status200<T200, T404>(@JsonProperty("value") T200 value) implements Response200404<T200, T404> {
    public Status200<T200, T404> withValue(T200 value) {
      return new Status200<>(value);
    };

    @Override
    public String status() {
      return "200";
    };
  };

  record Status404<T200, T404>(@JsonProperty("value") T404 value) implements Response200404<T200, T404> {
    public Status404<T200, T404> withValue(T404 value) {
      return new Status404<>(value);
    };

    @Override
    public String status() {
      return "404";
    };
  };

  @JsonProperty("status")
  String status();
}