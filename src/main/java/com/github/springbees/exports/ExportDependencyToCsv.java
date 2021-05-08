package com.github.springbees.exports;

import com.github.springbees.pojo.DependencyEntry;
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
