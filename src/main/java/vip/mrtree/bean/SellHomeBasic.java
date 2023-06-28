package vip.mrtree.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellHomeBasic extends HomeBasic {
    private String followInfo;
    private String totalPrice;
    private String unitPrice;
    private String houseType;
    private String furnish; // 装修情况
    private String floor; // 楼层
    private String age;
    private int focus;
    private String time; // 发布时间
}
