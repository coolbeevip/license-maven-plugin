/**
 * Copyright © 2020 Lei Zhang (zhanglei@apache.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolbeevip.exports;

import io.github.coolbeevip.pojo.DependencyEntry;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhanglei
 */
public class ExportDependencyToPom extends AbstractExportDependency {

  final MavenProject project;
  Map<String, List<DependencyEntry>> groupDependencies = new HashMap<>();

  public ExportDependencyToPom(MavenProject project) {
    this.project = project;
  }

  @Override
  void exportBefore(List<String> notices) {
    notices.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    notices.add("");
    notices.add("<!-- license-maven-plugin automatically created by " + new Date() + "-->");
    notices.add("");
    notices.add("<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
    notices.add("");
    notices.add("  <modelVersion>4.0.0</modelVersion>");
    notices.add("  <groupId>" + project.getGroupId() + "</groupId>");
    notices.add("  <artifactId>" + project.getArtifactId() + "</artifactId>");
    notices.add("  <version>" + project.getVersion() + "</version>");
    notices.add("  <packaging>pom</packaging>");
    notices.add("");
    notices.add("  <dependencies>");

  }

  @Override
  void exportContent(List<String> notices, DependencyEntry entry) {
    if (groupDependencies.containsKey(entry.getGroupId())) {
      groupDependencies.get(entry.getGroupId()).add(entry);
    } else {
      List<DependencyEntry> dependencyEntries = new ArrayList<>();
      dependencyEntries.add(entry);
      groupDependencies.put(entry.getGroupId(), dependencyEntries);
    }
  }

  @Override
  void exportContentAfter(List<String> notices) {
    groupDependencies.entrySet().stream().forEach(entry -> {
      String groupId = entry.getKey();
      entry.getValue().stream().forEach(e -> {
        notices.add("    <dependency>");
        notices.add("      <groupId>" + groupId + "</groupId>");
        notices.add("      <artifactId>" + e.getArtifactId() + "</artifactId>");
        notices.add("      <version>" + e.getVersion() + "</version>");
        if (e.getScope() != null) {
          notices.add("    <scope>" + e.getScope() + "</scope>");
        }
        notices.add("    </dependency>");
      });
    });
    notices.add("  </dependencies>");
    notices.add("</project>");
  }

  @Override
  String getExportFileName() {
    return String.format("%s-%s-flatten-pom.xml", project.getGroupId().replace(".", "_"),
        project.getArtifactId());
  }
}
