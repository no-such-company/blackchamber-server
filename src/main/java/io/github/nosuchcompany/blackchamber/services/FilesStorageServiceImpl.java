package io.github.nosuchcompany.blackchamber.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import static io.github.nosuchcompany.blackchamber.constants.Constants.*;

@Service
public class FilesStorageServiceImpl implements FileStorageService {

    private final Path root = Paths.get(BC_STORAGE_TEMP);

    @Override
    public void init() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException(COULD_NOT_INITIALIZE_FOLDER_FOR_UPLOAD);
        }
    }

    @Override
    public void save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException(COULD_NOT_STORE_THE_FILE_ERROR + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException(COULD_NOT_READ_THE_FILE);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(ERROR + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException(COULD_NOT_LOAD_THE_FILES);
        }
    }
}