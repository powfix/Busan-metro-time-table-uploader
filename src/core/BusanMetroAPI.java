package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BusanMetroAPI {
    private static final String CHARACTER_SET = "UTF-8";

    private static class PARAMETER {
        private static class HEADER {
            private static final String SERVICE_KEY = "ServiceKey";         // API Service key
            private static final String RESPONSE_TYPE = "act";              // Response type set) json, xml, xls
            private static final String STATION_CODE = "scode";             // Metro station code
            private static final String NUM_OF_ROWS = "numOfRows";           // 요청시 응답할 최대 row의 개수
            private static final String DAY_CODE = "day";                   // Day code set) 0, 1, 2
        }

        private static class VALUE {
            private static final String SERVICE_KEY = "1ZDBJy%2FOG7bUAzilIO4WmyjRhKqcGBaFTEQIkLfXQoMz9cv4UKlTfhgVghmJe9wukMYxBfSghoLyWbXyqdc9Dg%3D%3D";
            private static final String RESPONSE_TYPE = "json";
            private static final int NUM_OF_ROWS = 100000;
        }
    }

    public static String getResult(int sCode) {
        try {
            StringBuilder urlBuilder = new StringBuilder("http://data.humetro.busan.kr/voc/api/open_api_process.tnn");
            urlBuilder.append('?').append(URLEncoder.encode(PARAMETER.HEADER.SERVICE_KEY, CHARACTER_SET)).append('=').append(PARAMETER.VALUE.SERVICE_KEY)                                              /* API Key */
                    .append('&').append(URLEncoder.encode(PARAMETER.HEADER.RESPONSE_TYPE, CHARACTER_SET)).append('=').append(URLEncoder.encode(PARAMETER.VALUE.RESPONSE_TYPE, CHARACTER_SET))          /* Response type */
                    .append('&').append(URLEncoder.encode(PARAMETER.HEADER.STATION_CODE, CHARACTER_SET)).append('=').append(URLEncoder.encode(String.valueOf(sCode), CHARACTER_SET))                                  /* Station code */
                    .append('&').append(URLEncoder.encode(PARAMETER.HEADER.NUM_OF_ROWS, CHARACTER_SET)).append('=').append(URLEncoder.encode(String.valueOf(PARAMETER.VALUE.NUM_OF_ROWS), CHARACTER_SET));

            String tmp = urlBuilder.toString();
            System.out.println("Request URL : " + tmp);
            URL url = new URL(tmp);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-type", "application/json");
            // System.out.println("Response code : " + String.valueOf(httpConnection.getResponseCode()));
            BufferedReader reader = (httpConnection.getResponseCode() >= 200 && httpConnection.getResponseCode() <= 300)
                    ? new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "euc-kr"))
                    : new BufferedReader(new InputStreamReader(httpConnection.getErrorStream(), "euc-kr"));
            StringBuilder builder = new StringBuilder();
            tmp = null;
            while ((tmp = reader.readLine()) != null) {
                builder.append(tmp);
            }
            reader.close();
            httpConnection.disconnect();

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
