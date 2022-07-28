package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dao.DaoImpl;
import org.example.entity.DbEntity;
import org.example.exception.FileNotFoundException;
import org.example.exception.FileSizeException;
import org.example.exception.PathNotValidException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class Service {

    private static final int maxSize = 209715200;

    private final DaoImpl dao = new DaoImpl();

    public void upload(String location) {
        if (StringUtils.isEmpty(location)) {
            throw new PathNotValidException();
        }
        readFile(location).ifPresentOrElse(dao::save,
                () -> log.info("Something was wrong :C"));

    }

    public void download(String filename, String location) {
        if (StringUtils.isAnyEmpty(filename, location)) {
            throw new PathNotValidException("path or location is null");
        }
        dao.findUsingStored(filename).ifPresentOrElse(entity -> writeFile(location, entity.getContent()),
                FileNotFoundException::new);
    }

    private void writeFile(String location, byte[] binary) {
        if (StringUtils.isEmpty(location)) {
            throw new PathNotValidException();
        }
        if (Objects.isNull(binary)) {
            throw new RuntimeException("binary is null!");
        }
        var file = new File(location);
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), binary);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    private Optional<DbEntity> readFile(String location) {
        try (InputStream fis = new FileInputStream(location)) {
            var file = new File(location);
            var content = Files.readAllBytes(file.toPath());
            if (maxSize < content.length) {
                throw new FileSizeException();
            }
            var entityToSave = DbEntity.builder()
                    .name(file.getName())
                    .content(Files.readAllBytes(file.toPath()))
                    .build();
            return Optional.of(entityToSave);
        } catch (IOException e) {
            log.info(e.getMessage());
            return Optional.empty();
        }
    }
}
