package com.twendee.fpl.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static String getDate(Instant instant) {
        if (instant == null) {
            return "";
        }
        //DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static HttpResponse requestGet(String uri) throws IOException {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("Content-Type", "application/json");
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
        return client.execute(httpGet);
    }
}
