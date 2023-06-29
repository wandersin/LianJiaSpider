package vip.mrtree.service;

import org.jsoup.nodes.Element;
import vip.mrtree.bean.CityEnum;
import vip.mrtree.bean.UrlCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface Spider {
    UrlCollection getUrlCollection(CityEnum city);

    default List<Object> search(CityEnum city, String key) throws IOException, InterruptedException {
        int num = getPageNum(city, key);
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            list.addAll(page2item(city, key, i + 1));
            Thread.sleep(5000);
        }
        return list;
    }

    int getPageNum(CityEnum city, String key) throws IOException;

    List<Object> page2item(CityEnum city, String key, int num) throws IOException;

    Object element2item(CityEnum city, Element element);

    void save(String filePath, List<Object> list) throws IOException;
}
