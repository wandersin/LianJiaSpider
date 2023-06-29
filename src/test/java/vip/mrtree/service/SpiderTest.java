package vip.mrtree.service;

import vip.mrtree.bean.CityEnum;
import vip.mrtree.model.Rent;
import vip.mrtree.utils.DateUtils;
import vip.mrtree.utils.StringUtils;

import java.io.IOException;
import java.util.List;

public class SpiderTest {
    private final static String ROOT_PATH = "D:\\MySynologyDrive\\Develop\\测试环境\\Spider\\链家";

    public static void main(String[] args) throws IOException, InterruptedException {
        String key = "交子大道";
        rentTest(CityEnum.CHENGDU, key);
    }

    public static void rentTest(CityEnum city, String key) throws IOException, InterruptedException {
        Spider spider = new Rent();
        List<Object> list = spider.search(city, key);
        String filePath = StringUtils.strFormat("{}\\{}-{}.xlsx", ROOT_PATH, DateUtils.getDateStr("yyyyMMdd"), key);
        spider.save(filePath, list);
    }
}
