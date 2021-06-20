package com.samutup.crd.content.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EngineSettings {

  private int httpport;

  public int getHttpport() {
    return httpport;
  }

  public void setHttpport(int httpport) {
    this.httpport = httpport;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EngineSettings that = (EngineSettings) o;
    return httpport == that.httpport;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(httpport);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("httpport", httpport)
        .toString();
  }
}
