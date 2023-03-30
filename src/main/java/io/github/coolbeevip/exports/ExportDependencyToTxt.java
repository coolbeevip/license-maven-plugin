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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhanglei
 */
public class ExportDependencyToTxt extends AbstractExportDependency {

  final MavenProject project;
  Map<String, List<DependencyEntry>> groupDependencies = new HashMap<>();

  public ExportDependencyToTxt(MavenProject project) {
    this.project = project;
  }

  @Override
  void exportBefore(List<String> notices) {
    notices.add("NOTICE");
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
  }

  @Override
  String getExportFileName() {
    return "NOTICE.TXT";
  }
}
