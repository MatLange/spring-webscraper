package com.scraper.api.model;

import lombok.Data;

@Data
public class WebsiteDTO {

    String title;
    String id;
    String baseUrl;
    String url;
    String itemTopElementName;
    String itemLinkElement;
    String itemTitlelementName;
}
