package vip.mrtree.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentHomeBasic extends HomeBasic {
    private boolean featured; // 精选
    private String price; // 租金
    private String brand; // 优选
}
