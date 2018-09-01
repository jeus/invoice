/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

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
    private Date regdatetime;

    @NotNull
    private String callback;//callback url

    @NotNull
    private long amount;

    @NotNull
    private Date userDateTime;

    private String description;
    @NotNull
    private long order;

    @ManyToOne
    @JoinColumn(name = "merchant", referencedColumnName= "id")
    private Merchant merchant;



}
