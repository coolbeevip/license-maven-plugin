package com.github.springbees;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;

public class LicensesRepository {

  Set<LicenseEntry> licenseEntries = new HashSet<>();

  public void add(LicenseEntry entry) {
    if (!licenseEntries.contains(entry)) {
      licenseEntries.add(entry);
    } else {
      licenseEntries.stream().filter(e -> entry.equals(e)).findAny()
          .ifPresent(e -> e.addLicenses(entry.getLicense()));
    }

  }

  public Set<LicenseEntry> getLicenseEntries() {
    return licenseEntries;
  }

  public void list(Log log) {
    licenseEntries.stream().forEach(entry -> {
      log.info(entry.toString());
    });
  }

  public void saveNotices(List<String> notices) {
    licenseEntries.stream().forEach(entry -> {
      notices.add("===========================================================================");
      notices.add("Includes content from " + (entry.getOrganization().trim().length() == 0 ? entry
          .getGroupId() : entry.getOrganization()));
      notices.add(entry.getOrganizationUrl().trim().length() == 0 ? entry.getHomePage()
          : entry.getOrganizationUrl());
      String lic = entry.getLicense().entrySet().stream()
          .map(entry1 -> entry1.getKey() + " (" + entry1.getValue() + ")").collect(
              Collectors.joining(","));
      notices.add("* " + entry.getArtifactId() + ", Version " + entry.getVersion() + " (" + entry
          .getHomePage() + ") under " + lic);
      notices.add("");
    });

//    licenseEntries.stream().forEach(entry -> {
//      String fileName =
//          path + "/LICENSE-" + entry.getGroupId() + "-" + entry.getArtifactId()
//              + "-"
//              + entry.getVersion();
//      entry.getLicense().entrySet().stream().filter(entry1 -> entry1.getValue().length() > 0)
//          .forEach(entry1 -> {
//            try {
//              URL url = new URL(entry1.getValue());
//              if (checkUrl(url)) {
//                downloadFile(url, fileName);
//              } else {
//                if (log != null) {
//                  log.error("无效的License地址");
//                }
//              }
//
//              if (log != null) {
//                log.info("download " + entry1.getValue() + " to " + fileName + " success");
//              }
//            } catch (Exception ex) {
//              if (log != null) {
//                log.error("download " + entry1.getValue() + " to " + fileName + " fail");
//              }
//            }
//          });
//    });
  }

  private boolean checkUrl(URL url) {
    try {
      HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      int responseCode = huc.getResponseCode();
      if (HttpURLConnection.HTTP_NOT_FOUND == responseCode) {
        return false;
      } else {
        return true;
      }
    } catch (Exception ex) {
      return false;
    }
  }

  private void downloadFile(URL url, String fileName) throws IOException {
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(fileName);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    fos.close();
    rbc.close();
  }
}