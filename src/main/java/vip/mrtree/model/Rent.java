package vip.mrtree.model;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.nodes.Element;
import vip.mrtree.bean.CityEnum;
import vip.mrtree.bean.RentHomeBasic;
import vip.mrtree.bean.UrlCollection;
import vip.mrtree.service.Spider;
import vip.mrtree.utils.CollectionUtils;
import vip.mrtree.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rent implements Spider {
    private static final Map<CityEnum, UrlCollection> BASE_URL_MAP = new HashMap<>();

    static {
        BASE_URL_MAP.put(CityEnum.CHENGDU, new UrlCollection(
            "https://cd.lianjia.com/zufang/rs{}/",
            "https://cd.lianjia.com/zufang/pg{}rs{}/",
            "https://cd.lianjia.com/zufang/{}"
        ));
        BASE_URL_MAP.put(CityEnum.BEIJING, new UrlCollection(
            "https://bj.lianjia.com/zufang/rs{}/",
            "https://bj.lianjia.com/zufang/pg{}rs{}/",
            "https://bj.lianjia.com/zufang/{}"
        ));
    }

    @Override
    public UrlCollection getUrlCollection(CityEnum city) {
        return BASE_URL_MAP.get(city);
    }

    @Override
    public Object element2item(CityEnum city, Element element) {
        RentHomeBasic rentHomeBasic = new RentHomeBasic();
        String id = element.attr("data-house_code");
        rentHomeBasic.setId(id);
        rentHomeBasic.setDetailUrl(StringUtils.strFormat(getUrlCollection(city).getDetail(), id));
        rentHomeBasic.setTitle(element.getElementsByClass("content__list--item--title").text());
        String info = element.getElementsByClass("content__list--item--des").text();
        rentHomeBasic.setInfo(info);
        try {
            List<String> list = Arrays.stream(info.split("/")).map(String::trim).toList();
            int offset = 0;
            if (list.get(0).contains("精选")) {
                rentHomeBasic.setFeatured(Boolean.TRUE);
                offset++;
            }
            rentHomeBasic.setLocation(list.get(offset));
            rentHomeBasic.setArea(list.get(1 + offset));
            rentHomeBasic.setToward(list.get(2 + offset));
            rentHomeBasic.setHouseType(list.get(3 + offset));
            rentHomeBasic.setFloor(list.get(4 + offset));
        } catch (Exception ignore) {}
        rentHomeBasic.getTag().addAll(element.select("i[class*=content__item__tag]").stream().map(Element::text).toList());
        rentHomeBasic.setPrice(element.getElementsByClass("content__list--item-price").text());
        rentHomeBasic.setBrand(element.getElementsByClass("brand").text());
        return rentHomeBasic;
    }

    @Override
    public void save(String filePath, List<Object> list) throws IOException {
        System.out.println(StringUtils.strFormat("保存查询结果至: {}", filePath));
        // excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("基本信息");
        // 表头
        XSSFRow row = sheet.createRow(0);
        XSSFCell locationCell = row.createCell(2);
        locationCell.setCellValue("位置");
        XSSFCell areaCell = row.createCell(3);
        areaCell.setCellValue("面积");
        XSSFCell typeCell = row.createCell(4);
        typeCell.setCellValue("户型");
        XSSFCell priceCell = row.createCell(5);
        priceCell.setCellValue("价格");
        XSSFCell towardCell = row.createCell(6);
        towardCell.setCellValue("朝向");
        XSSFCell floorCell = row.createCell(7);
        floorCell.setCellValue("楼层");
        XSSFCell tagCell = row.createCell(8);
        tagCell.setCellValue("标签");
        XSSFCell brandCell = row.createCell(9);
        brandCell.setCellValue("品牌优选");
        XSSFCell detailUrlCell = row.createCell(10);
        detailUrlCell.setCellValue("详情链接");
        int rowNum = 1;
        for (Object tmp : list) {
            RentHomeBasic item = (RentHomeBasic) tmp;
            XSSFRow itemRow = sheet.createRow(rowNum);
            XSSFCell featured = itemRow.createCell(0);
            if (item.isFeatured()) {
                featured.setCellValue("精选");
            }
            XSSFCell title = itemRow.createCell(1);
            title.setCellValue(item.getTitle());
            XSSFCell location = itemRow.createCell(2);
            location.setCellValue(item.getLocation());
            XSSFCell area = itemRow.createCell(3);
            area.setCellValue(item.getArea());
            XSSFCell type = itemRow.createCell(4);
            type.setCellValue(item.getHouseType());
            XSSFCell price = itemRow.createCell(5);
            price.setCellValue(item.getPrice());
            XSSFCell toward = itemRow.createCell(6);
            toward.setCellValue(item.getToward());
            XSSFCell floor = itemRow.createCell(7);
            floor.setCellValue(item.getFloor());
            XSSFCell tag = itemRow.createCell(8);
            tag.setCellValue(CollectionUtils.join(item.getTag(), ", "));
            XSSFCell brand = itemRow.createCell(9);
            brand.setCellValue(item.getBrand());
            XSSFCell detailUrl = itemRow.createCell(10);
            detailUrl.setCellValue(item.getDetailUrl());
            rowNum++;
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("保存成功");
    }
}
