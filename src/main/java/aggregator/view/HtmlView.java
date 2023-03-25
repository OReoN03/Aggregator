package aggregator.view;

import aggregator.Controller;
import aggregator.vo.Vacancy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.util.List;

public class HtmlView implements View {
    private Controller controller;
    private final String filePath = "./src/main/java/" +
            this.getClass().getPackage().getName().replaceAll("[.]", "/") +
            "/vacancies.html";
    private final String backupFilePath = "./src/main/java/" +
            this.getClass().getPackage().getName().replaceAll("[.]", "/") +
            "/backup.html";

    @Override
    public void update(List<Vacancy> vacancies) {
        try {
            reFillFile();
            String newContent = getUpdatedFileContent(vacancies);
            updateFile(newContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void reFillFile() {
        File file = new File(filePath);
        if(file.delete()){
            try {
                if (file.createNewFile()) {
                    InputStream is = new FileInputStream(backupFilePath);
                    OutputStream os = new FileOutputStream(filePath);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    is.close();
                    os.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else{
            throw new RuntimeException("File still exists.");
        }
    }
    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void userRegionSelectEmulationMethod(String area) {
        controller.onCitySelect(area);
     }

    private String getUpdatedFileContent(List<Vacancy> vacancies) {
        try {
            Document doc = getDocument();
            Elements templateHidden = doc.getElementsByClass("template");
            Element template = templateHidden.clone().removeAttr("style").removeAttr("template").get(0);

            Elements prevVacancies = doc.getElementsByClass("vacancy");

            for (Element redundant : prevVacancies) {
                if (!redundant.hasClass("template")) {
                    redundant.remove();
                }
            }

            for (Vacancy vacancy : vacancies) {
                Element vacancyElement = template.clone();

                Element vacancyLink = vacancyElement.getElementsByAttribute("href").get(0);
                vacancyLink.appendText(vacancy.getTitle());
                vacancyLink.attr("href", vacancy.getUrl());
                Element city = vacancyElement.getElementsByClass("city").get(0);
                if (vacancy.getSiteName().equals("career.habr.com")) {
                    String cityStr = "";
                    String[] str = vacancy.getCity().split("• ");
                    for (int i = 0; i < str.length; i++) {
                        if (!str[i].trim().equals("Полный рабочий день") && !str[i].trim().equals("Можно удаленно")) {
                            cityStr += str[i] + " ";
                        } else {
                            break;
                        }
                    }
                    cityStr = cityStr.trim();
                    city.appendText(cityStr);
                }
                else {
                    city.appendText(vacancy.getCity());
                }
                Element companyName = vacancyElement.getElementsByClass("companyName").get(0);
                companyName.appendText(vacancy.getCompanyName());
                Elements salaryEls = vacancyElement.getElementsByClass("salary");
                Element salary = salaryEls.get(0);
                salary.appendText(vacancy.getSalary());

                templateHidden.before(vacancyElement.outerHtml());
            }
            return doc.html();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateFile(String content) {
        File file = new File(filePath);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
             outputStream.write(content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Document getDocument() throws IOException {
        return Jsoup.parse(new File(filePath), "UTF-8");
    }
}
