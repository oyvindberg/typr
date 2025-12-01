package testapi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import testapi.model.Error;

/** Generic response type for shape: 200, 4XX, 5XX */
public sealed interface Response2004XX5XX<T200> {
  record Status200<T200>(@JsonProperty("value") T200 value) implements Response2004XX5XX<T200> {
    public Status200<T200> withValue(T200 value) {
      return new Status200<>(value);
    };

    @Override
    public String status() {
      return "200";
    };
  };

  record Status4XX<T200>(
    /** HTTP status code */
    @JsonProperty("statusCode") Integer statusCode,
    @JsonProperty("value") Error value
  ) implements Response2004XX5XX<T200> {
    /** HTTP status code */
    public Status4XX<T200> withStatusCode(Integer statusCode) {
      return new Status4XX<>(statusCode, value);
    };

    public Status4XX<T200> withValue(Error value) {
      return new Status4XX<>(statusCode, value);
    };

    @Override
    public String status() {
      return "4XX";
    };
  };

  record Status5XX<T200>(
    /** HTTP status code */
    @JsonProperty("statusCode") Integer statusCode,
    @JsonProperty("value") Error value
  ) implements Response2004XX5XX<T200> {
    /** HTTP status code */
    public Status5XX<T200> withStatusCode(Integer statusCode) {
      return new Status5XX<>(statusCode, value);
    };

    public Status5XX<T200> withValue(Error value) {
      return new Status5XX<>(statusCode, value);
    };

    @Override
    public String status() {
      return "5XX";
    };
  };

  @JsonProperty("status")
  String status();
}