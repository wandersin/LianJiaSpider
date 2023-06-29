package vip.mrtree.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import vip.mrtree.bean.CityEnum;
import vip.mrtree.bean.UrlCollection;
import vip.mrtree.utils.StringUtils;

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

    default int getPageNum(CityEnum city, String key) throws IOException {
        String url = StringUtils.strFormat(getUrlCollection(city).getSearch(), key);
        System.out.println(StringUtils.strFormat("开始查询 {}", url));
        String pageData = Jsoup.connect(url)
            .get()
            .getElementsByClass("content__pg")
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("获取页码错误"))
            .attr("data-totalpage");
        int totalPage = Integer.parseInt(pageData);
        System.out.println(StringUtils.strFormat("查询到总页数: {}", totalPage));
        return totalPage;
    }


    default List<Object> page2item(CityEnum city, String key, int num) throws IOException {
        String url = StringUtils.strFormat(getUrlCollection(city).getPage(), num, key);
        System.out.println(StringUtils.strFormat("开始查询第 {} 页数据: {}", num, url));
        List<Object> list = new ArrayList<>();
        for (Element element : Jsoup.connect(url).get().getElementsByClass("content__list--item")) {
            list.add(element2item(city, element));
        }
        return list;
    }

    Object element2item(CityEnum city, Element element);

    void save(String filePath, List<Object> item) throws IOException;
}
