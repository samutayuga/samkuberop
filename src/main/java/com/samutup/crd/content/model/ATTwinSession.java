package com.samutup.crd.content.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class atwinSession {

  Map<String, String> payload;


  public Map<String, String> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, String> payload) {
    this.payload = payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    atwinSession that = (atwinSession) o;
    return Objects.equal(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(payload);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("payload", payload)
        .toString();
  }
}
