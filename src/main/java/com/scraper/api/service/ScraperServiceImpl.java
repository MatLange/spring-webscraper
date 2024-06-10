package com.scraper.api.service;

import com.scraper.api.model.ResponseDTO;
import com.scraper.api.model.WebsiteDTO;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

@Service
public class ScraperServiceImpl implements ScraperService {

    @Value("#{'${website.urls}'.split(',')}")
    List<String> urls;

    @Override
    public Set<ResponseDTO> getVehicleByModel(String vehicleModel) {

        Set<ResponseDTO> responseDTOS = new HashSet<>();
        JSONArray websites = readConfigJSON();
        Iterator website = websites.iterator();
        while (website.hasNext() && website.next() instanceof WebsiteDTO) {
            extractDataFromWebsite(responseDTOS, "", (WebsiteDTO) website.next());
        }   
        return responseDTOS;
    }

    public String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }    

    private JSONArray readConfigJSON() {
        JSONParser parser = new JSONParser();
        try {
            String jsonString = readFile("src/main/resources/config.json");
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            JSONArray websites = (JSONArray)jsonObject.get("websites");
            return websites;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null; // Add a return statement here
    }

    private void extractDataFromWebsite(Set<ResponseDTO> responseDTOS, String url, WebsiteDTO websiteDTO) {
        try  (Playwright playwright = Playwright.create())  {
            final BrowserType chromium = playwright.chromium();
            final Browser browser = chromium.launch();
            BrowserContext context = browser.newContext();
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            context.setExtraHTTPHeaders(headers);
            final Page page = context.newPage();
            page.setExtraHTTPHeaders(headers);
            page.navigate(websiteDTO.getBaseUrl());
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
            String elementClassName = websiteDTO.getItemTopElementName();
            Element element = document.getElementsByClass(elementClassName).first();
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
