package com.github.springbees.exports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springbees.DependencyExport;
import com.github.springbees.pojo.DependencyEntry;
import com.github.springbees.storage.MavenRepositoryStorage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractExportDependency implements DependencyExport {

  public final ObjectMapper jsonMapper = new ObjectMapper();

  abstract void exportBefore(List<String> notices);

  abstract void exportContent(List<String> notices, DependencyEntry entry);

  abstract void exportContentAfter(List<String> notices);

  abstract String getExportFileName();

  @Override
  public void export(Log log, List<String> notices, Path path) {
    exportBefore(notices);
    MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();
    store.stream().sorted().map(v -> {
      try {
        return jsonMapper.readValue(v, DependencyEntry.class);
      } catch (JsonProcessingException e) {
        return null;
      }
    }).forEach(entry -> {
      if (entry != null) {
        exportContent(notices, entry);
      }
    });
    exportContentAfter(notices);
    if (!Files.exists(path)) {
      try {
        Files.createDirectories(path);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (BufferedWriter writer = new BufferedWriter(
      new FileWriter(path.toAbsolutePath() + "/" + getExportFileName()))) {
      for (String l : notices) {
        writer.write(l + "\r\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("write " + path.toAbsolutePath() + "/" + getExportFileName());
  }
}
