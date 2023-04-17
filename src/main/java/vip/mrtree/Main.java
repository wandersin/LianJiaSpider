package vip.mrtree;

import com.alibaba.fastjson.JSON;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vip.mrtree.bean.HomeBasic;
import vip.mrtree.utils.CollectionUtils;
import vip.mrtree.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private final static String SEARCH_URL = "https://bj.lianjia.com/ershoufang/rs{}/";

    private final static String PAGE_URL = "https://bj.lianjia.com/ershoufang/pg{}rs{}/";

    private final static String DIR_PATH = "D:\\MySynologyDrive\\Develop\\测试环境\\tmp";

    private final static String FILE_NAME = "链家.html";

    private final static String KEY_WORD = "沙河";

    public static void main(String[] args) throws Exception {
        start(KEY_WORD);
    }

    private static void start(String key) throws Exception {
        // step.1 查询总页数
        String url = StringUtils.strFormat(SEARCH_URL, key);
        System.out.println(StringUtils.strFormat("开始查询 {}", url));
        String pageData = Jsoup.connect(url)
            .get()
            .getElementsByClass("house-lst-page-box")
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("获取页码错误"))
            .attr("page-data");
        Integer totalPage = JSON.parseObject(pageData).getInteger("totalPage");
        System.out.println(StringUtils.strFormat("查询到总页数: {}", totalPage));
        List<HomeBasic> itemList = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            System.out.println(StringUtils.strFormat("开始查询第 {} 页数据", i));
            String pageUrl = StringUtils.strFormat(PAGE_URL, i, key);
            itemList.addAll(page2Item(pageUrl));
            Thread.sleep(5000);
        }
        String fileName = StringUtils.strFormat("链家二手房 - {}.xlsx", key);
        saveItem(itemList, StringUtils.join(DIR_PATH, File.separator, fileName));
    }

    private static void savePage() throws Exception {
        Connection connect = Jsoup.connect(StringUtils.strFormat(SEARCH_URL, "良乡"));
        Document document = connect.get();
        String file = StringUtils.join(DIR_PATH, File.separator, FILE_NAME);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(document.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    private static Document readPage(String filePath) throws Exception {
        return Jsoup.parse(new File(filePath));
    }

    private static void saveItem(List<HomeBasic> list, String filePath) throws Exception {
        System.out.println(StringUtils.strFormat("保存 {} 条记录至 {}", list.size(), filePath));
        // excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("基本信息");
        // 表头
        XSSFRow row = sheet.createRow(0);
        XSSFCell idCell = row.createCell(0);
        idCell.setCellValue("id");
        XSSFCell locationCell = row.createCell(2);
        locationCell.setCellValue("位置");
        XSSFCell infoCell = row.createCell(3);
        infoCell.setCellValue("基本信息");
        XSSFCell totalPriceCell = row.createCell(4);
        totalPriceCell.setCellValue("总价");
        XSSFCell unitPriceCell = row.createCell(5);
        unitPriceCell.setCellValue("单价");
        XSSFCell followCell = row.createCell(6);
        followCell.setCellValue("关注");
        XSSFCell tagCell = row.createCell(7);
        tagCell.setCellValue("标签");
        XSSFCell urlCell = row.createCell(8);
        urlCell.setCellValue("详情");
        int rowNum = 1;
        for (HomeBasic item : list) {
            XSSFRow itemRow = sheet.createRow(rowNum);
            XSSFCell id = itemRow.createCell(0);
            id.setCellValue(item.getId());
            XSSFCell title = itemRow.createCell(1);
            title.setCellValue(item.getTitle());
            XSSFCell location = itemRow.createCell(2);
            location.setCellValue(item.getLocation());
            XSSFCell info = itemRow.createCell(3);
            info.setCellValue(item.getInfo());
            XSSFCell totalPrice = itemRow.createCell(4);
            totalPrice.setCellValue(item.getTotalPrice());
            XSSFCell unitPrice = itemRow.createCell(5);
            unitPrice.setCellValue(item.getUnitPrice());
            XSSFCell follow = itemRow.createCell(6);
            follow.setCellValue(item.getFollowInfo());
            XSSFCell tag = itemRow.createCell(7);
            tag.setCellValue(CollectionUtils.join(item.getTag(), ", "));
            XSSFCell detail = itemRow.createCell(8);
            detail.setCellValue(item.getDetailUrl());
            rowNum++;
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("保存成功");
    }

    private static List<HomeBasic> page2Item(String url) throws Exception {
        return toItem(Jsoup.connect(url).get());
    }

    private static List<HomeBasic> toItem(Document document) throws Exception {
        Elements parent = document.getElementsByClass("sellListContent");
        Element ul = parent.stream().findFirst().orElseThrow(() -> new RuntimeException("未找到详情"));
        List<Element> list = ul.getElementsByTag("li")
            .stream()
            .map(li -> li.getElementsByClass("info").stream().findAny().orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return list.stream().map(li -> {
            HomeBasic basic = new HomeBasic();
            Element a = li.getElementsByTag("a").get(0);
            String detailUrl = a.attr("href");
            Matcher matcher = Pattern.compile("^https://bj\\.lianjia\\.com/ershoufang/(.+)\\.html$").matcher(detailUrl);
            if (matcher.find()) {
                basic.setId(matcher.group(1));
            }
            basic.setTitle(a.text());
            basic.setDetailUrl(detailUrl);
            List<String> locationStr = li.getElementsByClass("positionInfo").stream().map(Element::text).collect(Collectors.toList());
            basic.setLocation(CollectionUtils.join(locationStr, "-"));
            basic.setInfo(li.getElementsByClass("houseInfo").get(0).text());
            basic.setFollowInfo(li.getElementsByClass("followInfo").get(0).text());
            List<String> tagList = li.getElementsByClass("tag").get(0).getElementsByTag("span").stream().map(Element::text).collect(Collectors.toList());
            basic.getTag().addAll(tagList);
            basic.setTotalPrice(li.getElementsByClass("totalPrice").text());
            basic.setUnitPrice(li.getElementsByClass("unitPrice").text());
            return basic;
        }).collect(Collectors.toList());
    }
}