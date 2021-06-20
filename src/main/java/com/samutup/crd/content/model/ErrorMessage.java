package com.samutup.crd.content.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorMessage {

    private String errMessage;

    public String getErrMessage() {
        return errMessage;
    }

    public ErrorMessage setErrMessage(String errMessage) {
        this.errMessage = errMessage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorMessage)) {
            return false;
        }
        ErrorMessage that = (ErrorMessage) o;
        return Objects.equal(getErrMessage(), that.getErrMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getErrMessage());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errMessage", errMessage)
                .toString();
    }
}
