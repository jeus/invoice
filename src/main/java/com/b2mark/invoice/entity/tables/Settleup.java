package com.b2mark.invoice.entity.tables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1>Settleup Table in database.</h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@Entity
@Table(name = "settleup")
@Getter
@Setter
@NoArgsConstructor
public class Settleup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true,hidden = true)
    private long id;
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(readOnly = true,hidden = true)
    private Date datetime;
    @NotNull
    private long amount;
    @NotNull
    private String originCard;
    @NotNull
    private String destCard;
    @NotNull
    private String txid;
    @ManyToOne
    @JoinColumn(name = "merchant", referencedColumnName = "id")
    @ApiModelProperty(readOnly = true, hidden = true)
    private Merchant merchant;


    @OneToMany(cascade = CascadeType.DETACH,fetch = FetchType.LAZY)
    @JoinTable(name = "invoicesettle", joinColumns = @JoinColumn(name = "settleup"),
            inverseJoinColumns = @JoinColumn(name = "invoice_id")
    )
    @JsonIgnoreProperties("merchant")
    @JsonManagedReference
    private Set<Invoice> invoices = new HashSet<>();
}





