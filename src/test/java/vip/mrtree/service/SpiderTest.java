package vip.mrtree.service;

import vip.mrtree.bean.CityEnum;
import vip.mrtree.model.Rent;
import vip.mrtree.utils.StringUtils;

import java.io.IOException;
import java.util.List;

public class SpiderTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String key = "麓山上院";
        rentTest(key);
    }

    public static void rentTest(String key) throws IOException, InterruptedException {
        Spider spider = new Rent();
        List<Object> list = spider.search(CityEnum.CHENGDU, key);
        System.out.println(list.size());
        String filePath = StringUtils.strFormat("D:\\MySynologyDrive\\Develop\\测试环境\\Spider\\链家\\成都\\租房\\{}.xlsx", key);
        spider.save(filePath, list);
    }
}
