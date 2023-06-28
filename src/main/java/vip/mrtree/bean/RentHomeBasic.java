package vip.mrtree.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentHomeBasic extends HomeBasic {
    private String price; // 租金
    private String toward; // 朝向
    private String brand; // 优选
}
