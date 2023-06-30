package vip.mrtree.model;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vip.mrtree.bean.CityEnum;
import vip.mrtree.bean.SellHomeBasic;
import vip.mrtree.bean.UrlCollection;
import vip.mrtree.service.Spider;
import vip.mrtree.utils.CollectionUtils;
import vip.mrtree.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sell implements Spider {
    private static final Map<CityEnum, UrlCollection> BASE_URL_MAP = new HashMap<>();

    static {
        BASE_URL_MAP.put(CityEnum.CHENGDU, new UrlCollection(
            "https://cd.lianjia.com/ershoufang/rs{}/",
            "https://cd.lianjia.com/ershoufang/pg{}rs{}/",
            "https://cd.lianjia.com/ershoufang/{}.html"
        ));
        BASE_URL_MAP.put(CityEnum.BEIJING, new UrlCollection(
            "https://bj.lianjia.com/ershoufang/rs{}/",
            "https://bj.lianjia.com/ershoufang/pg{}rs{}/",
            "https://bj.lianjia.com/ershoufang/{}.html"
        ));
    }

    @Override
    public UrlCollection getUrlCollection(CityEnum city) {
        return BASE_URL_MAP.get(city);
    }

    @Override
    public int getPageNum(CityEnum city, String key) throws IOException {
        String url = StringUtils.strFormat(getUrlCollection(city).getSearch(), key);
        System.out.println(StringUtils.strFormat("开始查询 {}", url));
        String pageData = Jsoup.connect(url)
                .get()
                .getElementsByClass("house-lst-page-box")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("获取页码错误"))
                .attr("page-data");
        int totalPage = JSONObject.parseObject(pageData).getInteger("totalPage");
        System.out.println(StringUtils.strFormat("查询到总页数: {}", totalPage));
        return totalPage;
    }

    @Override
    public List<Object> page2item(CityEnum city, String key, int num) throws IOException {
        String url = StringUtils.strFormat(getUrlCollection(city).getPage(), num, key);
        System.out.println(StringUtils.strFormat("开始查询第 {} 页数据: {}", num, url));
        List<Object> list = new ArrayList<>();
        Elements elements = Jsoup.connect(url).get().select("ul.sellListContent").select("li");
        for (Element element : elements) {
            list.add(element2item(city, element));
        }
        return list;
    }

    @Override
    public Object element2item(CityEnum city, Element element) {
        SellHomeBasic sellHomeBasic = new SellHomeBasic();
        String id = element.attr("data-lj_action_housedel_id");
        sellHomeBasic.setId(id);
        sellHomeBasic.setDetailUrl(StringUtils.strFormat(getUrlCollection(city).getDetail(), id));
        sellHomeBasic.setTitle(element.select("div.title a").text());
        sellHomeBasic.setLocation(element.select("div.flood").text());
        String info = element.select("div.houseInfo").text();
        try {
            List<String> list = new ArrayList<>(Arrays.stream(info.split("\\|")).map(String::trim).toList());
            sellHomeBasic.setHouseType(list.remove(0));
            sellHomeBasic.setArea(list.remove(0));
            Matcher areaMatcher = Pattern.compile("(\\d+\\.*\\d*)").matcher(sellHomeBasic.getArea());
            if (areaMatcher.find()) {
                sellHomeBasic.setAreaNum(Float.parseFloat(areaMatcher.group(1)));
            }
            sellHomeBasic.setToward(list.remove(0));
            sellHomeBasic.setInfo(CollectionUtils.join(list, ", "));
        } catch (Exception ignore) {}
        String followInfo = element.select("div.followInfo").text();
        Matcher followMatcher = Pattern.compile("(\\d+)人关注[/\\s]{0,3}(.+)发布").matcher(followInfo);
        if (followMatcher.find()) {
            sellHomeBasic.setFollowNum(Integer.parseInt(followMatcher.group(1)));
            sellHomeBasic.setRelease(followMatcher.group(2));
        }
        sellHomeBasic.getTag().addAll(element.select("div.tag span").stream().map(Element::text).toList());
        sellHomeBasic.setTotalPrice(element.select("div.totalPrice").text());
        sellHomeBasic.setUnitPrice(element.select("div.unitPrice").text());
        return sellHomeBasic;
    }

    @Override
    public void save(String filePath, List<Object> list) throws IOException {
        System.out.println(StringUtils.strFormat("保存查询结果至: {}", filePath));
        // excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("基本信息");
        // 表头
        XSSFRow row = sheet.createRow(0);
        // title, 位置, 面积, 户型, 朝向, 装修情况, 楼层, 房龄, 总价, 单价, 标签, 其他
        XSSFCell locationCell = row.createCell(1);
        locationCell.setCellValue("位置");
        XSSFCell areaCell = row.createCell(2);
        areaCell.setCellValue("面积 (平米)");
        XSSFCell typeCell = row.createCell(3);
        typeCell.setCellValue("户型");
        XSSFCell towardCell = row.createCell(4);
        towardCell.setCellValue("朝向");
        XSSFCell infoCell = row.createCell(5);
        infoCell.setCellValue("附加信息");
        XSSFCell totalPriceCell = row.createCell(6);
        totalPriceCell.setCellValue("总价");
        XSSFCell unitPriceCell = row.createCell(7);
        unitPriceCell.setCellValue("单价");
        XSSFCell tagCell = row.createCell(8);
        tagCell.setCellValue("标签");
        XSSFCell followCell = row.createCell(9);
        followCell.setCellValue("关注人数");
        XSSFCell releaseCell = row.createCell(10);
        releaseCell.setCellValue("发布日期");
        XSSFCell detailUrlCell = row.createCell(11);
        detailUrlCell.setCellValue("详情链接");
        int rowNum = 1;
        for (Object tmp : list) {
            SellHomeBasic item = (SellHomeBasic) tmp;
            XSSFRow itemRow = sheet.createRow(rowNum);
            XSSFCell title = itemRow.createCell(0);
            title.setCellValue(item.getTitle());
            XSSFCell location = itemRow.createCell(1);
            location.setCellValue(item.getLocation());
            XSSFCell area = itemRow.createCell(2);
            area.setCellValue(item.getAreaNum());
            area.setCellType(CellType.NUMERIC);
            XSSFCell type = itemRow.createCell(3);
            type.setCellValue(item.getHouseType());
            XSSFCell toward = itemRow.createCell(4);
            toward.setCellValue(item.getToward());
            XSSFCell info = itemRow.createCell(5);
            info.setCellValue(item.getInfo());
            XSSFCell totalPrice = itemRow.createCell(6);
            totalPrice.setCellValue(item.getTotalPrice());
            XSSFCell unitPrice = itemRow.createCell(7);
            unitPrice.setCellValue(item.getUnitPrice());
            XSSFCell tag = itemRow.createCell(8);
            tag.setCellValue(CollectionUtils.join(item.getTag(), ", "));
            XSSFCell follow = itemRow.createCell(9);
            follow.setCellValue(item.getFollowNum());
            follow.setCellType(CellType.NUMERIC);
            XSSFCell release = itemRow.createCell(10);
            release.setCellValue(item.getRelease());
            XSSFCell detailUrl = itemRow.createCell(11);
            detailUrl.setCellValue(item.getDetailUrl());
            XSSFHyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            link.setAddress(item.getDetailUrl());
            XSSFCellStyle linkStyle = workbook.createCellStyle();
            XSSFFont cellFont = workbook.createFont();
            cellFont.setUnderline((byte) 1);
            byte[] color = {0, (byte) 176, (byte) 240};
            cellFont.setColor(new XSSFColor(color));
            linkStyle.setFont(cellFont);
            detailUrl.setCellStyle(linkStyle);
            detailUrl.setHyperlink(link);
            rowNum++;
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("保存成功");
    }
}
