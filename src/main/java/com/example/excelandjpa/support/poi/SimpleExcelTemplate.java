package com.example.excelandjpa.support.poi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

public class SimpleExcelTemplate extends ExcelTemplate {

  @Override
  public Path findOut(String template) {
    try {
      return Paths
          .get(new ClassPathResource("templates/excel").getURI()).resolve(template);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void output(SXSSFWorkbook workbook, OutputStream out) throws IOException {
    workbook.write(Files.newOutputStream(Paths.get("/Users/green/Desktop/x.xlsx")));
  }
}
