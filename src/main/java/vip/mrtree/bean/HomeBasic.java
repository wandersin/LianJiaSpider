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
    private String toward; // 朝向
    private String info;
    private String houseType; // 户型
    private String floor; // 楼层
    private List<String> tag = new ArrayList<>();
    private String detailUrl;
}
