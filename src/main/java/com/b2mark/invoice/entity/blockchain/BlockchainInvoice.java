package com.b2mark.invoice.entity.blockchain;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amount",
        "invoiceId",
        "trasnactionId",
        "requestStatus",
        "address",
        "expiredDateTime",
        "depositeDateTime",
        "createtedDateTiem",
        "trasnactionConfirmation",
        "coinSymbol"
})
@Setter
@Getter
public class BlockchainInvoice {

    @JsonProperty("amount")
    private String amount;
    @JsonProperty("invoiceId")
    private String invoiceId;
    @JsonProperty("trasnactionId")
    private String trasnactionId;
    @JsonProperty("requestStatus")
    private String requestStatus;
    @JsonProperty("address")
    private String address;
    @JsonProperty("expiredDateTime")
    private String expiredDateTime;
    @JsonProperty("depositeDateTime")
    private String depositeDateTime;
    @JsonProperty("createtedDateTiem")
    private String createtedDateTiem;
    @JsonProperty("trasnactionConfirmation")
    public Long trasnactionConfirmation;
    @JsonProperty("coinSymbol")
    private String coinSymbol;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String toString() {
        Gson json = new Gson();
        return json.toJson(this);
    }


}