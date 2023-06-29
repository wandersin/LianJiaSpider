package vip.mrtree.bean;

import lombok.Data;

@Data
public class UrlCollection {
    private String search;
    private String page;
    private String detail;

    public UrlCollection(String search, String page, String detail) {
        this.search = search;
        this.page = page;
        this.detail = detail;
    }
}
