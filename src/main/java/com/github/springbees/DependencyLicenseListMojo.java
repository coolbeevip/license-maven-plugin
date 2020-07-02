package com.github.springbees;

import java.util.List;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Mojo(name = "dependency-license-list", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class DependencyLicenseListMojo
    extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Parameter(property = "scope")
  String scope;

  Parse parse = new ParseMvnRepository();

  public void execute() {
    getLog().debug("ignore dependency groupId [" + project.getGroupId() + "]");
    List<Dependency> dependencies = project.getDependencies();
    dependencies.parallelStream()
        .filter(d -> !project.getGroupId().equals(d.getGroupId()))
        .filter(d -> scope == null || scope.isEmpty() || scope.equals(d.getScope()))
        .forEach(dependency -> {
          LicensesRepository licensesRepository = parse
              .parseLicense(Optional.of(getLog()),dependency.getGroupId(), dependency.getArtifactId(),
                  dependency.getVersion());
          licensesRepository.list(getLog());
        });
  }

}
