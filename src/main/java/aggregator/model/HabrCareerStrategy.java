package aggregator.model;

import aggregator.vo.Vacancy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HabrCareerStrategy implements Strategy {
    private static final String URL_FORMAT = "https://career.habr.com/vacancies?locations[]=%s&page=%d&skills[]=%d&type=all";
    //https://career.habr.com/vacancies?locations[]=c_678&page=2&skills[]=1012&type=all
    @Override
    public List<Vacancy> getVacancies(String searchString) {
        List<Vacancy> allVacancies = new ArrayList<>();
        int page = 0;
        try {
            while (true) {
                Document doc = getDocument(getAreaId(searchString), page);
                String s = doc.html();
                Elements vacanciesHtmlList = doc.getElementsByAttributeValue("class", "vacancy-card");

                if (vacanciesHtmlList.isEmpty()) break;

                for (Element element : vacanciesHtmlList) {
                    Elements links = element.getElementsByAttributeValue("class", "vacancy-card__title-link");
                    Elements locations = element.getElementsByAttributeValue("class", "vacancy-card__meta");
                    Elements companyName = element.getElementsByAttributeValue("class", "vacancy-card__company-title");
                    Elements salary = element.getElementsByAttributeValue("class", "vacancy-card__salary");

                    Vacancy vacancy = new Vacancy();
                    vacancy.setSiteName("career.habr.com");
                    vacancy.setTitle(links.get(0).text());
                    vacancy.setUrl("https://career.habr.com" + links.get(0).attr("href"));
                    vacancy.setCity(locations.get(0).text());
                    vacancy.setCompanyName(companyName.get(0).text());
                    vacancy.setSalary(salary.size() > 0 ? salary.get(0).text() : "");

                    allVacancies.add(vacancy);
                }
                page++;
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return allVacancies;
    }
    protected Document getDocument(String searchString, int page) throws IOException {
        String url = String.format(URL_FORMAT, searchString, page, 1012);
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0")
                .referrer("https://career.habr.com/")
                .get();
    }

    public static HashMap<String, String> getAreas() {
        HashMap<String, String> areas = new HashMap<>();
        areas.put("Белгород", "c_685");
        areas.put("Волгоград", "c_690");
        areas.put("Воронеж", "c_692");
        areas.put("Иркутск", "c_696");
        areas.put("Казань", "c_698");
        areas.put("Калининград", "c_699");
        areas.put("Калуга", "c_700");
        areas.put("Курск", "c_710");
        areas.put("Липецк", "c_712");
        areas.put("Москва", "c_678");
        areas.put("Нижний Новгород", "c_715");
        areas.put("Новокузнецк", "c_716");
        areas.put("Омск", "c_718");
        areas.put("Орёл", "c_719");
        areas.put("Оренбург", "c_720");
        areas.put("Псков", "c_724");
        areas.put("Ростов-на-Дону", "c_726");
        areas.put("Тула", "c_737");
        areas.put("Уфа", "c_740");
        areas.put("Хабаровск", "c_741");
        areas.put("Чебоксары", "c_743");
        areas.put("Якутск", "c_746");
        areas.put("Баку", "c_876");
        areas.put("Ереван", "c_878");
        areas.put("Гюмри", "c_879");
        areas.put("Брест", "c_880");
        return areas;
    }

    public static String getAreaId(String area) {
        return getAreas().get(area);
    }
}
