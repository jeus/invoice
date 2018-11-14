package com.b2mark.invoice.common.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@JsonPropertyOrder({
        "name",
        "status",
        "size",
        "page",
        "count",
        "next",
        "previous",
        "content"
})
@Setter
@Getter
public class Pagination<T> {


    private List<T> content = new ArrayList<>();
    private String name;
    private long count;
    private int status;
    private int page;
    private int size;
    @JsonIgnore
    private String apiAddress;


    public void add(T t) {
        content.add(t);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNext() {
        if (((page + 1) * size) < count)
            return toFormatPattern(page + 1);
        else
            return null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPrevious() {
        if (page > 0) {
            return toFormatPattern(page - 1);
        } else
            return null;

    }


    private String toFormatPattern(int page) {
        if (apiAddress.toLowerCase().contains("page=")) {
           return  apiAddress.replaceAll("page=\\d*", "page=" + page);
        }else
            return apiAddress + "&page=" + page;
    }


}
