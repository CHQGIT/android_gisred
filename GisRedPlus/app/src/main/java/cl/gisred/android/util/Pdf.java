package cl.gisred.android.util;

import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.HTMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Created by cramiret on 11-08-2016.
 */
public class Pdf {

    private String sHtml;

    public Pdf (String mHtml) {
        sHtml = mHtml;
    }

    public boolean create (String htmlText, String absoluteFilePath) {
        try {
            String k = htmlText;
            /*k = "<html>\n" +
                "<head>\n" +
                "    <style>.col{padding:3px 20px 3px 20px}</style>\n" +
                "</head>\n" +
                "<body style=\"font-family:tahoma\">\n" +
                "    <div style=\"background:rgb(230,230,230); padding:5px ;border:1px solid black;\">\n" +
                "    <b style=\"color:rgb(51,153,255)\">Sample header</b>\n" +
                "    </div>\n" +
                "    <br />\n" +
                "    <table border='0' style='border-collapse: collapse;'>\n" +
                "    <tr>\n" +
                "    <td class=\"col\">String 1</td>\n" +
                "    <td class=\"col\">: 1234354545</td> \n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "    <td class=\"col\">String 2</td>\n" +
                "    <td class=\"col\">: rere</td> \n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "    <td class=\"col\">String 3</td>\n" +
                "    <td class=\"col\">: ureuiu</td> \n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "    <td class=\"col\">Date</td>\n" +
                "    <td class=\"col\">: dfdfjkdjk</td> \n" +
                "    </tr>\n" +
                "    </table>\n" +
                "    <br />\n" +
                "    <br />\n" +
                "    <br />\n" +
                "    <hr />\n" +
                "    <br />\n" +
                "    Contact us\n" +
                "</body>\n" +
                "</html>";*/

            Document document = new Document(PageSize.LETTER);
            OutputStream file = new FileOutputStream(absoluteFilePath + "index.pdf");
            PdfWriter oPdfWriter = PdfWriter.getInstance(document, file);
            document.open();
            //HTMLWorker htmlWorker = new HTMLWorker(document);
            XMLWorkerHelper.getInstance().parseXHtml(oPdfWriter, document, new StringReader(k));
            //htmlWorker.parse(new StringReader(k));
            document.close();
            file.close();
            return true;
        } catch (Exception e) {
            /*File file = new File(absoluteFilePath);
            if(file.exists()) {
                boolean isDeleted = file.delete();
                Log.i("CHECKING", "PDF isDeleted: " + isDeleted);
            }
            Log.e("Exception: " + e.getMessage());*/
            e.printStackTrace();
            return false;
        }
    }
}
