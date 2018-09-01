package com.b2mark.invoice.entity.jwt;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"jti",
"exp",
"nbf",
"iat",
"iss",
"aud",
"sub",
"typ",
"azp",
"nonce",
"auth_time",
"session_state",
"acr",
"allowed-origins",
"realm_access",
"resource_access",
"name",
"preferred_username",
"given_name",
"family_name",
"email"
})
public class JwtUser {

@JsonProperty("jti")
private String jti;
@JsonProperty("exp")
private Integer exp;
@JsonProperty("nbf")
private Integer nbf;
@JsonProperty("iat")
private Integer iat;
@JsonProperty("iss")
private String iss;
@JsonProperty("aud")
private String aud;
@JsonProperty("sub")
private String sub;
@JsonProperty("typ")
private String typ;
@JsonProperty("azp")
private String azp;
@JsonProperty("nonce")
private String nonce;
@JsonProperty("auth_time")
private Integer authTime;
@JsonProperty("session_state")
private String sessionState;
@JsonProperty("acr")
private String acr;
@JsonProperty("allowed-origins")
private List<String> allowedOrigins = null;
@JsonProperty("realm_access")
private RealmAccess realmAccess;
@JsonProperty("resource_access")
private ResourceAccess resourceAccess;
@JsonProperty("name")
private String name;
@JsonProperty("preferred_username")
private String preferredUsername;
@JsonProperty("given_name")
private String givenName;
@JsonProperty("family_name")
private String familyName;
@JsonProperty("email")
private String email;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("jti")
public String getJti() {
return jti;
}

@JsonProperty("jti")
public void setJti(String jti) {
this.jti = jti;
}

@JsonProperty("exp")
public Integer getExp() {
return exp;
}

@JsonProperty("exp")
public void setExp(Integer exp) {
this.exp = exp;
}

@JsonProperty("nbf")
public Integer getNbf() {
return nbf;
}

@JsonProperty("nbf")
public void setNbf(Integer nbf) {
this.nbf = nbf;
}

@JsonProperty("iat")
public Integer getIat() {
return iat;
}

@JsonProperty("iat")
public void setIat(Integer iat) {
this.iat = iat;
}

@JsonProperty("iss")
public String getIss() {
return iss;
}

@JsonProperty("iss")
public void setIss(String iss) {
this.iss = iss;
}

@JsonProperty("aud")
public String getAud() {
return aud;
}

@JsonProperty("aud")
public void setAud(String aud) {
this.aud = aud;
}

@JsonProperty("sub")
public String getSub() {
return sub;
}

@JsonProperty("sub")
public void setSub(String sub) {
this.sub = sub;
}

@JsonProperty("typ")
public String getTyp() {
return typ;
}

@JsonProperty("typ")
public void setTyp(String typ) {
this.typ = typ;
}

@JsonProperty("azp")
public String getAzp() {
return azp;
}

@JsonProperty("azp")
public void setAzp(String azp) {
this.azp = azp;
}

@JsonProperty("nonce")
public String getNonce() {
return nonce;
}

@JsonProperty("nonce")
public void setNonce(String nonce) {
this.nonce = nonce;
}

@JsonProperty("auth_time")
public Integer getAuthTime() {
return authTime;
}

@JsonProperty("auth_time")
public void setAuthTime(Integer authTime) {
this.authTime = authTime;
}

@JsonProperty("session_state")
public String getSessionState() {
return sessionState;
}

@JsonProperty("session_state")
public void setSessionState(String sessionState) {
this.sessionState = sessionState;
}

@JsonProperty("acr")
public String getAcr() {
return acr;
}

@JsonProperty("acr")
public void setAcr(String acr) {
this.acr = acr;
}

@JsonProperty("allowed-origins")
public List<String> getAllowedOrigins() {
return allowedOrigins;
}

@JsonProperty("allowed-origins")
public void setAllowedOrigins(List<String> allowedOrigins) {
this.allowedOrigins = allowedOrigins;
}

@JsonProperty("realm_access")
public RealmAccess getRealmAccess() {
return realmAccess;
}

@JsonProperty("realm_access")
public void setRealmAccess(RealmAccess realmAccess) {
this.realmAccess = realmAccess;
}

@JsonProperty("resource_access")
public ResourceAccess getResourceAccess() {
return resourceAccess;
}

@JsonProperty("resource_access")
public void setResourceAccess(ResourceAccess resourceAccess) {
this.resourceAccess = resourceAccess;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("preferred_username")
public String getPreferredUsername() {
return preferredUsername;
}

@JsonProperty("preferred_username")
public void setPreferredUsername(String preferredUsername) {
this.preferredUsername = preferredUsername;
}

@JsonProperty("given_name")
public String getGivenName() {
return givenName;
}

@JsonProperty("given_name")
public void setGivenName(String givenName) {
this.givenName = givenName;
}

@JsonProperty("family_name")
public String getFamilyName() {
return familyName;
}

@JsonProperty("family_name")
public void setFamilyName(String familyName) {
this.familyName = familyName;
}

@JsonProperty("email")
public String getEmail() {
return email;
}

@JsonProperty("email")
public void setEmail(String email) {
this.email = email;
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