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

package com.github.coolbeevip.exports;

import com.github.coolbeevip.pojo.DependencyEntry;
import java.util.List;
import java.util.stream.Collectors;

public class ExportDependencyToCsv extends AbstractExportDependency {

  @Override
  void exportBefore(List<String> notices) {
    notices.add("groupId\tartifactId\tversion\tlicense\torganization\turl");
  }

  @Override
  void exportContent(List<String> notices, DependencyEntry entry) {
    String line = String
      .format("%s\t%s\t%s\t%s\t%s\t%s", entry.getGroupId(), entry.getArtifactId(),
        entry.getVersion(),
        entry.getLicense().keySet().stream().collect(Collectors.joining("/")),
        entry.getOrganization(),
        entry.getHomePage());
    notices.add(line);
  }

  @Override
  void exportContentAfter(List<String> notices) {

  }


  @Override
  String getExportFileName() {
    return "NOTICE.CSV";
  }
//
//  @Override
//  public void export(Log log, List<String> notices, Path path) {
//    notices.add("groupId\tartifactId\tversion\tlicense\torganization\turl");
//    MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();
//    store.stream().sorted().map(v -> {
//      try {
//        return jsonMapper.readValue(v, DependencyEntry.class);
//      } catch (JsonProcessingException e) {
//        return null;
//      }
//    }).forEach(entry -> {
//      if (entry != null) {
//        String line = String
//          .format("%s\t%s\t%s\t%s\t%s\t%s", entry.getGroupId(), entry.getArtifactId(),
//            entry.getVersion(),
//            entry.getLicense().keySet().stream().collect(Collectors.joining("/")),
//            entry.getOrganization(),
//            entry.getHomePage());
//        notices.add(line);
//        log.info(line);
//      }
//    });
//    if (!Files.exists(path)) {
//      try {
//        Files.createDirectories(path);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//    try (BufferedWriter writer = new BufferedWriter(
//      new FileWriter(path.toAbsolutePath() + "/NOTICE.CSV"))) {
//      for (String l : notices) {
//        writer.write(l + "\r\n");
//      }
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//    log.info("write " + path.toAbsolutePath() + "/NOTICE.CSV");
//  }
}
