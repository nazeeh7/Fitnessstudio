package kickstart.mitarbeiter;

import java.io.OutputStream;
import java.util.Map;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import com.lowagie.text.DocumentException;

@Service
public class PdfService {
    private final TemplateEngine templateEngine;

    @Autowired
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void createPdf(String templateName, Map<String,Object> variables, OutputStream outputStream) {
        // Erstelle HTML-Content aus dem Thymeleaf Template
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = this.templateEngine.process(templateName, context);

        // Konvertiere HTML zu PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        try {
            renderer.createPDF(outputStream);
        } catch (DocumentException e) {
            // handle exception
        }
    }
}