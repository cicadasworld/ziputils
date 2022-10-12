package cn.jin;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ZipFileManager {

    private final Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void unzip(Path outputDir) throws IOException {
        if (!Files.isRegularFile(zipFile)) {
            throw new IOException("Wrong zip file: " + zipFile);
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
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

    public void zip(Path source) throws IOException {
        Path zipDir = zipFile.getParent();
        if (Files.notExists(zipDir)) {
            Files.createDirectories(zipDir);
        }

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
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

    private void getFileList(Path path, List<Path> fileNames) throws IOException {
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

    private void addNewZipEntry(ZipOutputStream zipOutputStream, Path filePath, Path fileName) throws IOException {
        Path fullPath = filePath.resolve(fileName);
        ZipEntry entry = new ZipEntry(fileName.toString());
        zipOutputStream.putNextEntry(entry);
        Files.copy(fullPath, zipOutputStream);
        zipOutputStream.closeEntry();
    }
}
