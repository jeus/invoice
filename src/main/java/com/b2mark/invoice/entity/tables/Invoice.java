/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice {

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty( readOnly = true)
    private long id;


    @NotNull
    @ReadOnlyProperty
    @ApiModelProperty(readOnly = true)
    @Column(name = "regdatetime", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "MM-dd hh:mm" , timezone="UTC")
    private Date regdatetime;

    @NotNull
    private String callback;//callback url

    @NotNull
    private long amount;

    @NotNull
    private Date userdatetime;

    private String description;
    @NotNull
    private long orderid;

    @ManyToOne
    @JoinColumn(name = "merchant", referencedColumnName= "id")
    @ApiModelProperty(readOnly = true,hidden = true)
    private Merchant merchant;


    @NotNull
    private String currency;

    @NotNull
    private String status;

    @NotNull
    private String qr;


}