import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class UnzipFile {
    public static void main(String[] args) throws IOException {
        String fileZip = "src/main/resources/unzipTest/compressed.zip";
        File destDir = new File("src/main/resources/unzipTest");

        byte[] bytes = new byte[1024];
        FileInputStream fis = new FileInputStream(fileZip);
        ZipInputStream zipIn = new ZipInputStream(fis);
        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int length;
            while ((length = zipIn.read(bytes)) > 0) {
                fos.write(bytes, 0, length);
            }
            fos.close();
            zipEntry = zipIn.getNextEntry();
        }
        zipIn.closeEntry();
        zipIn.close();
    }

    private static File newFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());
        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("该解压项在目标文件夹之外: " + zipEntry.getName());
        }
        return destFile;
    }
}
