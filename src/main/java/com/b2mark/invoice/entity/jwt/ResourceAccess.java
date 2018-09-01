package com.b2mark.invoice.entity.jwt;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"test-realm",
"master-realm",
"account"
})
public class ResourceAccess {

@JsonProperty("test-realm")
private TestRealm testRealm;
@JsonProperty("master-realm")
private MasterRealm masterRealm;
@JsonProperty("account")
private Account account;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("test-realm")
public TestRealm getTestRealm() {
return testRealm;
}

@JsonProperty("test-realm")
public void setTestRealm(TestRealm testRealm) {
this.testRealm = testRealm;
}

@JsonProperty("master-realm")
public MasterRealm getMasterRealm() {
return masterRealm;
}

@JsonProperty("master-realm")
public void setMasterRealm(MasterRealm masterRealm) {
this.masterRealm = masterRealm;
}

@JsonProperty("account")
public Account getAccount() {
return account;
}

@JsonProperty("account")
public void setAccount(Account account) {
this.account = account;
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