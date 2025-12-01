package testapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import testapi.model.Error;

/** Generic response type for shape: 404, default */
public sealed interface Response404Default<T404> {
  record Status404<T404>(@JsonProperty("value") T404 value) implements Response404Default<T404> {
    public Status404<T404> withValue(T404 value) {
      return new Status404<>(value);
    };

    @Override
    public String status() {
      return "404";
    };
  };

  record StatusDefault<T404>(
    /** HTTP status code */
    @JsonProperty("statusCode") Integer statusCode,
    @JsonProperty("value") Error value
  ) implements Response404Default<T404> {
    /** HTTP status code */
    public StatusDefault<T404> withStatusCode(Integer statusCode) {
      return new StatusDefault<>(statusCode, value);
    };

    public StatusDefault<T404> withValue(Error value) {
      return new StatusDefault<>(statusCode, value);
    };

    @Override
    public String status() {
      return "default";
    };
  };

  @JsonProperty("status")
  String status();
}