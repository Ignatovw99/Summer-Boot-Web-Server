package summer.util;

import summer.api.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TemplateEngine {

    private static final String RENDER_TEMPLATE_PATTERN = "${%s}";

    private static final String TEMPLATE_FILE_EXTENSION = ".html";

    private final String applicationsTemplateFolder;

    public TemplateEngine(String applicationsTemplateFolder) {
        this.applicationsTemplateFolder = applicationsTemplateFolder;
    }

    private String renderTemplate(String templateContent, Model model) {
        String renderedContent = templateContent;

        for (Map.Entry<String, Object> viewDataObject : model.getAttributes().entrySet()) {
            renderedContent = renderedContent.replace(
                    String.format(RENDER_TEMPLATE_PATTERN, viewDataObject.getKey()),
                    viewDataObject.getValue().toString()
            );
        }

        return renderedContent;
    }

    private Model getNewModel() {
        return new Model();
    }

    public String loadTemplate(String templateName) throws IOException {
        return this.loadTemplate(templateName, this.getNewModel());
    }

    public String loadTemplate(String templateName, Model model) throws IOException {
        String templateContent = String.join(
                "",
                Files.readAllLines(Paths.get(this.applicationsTemplateFolder.concat(templateName).concat(TEMPLATE_FILE_EXTENSION)))
        );

        if (model != null) {
            templateContent = this.renderTemplate(templateContent, model);
        }

        return templateContent;
    }
}
