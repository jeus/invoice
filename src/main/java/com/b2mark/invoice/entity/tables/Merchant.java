package com.b2mark.invoice.entity.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@Entity
@Table(name = "merchant")
@Getter
@Setter
@NoArgsConstructor
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true,hidden = true)
    private long id;
    @NotNull
    private String mobile;
    @NotNull
    @ApiModelProperty(readOnly = true)
    private String token;
    @NotNull
    private String pushToken;
    @NotNull
    private String shopName;
    @NotNull
    private String callback;
    @NotNull
    private String cardNumber;
    @NotNull
    @JsonIgnore
    private String apiKey;
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(readOnly = true,hidden = true)
    private Date datetime;
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(hidden = true)
    private Date lastSendToken;

    public String toString(){
        Gson json = new Gson();
        return json.toJson(this);
    }
}
