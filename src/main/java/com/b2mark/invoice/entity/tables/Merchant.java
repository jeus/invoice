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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


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
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(readOnly = true,hidden = true)
    private Date datetime;
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(hidden = true)
    private Date lastSendToken;
//    @OneToMany(mappedBy = "merchant")
//    private List<Invoice> invoices;

}
