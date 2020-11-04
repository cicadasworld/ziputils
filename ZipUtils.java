import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipFile(File fileToZip, OutputStream os) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(os);
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
    }

    public static void zipFiles(List<String> srcFiles, OutputStream os) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(os);
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) != -1) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
    }

    public static byte[] zipFolder(File dir) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            zipFolder(dir, out);
            return out.toByteArray();
        }
    }

    public static void zipFolder(File dir, OutputStream os) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(os)) {
            zipFile(dir, dir.getName(), zipOut);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) > 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public static void unzipFile(String fileZip, File destDir) throws IOException {
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
            throw new IOException("unzip file is out of destination folder: " + zipEntry.getName());
        }
        return destFile;
    }
}
