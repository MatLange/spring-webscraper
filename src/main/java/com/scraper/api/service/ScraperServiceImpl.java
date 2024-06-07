package com.scraper.api.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.scraper.api.model.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScraperServiceImpl implements ScraperService {

    @Value("#{'${website.urls}'.split(',')}")
    List<String> urls;

    @Override
    public Set<ResponseDTO> getVehicleByModel(String vehicleModel) {

        Set<ResponseDTO> responseDTOS = new HashSet<>();

        for (String url: urls) {

            if (url.contains("computerfutures")) {
                extractDataFromCf(responseDTOS, url);
            }

        }

        return responseDTOS;
    }

    private void extractDataFromCf(Set<ResponseDTO> responseDTOS, String url) {
        try  (Playwright playwright = Playwright.create())  {
            final BrowserType chromium = playwright.chromium();
            final Browser browser = chromium.launch();
            BrowserContext context = browser.newContext();
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            context.setExtraHTTPHeaders(headers);
            final Page page = context.newPage();
            page.setExtraHTTPHeaders(headers);
            page.navigate(url);
/*             page.fill(searchbarCSSSelectorQuery,searchText);
            page.click(searchButtonSelectorQuery); */
            page.waitForTimeout(5000);
            Document document = Jsoup.parse(page.content());
            browser.close();            
/*             Document document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .get();
 */            
            Element element = document.getElementsByClass("job-search__list").first();
            if (element == null) {
                return;
            }
            Elements elements = element.getElementsByTag("a");

            for (Element ads: elements) {

                ResponseDTO responseDTO = new ResponseDTO();

                if (StringUtils.isNotEmpty(ads.attr("href"))) {
                    responseDTO.setTitle(ads.attr("aria-label"));
                    responseDTO.setUrl("https://www.computerfutures.com/"+ ads.attr("href"));
                }
                if (responseDTO.getUrl() != null) responseDTOS.add(responseDTO);

            }
        }  catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
