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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rent implements Spider {
    private static final Map<CityEnum, UrlCollection> BASE_URL_MAP = new HashMap<>();

    static {
        BASE_URL_MAP.put(CityEnum.CHENGDU, new UrlCollection(
            "https://cd.lianjia.com/zufang/rs{}/",
            "https://cd.lianjia.com/zufang/pg{}rs{}/"
        ));
        BASE_URL_MAP.put(CityEnum.BEIJING, new UrlCollection(
            "https://bj.lianjia.com/zufang/rs{}/",
            "https://bj.lianjia.com/zufang/pg{}rs{}/"
        ));
    }

    @Override
    public UrlCollection getUrlCollection(CityEnum city) {
        return BASE_URL_MAP.get(city);
    }

    @Override
    public Object element2item(Element element) {
        RentHomeBasic rentHomeBasic = new RentHomeBasic();
        rentHomeBasic.setTitle(element.getElementsByClass("content__list--item--title").text());
        rentHomeBasic.setInfo(element.getElementsByClass("content__list--item--des").text());
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
        XSSFCell infoCell = row.createCell(1);
        infoCell.setCellValue("基本信息");
        XSSFCell priceCell = row.createCell(2);
        priceCell.setCellValue("价格");
        XSSFCell tagCell = row.createCell(3);
        tagCell.setCellValue("标签");
        XSSFCell brandCell = row.createCell(4);
        brandCell.setCellValue("品牌优选");
        int rowNum = 1;
        for (Object tmp : list) {
            RentHomeBasic item = (RentHomeBasic) tmp;
            XSSFRow itemRow = sheet.createRow(rowNum);
            XSSFCell title = itemRow.createCell(0);
            title.setCellValue(item.getTitle());
            XSSFCell info = itemRow.createCell(1);
            info.setCellValue(item.getInfo());
            XSSFCell price = itemRow.createCell(2);
            price.setCellValue(item.getPrice());
            XSSFCell tag = itemRow.createCell(3);
            tag.setCellValue(CollectionUtils.join(item.getTag(), ", "));
            XSSFCell brand = itemRow.createCell(4);
            brand.setCellValue(item.getBrand());
            rowNum++;
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("保存成功");
    }
}
