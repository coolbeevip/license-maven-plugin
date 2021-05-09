/**
 * Copyright © 2020 Lei Zhang (zhanglei@apache.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolbeevip.exports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coolbeevip.DependencyExport;
import io.github.coolbeevip.pojo.DependencyEntry;
import io.github.coolbeevip.storage.MavenRepositoryStorage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractExportDependency implements DependencyExport {

  public final ObjectMapper jsonMapper = new ObjectMapper();

  abstract void exportBefore(List<String> notices);

  abstract void exportContent(List<String> notices, DependencyEntry entry);

  abstract void exportContentAfter(List<String> notices);

  abstract String getExportFileName();

  @Override
  public void export(Map<String, Dependency> exportDependencies, Log log, List<String> notices,
      Path path) {
    exportBefore(notices);
    MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();

    store.stream().filter(entry -> exportDependencies.containsKey(entry.getKey())).map(
        Entry::getValue).sorted().map(v -> {
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
