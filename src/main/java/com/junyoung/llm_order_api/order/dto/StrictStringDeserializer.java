package com.junyoung.llm_order_api.order.dto;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class StrictStringDeserializer extends StdDeserializer<String> {
    public StrictStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            return (String) ctxt.handleUnexpectedToken(getValueType(ctxt), p);
        }
        return p.getString();
    }
}
