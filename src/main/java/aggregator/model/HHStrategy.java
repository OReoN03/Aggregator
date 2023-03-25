package aggregator.model;

import aggregator.vo.Vacancy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HHStrategy implements Strategy {
    private static final String URL_FORMAT = "https://hh.ru/search/vacancy?text=java&from=suggest_post&area=%s&page=%d";
    @Override
    public List<Vacancy> getVacancies(String searchString) {
        List<Vacancy> allVacancies = new ArrayList<>();
        int areaId = getAreaId(searchString);
        int page = 0;
        try {
            while (true) {
                if (page > 39) break;
                Document doc = getDocument(String.valueOf(areaId), page);
                Elements vacanciesHtmlList = doc.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy vacancy-serp__vacancy_standard");
                vacanciesHtmlList.addAll(doc.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy vacancy-serp__vacancy_standard_plus"));
                vacanciesHtmlList.addAll(doc.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy vacancy-serp__vacancy_premium"));

                if (vacanciesHtmlList.isEmpty()) break;

                for (Element element : vacanciesHtmlList) {
                    Elements links = element.getElementsByAttributeValue("data-qa", "serp-item__title");
                    Elements locations = element.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy-address");
                    Elements companyName = element.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy-employer");
                    Elements salary = element.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy-compensation");

                    Vacancy vacancy = new Vacancy();
                    vacancy.setSiteName("hh.ru");
                    vacancy.setTitle(links.get(0).text());
                    vacancy.setUrl(links.get(0).attr("href"));
                    vacancy.setCity(locations.get(0).text());
                    vacancy.setCompanyName(companyName.get(0).text());
                    vacancy.setSalary(salary.size() > 0 ? salary.get(0).text() : "");

                    allVacancies.add(vacancy);
                }
                page++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return allVacancies;
    }

    protected Document getDocument(String searchString, int page) throws IOException {
        String url = String.format(URL_FORMAT, searchString, page);
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0")
                .referrer("https://hh.ru/")
                .get();
    }

    public static HashMap<String, Integer> getAreas(String url) {
        HashMap<String, Integer> areas = new HashMap<>();
        try {
            JSONArray array = new JSONArray(stream(new URL(url)));
            for (int k = 0; k < array.length(); k++) {
                JSONObject object = array.getJSONObject(k);
                JSONArray ar1 = object.getJSONArray("areas");
                for (int i = 0; i < ar1.length(); i++) {
                    JSONObject object1 = array.getJSONObject(k);
                    JSONArray ar2 = object1.getJSONArray("areas");
                    if (ar2.length() != 0) {
                        for (int j = 0; j < ar2.length(); j++) {
                            JSONObject object2 = ar2.getJSONObject(j);
                            areas.put(object2.getString("name"), object2.getInt("id"));
                        }
                    }
                    else {
                        areas.put(object1.getString("name"), object1.getInt("id"));
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return areas;
    }

    public static String stream(URL url) {
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            return json.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getAreaId(String area) {
        return getAreas("https://api.hh.ru/areas").get(area);
    }
}
