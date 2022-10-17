package cn.jin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class ZipUtils {

    private ZipUtils() {}

    public static void unzip(Path zipFile, Path outputDir) throws IOException {
        unzip(zipFile, outputDir, StandardCharsets.UTF_8);
    }

    public static void unzip(Path zipFile, Path outputDir, Charset charset) throws IOException {
        if (!Files.isRegularFile(zipFile)) {
            throw new IOException("Wrong zip file: " + zipFile);
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile), charset)) {
            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }
                Path fileFullName = outputDir.resolve(zipEntry.getName());
                // prevent zip slip vulnerability.
                // make sure normalized file still has outputDir as its prefix else throw exception
                Path normalizePath = fileFullName.normalize();
                if (!normalizePath.startsWith(outputDir)) {
                    throw new IOException("unzip file is out of destination directory: " + zipEntry.getName());
                }

                Path parent = normalizePath.getParent();
                if (Files.notExists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.copy(zipInputStream, normalizePath, REPLACE_EXISTING);
            }
        }
    }

    public static void zip(Path zipFile, Path source) throws IOException {
        zip(zipFile, source, StandardCharsets.UTF_8);
    }

    public static void zip(Path zipFile, Path source, Charset charset) throws IOException {
        Path zipDir = zipFile.getParent();
        if (Files.notExists(zipDir)) {
            Files.createDirectories(zipDir);
        }

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile), charset)) {
            if (Files.isDirectory(source)) {
                List<Path> fileNames = new ArrayList<>();
                getFileList(source, fileNames);
                for (Path fileName : fileNames) {
                    addNewZipEntry(zipOutputStream, source, fileName);
                }
            } else if (Files.isRegularFile(source)) {
                addNewZipEntry(zipOutputStream, source.getParent(), source.getFileName());
            } else {
                throw new IOException("path: " + source + " is not found");
            }
        }
    }

    private static void getFileList(Path path, List<Path> fileNames) throws IOException {
        if (Files.isRegularFile(path)) {
            fileNames.add(path);
        } else if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
                for (Path file : dirStream) {
                    getFileList(file, fileNames);
                }
            }
        }
    }

    private static void addNewZipEntry(ZipOutputStream zipOutputStream, Path filePath, Path fileName) throws IOException {
        Path fullPath = filePath.resolve(fileName);
        ZipEntry entry = new ZipEntry(filePath.getParent().relativize(fileName).toString());
        zipOutputStream.putNextEntry(entry);
        Files.copy(fullPath, zipOutputStream);
        zipOutputStream.closeEntry();
    }
}
