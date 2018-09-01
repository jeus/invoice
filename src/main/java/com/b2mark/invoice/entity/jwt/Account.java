package com.b2mark.invoice.entity.jwt;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"roles"
})
public class Account {

@JsonProperty("roles")
private List<String> roles = null;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("roles")
public List<String> getRoles() {
return roles;
}

@JsonProperty("roles")
public void setRoles(List<String> roles) {
this.roles = roles;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}