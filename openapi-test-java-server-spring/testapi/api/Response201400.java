package testapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Generic response type for shape: 201, 400 */
public sealed interface Response201400<T201, T400> {
  record Status201<T201, T400>(@JsonProperty("value") T201 value) implements Response201400<T201, T400> {
    public Status201<T201, T400> withValue(T201 value) {
      return new Status201<>(value);
    };

    @Override
    public String status() {
      return "201";
    };
  };

  record Status400<T201, T400>(@JsonProperty("value") T400 value) implements Response201400<T201, T400> {
    public Status400<T201, T400> withValue(T400 value) {
      return new Status400<>(value);
    };

    @Override
    public String status() {
      return "400";
    };
  };

  @JsonProperty("status")
  String status();
}