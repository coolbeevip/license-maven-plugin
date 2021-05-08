package com.github.springbees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springbees.pojo.DependencyEntry;
import com.github.springbees.storage.MavenRepositoryStorage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;

public class ExportNOTICE implements Export {

  final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public void export(Log log,
    List<String> notices, Path path) {
    Map<String, List<DependencyEntry>> groupDependencies = new HashMap<>();
    MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();
    store.stream().sorted().map(v -> {
      try {
        return jsonMapper.readValue(v, DependencyEntry.class);
      } catch (JsonProcessingException e) {
        return null;
      }
    }).forEach(entry -> {
      if (entry != null) {
        if (groupDependencies.containsKey(entry.getGroupId())) {
          groupDependencies.get(entry.getGroupId()).add(entry);
        } else {
          List<DependencyEntry> dependencyEntries = new ArrayList<>();
          dependencyEntries.add(entry);
          groupDependencies.put(entry.getGroupId(), dependencyEntries);
        }
      }
    });

    groupDependencies.entrySet().stream().forEach(entry -> {
      String groupId = entry.getKey();
      notices.add("===========================================================================");
      notices.add("Includes content from " + groupId);
      notices.add(entry.getValue().stream().findAny().get().getOrganization());
      entry.getValue().stream().forEach(e -> {
        String lic = e.getLicense().entrySet().stream()
          .map(entry1 -> entry1.getKey() + " (" + entry1.getValue() + ")")
          .collect(Collectors.joining(","));
        notices.add("* " + e.getArtifactId() + ", Version " + e.getVersion() + " (" + e
          .getHomePage() + ") under " + lic);
      });
      notices.add("");
    });

    if (!Files.exists(path)) {
      try {
        Files.createDirectories(path);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (BufferedWriter writer = new BufferedWriter(
      new FileWriter(path.toAbsolutePath() + "/NOTICE"))) {
      for (String l : notices) {
        writer.write(l + "\r\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("write " + path.toAbsolutePath() + "/NOTICE");
  }
}
