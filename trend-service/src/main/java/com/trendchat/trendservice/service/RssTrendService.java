package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.NewsItem;
import com.trendchat.trendservice.dto.TrendItem;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

@Slf4j
@Service
public class RssTrendService {

    public Map<String, TrendItem> fetchTrends() {
        String rssUrl = "https://trends.google.com/trending/rss?geo=KR";
        Map<String, TrendItem> trendMap = new LinkedHashMap<>();

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            parser.parse(new URL(rssUrl).openStream(), new DefaultHandler() {
                final StringBuilder buffer = new StringBuilder();

                String title = null;
                String approxTraffic = null;
                List<NewsItem> newsItems = new ArrayList<>();

                String newsTitle = null;
                String newsUrl = null;
                String newsImage = null;
                String newsSource = null;

                boolean inItem = false;
                boolean inNewsItem = false;

                @Override
                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) {
                    buffer.setLength(0);
                    switch (qName.toLowerCase()) {
                        case "item" -> {
                            inItem = true;
                            title = approxTraffic = null;
                            newsItems = new ArrayList<>();
                        }
                        case "ht:news_item" -> {
                            inNewsItem = true;
                            newsTitle = newsUrl = newsImage = newsSource = null;
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    switch (qName.toLowerCase()) {
                        case "title" -> {
                            if (inItem && !inNewsItem) {
                                title = buffer.toString();
                            } else if (inNewsItem) {
                                newsTitle = buffer.toString();
                            }
                        }
                        case "ht:approx_traffic" -> approxTraffic = buffer.toString();
                        case "ht:news_item_title" -> newsTitle = buffer.toString();
                        case "ht:news_item_url" -> newsUrl = buffer.toString();
                        case "ht:news_item_picture" -> newsImage = buffer.toString();
                        case "ht:news_item_source" -> newsSource = buffer.toString();
                        case "ht:news_item" -> {
                            if (newsTitle != null && newsUrl != null) {
                                newsItems.add(new NewsItem(newsTitle, newsUrl));
                            }
                            inNewsItem = false;
                        }
                        case "item" -> {
                            if (title != null && !title.isBlank()) {
                                trendMap.put(title, new TrendItem(approxTraffic, newsItems));
                            }
                            inItem = false;
                        }
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    buffer.append(ch, start, length);
                }
            });

        } catch (Exception e) {
            log.error("Exception occurred while parsing Google Trends RSS: {}", e.getMessage(), e);
        }

        return trendMap;
    }
}