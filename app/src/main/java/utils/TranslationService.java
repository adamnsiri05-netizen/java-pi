package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TranslationService {
    private static final String ENDPOINT = "https://ldfy.cc/translation/language/translate";
    private static final String API_KEY = "da7fefac73af458c9484821cfc04275a";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isEmpty() || sourceLang.equals(targetLang)) {
            return text;
        }
        try {
            String body = String.format("{\"text\":%s,\"sourceLang\":\"%s\",\"targetLang\":\"%s\",\"type\":\"2\"}",
                    jsonEscape(text), sourceLang, targetLang);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String bodyResp = resp.body();
                int idx = bodyResp.indexOf("\"translatedText\":");
                if (idx >= 0) {
                    int start = bodyResp.indexOf('"', idx + 17) + 1;
                    int end = bodyResp.indexOf('"', start);
                    if (start >=0 && end >=0) {
                        return bodyResp.substring(start, end);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return text;
    }

    private static String jsonEscape(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
