package com.example.excelandjpa.support.poi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SimpleExcelTemplateTest {

  @Test
  void generate() throws IOException {
    final OutputStream out = Files.newOutputStream(
        Paths.get("/Users/green/Desktop/x.xlsx"));

    final ExcelTemplate excelTemplate = new SimpleExcelTemplate();
    excelTemplate.generate("ECPBAA01.xlsx", initialize(), out);
  }

  @Test
  void generateMap() throws IOException {
    final OutputStream out = Files.newOutputStream(
        Paths.get("/Users/green/Desktop/x.xlsx"));

    final ExcelTemplate excelTemplate = new SimpleExcelTemplate();
    excelTemplate.generate("ECPBAA01.xlsx", initializeMap(), out);
  }

  @Test
  void concurrencyMultipleGenerate() throws InterruptedException {
    final ExecutorService executor = Executors.newFixedThreadPool(16);

    IntStream.range(0, 16).forEach(i -> executor.execute(() -> {
      final ExcelTemplate excelTemplate = new SimpleExcelTemplate() {
        @Override
        public void output(SXSSFWorkbook workbook, OutputStream out) throws IOException {
          workbook.write(out);
          System.out.println(i + " completed!");
        }
      };

      try {
        final OutputStream out = Files.newOutputStream(
            Paths.get("/Users/green/Desktop/" + i + ".xlsx"));

        excelTemplate.generate("ECPBAA01.xlsx", initializeMap(), out);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }));

    while (!executor.isTerminated()) {
      if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
        break;
      }
    }

  }

  private Set<Something> initialize() {
    return new HashSet<>(Arrays.asList(
        new Something(1L, LocalDateTime.now(), "X", "Y", "X", (short) 12),
        new Something(2L, LocalDateTime.now(), "X", "Y", "X", (short) 13),
        new Something(3L, LocalDateTime.now(), "X", "Y", "X", (short) 14)
    ));
  }

  private List<Map<String, Object>> initializeMap() {
    Map<String, Object> row = new HashMap<>();
    row.put("rownum", 1);
    row.put("name", "개똥이");
    row.put("firstName", "개");
    row.put("lastName", "똥이");
    row.put("middleName", "진돗개");
    row.put("gender", "수컷");
    row.put("age", 5);

    List<Map<String, Object>> list = new ArrayList<>();
    list.add(row);

    row = new HashMap<>(row);
    row.put("rownum", 2);
    list.add(row);

    row = new HashMap<>(row);
    row.put("rownum", 3);
    list.add(row);
    return list;
  }

  @SuppressWarnings("unused")
  private static class Something {

    private final long rownum;
    private final LocalDateTime name;
    private final String firstName;
    private final String lastName;
    private final String middleName;
    private final short age;

    public Something(long rownum, LocalDateTime name, String firstName, String lastName,
        String middleName, short age) {
      this.rownum = rownum;
      this.name = name;
      this.firstName = firstName;
      this.lastName = lastName;
      this.middleName = middleName;
      this.age = age;
    }

    public long getRownum() {
      return rownum;
    }

    public LocalDateTime getName() {
      return name;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getMiddleName() {
      return middleName;
    }

    public short getAge() {
      return age;
    }
  }

}
