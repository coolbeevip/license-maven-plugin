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

package io.github.coolbeevip;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.coolbeevip.parse.ParseMavenCentralRepositorySearch;
import io.github.coolbeevip.pojo.DependencyEntry;
import io.github.coolbeevip.storage.MavenRepositoryStorage;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DependencyLicenseListMojoTest {

  final static DependencyParse parse = new ParseMavenCentralRepositorySearch();
  final static MavenRepositoryStorage store = MavenRepositoryStorage.getInstance();

  @Test
  public void licenseTest() throws JsonProcessingException {
    parse.parseLicense(Optional.empty(), "org.mockito", "mockito-core", "2.23.4");
    DependencyEntry entry = store.get("org.mockito", "mockito-core", "2.23.4");
    assertEquals(entry.toString(), "org.mockito:mockito-core:2.23.4[The MIT License]");
  }

  @Test
  public void duplicateLicenseTest() throws JsonProcessingException {
    parse.parseLicense(Optional.empty(), "mysql", "mysql-connector-java", "8.0.15");
    DependencyEntry entry = store.get("mysql", "mysql-connector-java", "8.0.15");
    assertEquals(entry.toString(),
      "mysql:mysql-connector-java:8.0.15[The GNU General Public License, v2 with FOSS exception]");
  }

  @BeforeClass
  public static void beforeClass() {
    parse.open("mvnrepository.test.mapdb");
  }

  @AfterClass
  public static void afterClass() {
    parse.close();
  }
}
