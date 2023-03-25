package aggregator;

import aggregator.model.HHStrategy;
import aggregator.model.HabrCareerStrategy;
import aggregator.model.Model;
import aggregator.model.Provider;
import aggregator.view.HtmlView;

public class Aggregator {
    public static void main(String[] args) {
        Provider hh = new Provider(new HHStrategy());
        Provider habr = new Provider(new HabrCareerStrategy());

        HtmlView view = new HtmlView();
        Model model = new Model(view, habr);
        Controller controller = new Controller(model);

        view.setController(controller);
        view.userRegionSelectEmulationMethod("Москва");
    }
}