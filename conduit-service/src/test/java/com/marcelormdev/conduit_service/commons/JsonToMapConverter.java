package com.marcelormdev.conduit_service.commons;

import java.util.Map;

import tools.jackson.databind.ObjectMapper;

public class JsonToMapConverter<T> {

    public T convert(String json) {
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.readValue(json, Map.class);
    }

}
