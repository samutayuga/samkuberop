package com.samutup.crd.content.config;

public enum ContentConstant {

    SERVER_CONFIG("server_config", "server_config");

    final public String configVal;

    final public String cofigKey;

    ContentConstant(String configKey, String configValue) {
        this.cofigKey = configKey;
        this.configVal = configValue;
    }


}
