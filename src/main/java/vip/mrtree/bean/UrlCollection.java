package vip.mrtree.bean;

import lombok.Data;

@Data
public class UrlCollection {
    private String search;
    private String page;

    public UrlCollection(String search, String page) {
        this.search = search;
        this.page = page;
    }
}
