package vip.mrtree.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellHomeBasic extends HomeBasic {
    private int followNum;
    private String release; // 发布日期
    private String totalPrice;
    private String unitPrice;
    private String furnish; // 装修情况
    private String age;
}
