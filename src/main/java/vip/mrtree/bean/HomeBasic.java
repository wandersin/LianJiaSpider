package vip.mrtree.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HomeBasic {
    private String id;
    private String title;
    private String location;
    private String area; // 面积
    private String info;
    private List<String> tag = new ArrayList<>();
    private String detailUrl;
}
