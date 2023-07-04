package kickstart.verkaufsbereich.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Date;

@Service
public class InvoiceService {

    public ResponseEntity<ByteArrayResource> generateInvoice(Order order) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();
            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            Chunk chunk = new Chunk("Rechnung", font);

            document.add(new Paragraph(chunk));
            document.add(new Paragraph(new Date().toString()));

            for (OrderLine orderLine : order.getOrderLines()) {
                document.add(new Paragraph(orderLine.getProductName()+ " - " + orderLine.getQuantity() + " - " + orderLine.getPrice()));
            }

            document.add(new Paragraph("Gesamtsumme: " + order.getTotal()));

            document.close();

            byte[] invoiceBytes = out.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(invoiceBytes);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=Rechnung.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
