package vip.mrtree.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeBasic {
    private String title;
    private String location;
    private String info;
    private String followInfo;
    private String totalPrice;
    private String unitPrice;
    private String houseType;
    private String area; // 面积
    private String furnish; // 装修情况
    private String floor; // 楼层
    private String age;
    private int focus;
    private String time; // 发布时间
    private List<String> tag = new ArrayList<>();
}
