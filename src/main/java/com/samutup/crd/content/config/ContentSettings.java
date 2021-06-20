package com.samutup.crd.content.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.samutup.crd.content.model.ResourcePath;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ContentSettings {

    private List<ResourcePath> resourcePaths;
    private EngineSettings engineSettings;

    @JsonCreator
    public ContentSettings(
            @JsonProperty(value = "kube_resources")
                    List<ResourcePath> resourcePaths,
        @JsonProperty(value = "engine")
            EngineSettings engineSettings) {
        this.resourcePaths = resourcePaths;
        this.engineSettings=engineSettings;

    }

    public List<ResourcePath> getResourcePaths() {
        return resourcePaths;
    }

    public void setResourcePaths(List<ResourcePath> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public EngineSettings getEngineSettings() {
        return engineSettings;
    }

    public void setEngineSettings(EngineSettings engineSettings) {
        this.engineSettings = engineSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContentSettings that = (ContentSettings) o;
        return Objects.equal(resourcePaths, that.resourcePaths)
            && Objects.equal(engineSettings, that.engineSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resourcePaths, engineSettings);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("resourcePaths", resourcePaths)
            .add("engineSettings", engineSettings)
            .toString();
    }
}
