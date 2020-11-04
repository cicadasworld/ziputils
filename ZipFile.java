import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream("src/main/resources/compressed.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File fileToZip = new File("src/main/resources/test1.txt");
        FileInputStream fis = new FileInputStream(fileToZip);

        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read()) != -1) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
        zipOut.close();
        fos.close();
    }
}
