package com.example.excelandjpa.support.poi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class ExcelTemplate {

  public abstract Path findOut(String template);

  public abstract void output(SXSSFWorkbook workbook, OutputStream out) throws IOException;


  public <T> void generate(
      @NonNull String template,
      @NonNull Iterable<T> data,
      OutputStream out
  ) throws IOException {
    final Path xlsx = findOut(template);
    if (Files.notExists(xlsx)) {
      throw new FileNotFoundException();
    }

    try (
        final XSSFWorkbook xssfWorkbook = new XSSFWorkbook(Files.newInputStream(xlsx));
        final SXSSFWorkbook workbook = new SXSSFWorkbook(xssfWorkbook)
    ) {
      final ValueExtractor<T> extractor = new ValueExtractor<>(data);
      final Slot slot = new Slot(xssfWorkbook);

      int rowNum = slot.start;

      final Sheet sheet = workbook.getSheet("form");
      for (T item : data) {
        for (Row slotRow : slot.rows) {
          final Row row = sheet.createRow(rowNum);

          int cols = 0;
          for (Cell slotCell : slotRow) {
            final Cell cell = row.getCell(cols++, MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellStyle(slotCell.getCellStyle());

            final String column = slotCell.getStringCellValue();
            if (StringUtils.hasText(column)) {
              cellValue(cell, extractor.get(item, column));
            }
          }

          rowNum++;
        }

        merging(slot, sheet, rowNum);
      }

      output(workbook, out);
    }
  }

  private void merging(Slot slot, Sheet sheet, int rowNum) {
    for (CellRangeAddress merge : slot.merges) {
      int initRowNum = rowNum - slot.rows.size();

      try {
        sheet.addMergedRegion(
            new CellRangeAddress(
                merge.getFirstRow() + initRowNum,
                merge.getLastRow() + initRowNum,
                merge.getFirstColumn(),
                merge.getLastColumn()
            )
        );
      } catch (IllegalStateException e) {
        log.warn(e.getMessage());
      }
    }
  }

  private <T> void cellValue(Cell cell, T value) {
    if (null == value) {
      return;
    }

    if (value instanceof CharSequence) {
      cell.setCellValue(value.toString());
    } else if (value instanceof Number) {
      cell.setCellValue(((Number) value).doubleValue());
    } else if (value instanceof LocalDateTime) {
      cell.setCellValue((LocalDateTime) value);
    } else if (value instanceof LocalDate) {
      cell.setCellValue((LocalDate) value);
    } else if (value instanceof Date) {
      cell.setCellValue((Date) value);
    } else if (value instanceof Calendar) {
      cell.setCellValue((Calendar) value);
    }
  }


  private static class Slot {

    private final List<Row> rows = new ArrayList<>();
    private final List<CellRangeAddress> merges = new ArrayList<>();
    private final int start;

    public Slot(XSSFWorkbook workbook) {
      final XSSFSheet formSheet = workbook.getSheet("form");
      this.start = formSheet.getLastRowNum() + 1;

      final XSSFSheet slotSheet = workbook.getSheet("slot");
      merges.addAll(slotSheet.getMergedRegions());
      slotSheet.forEach(rows::add);

      workbook.removeSheetAt(workbook.getSheetIndex(slotSheet));
    }

  }


  private static class ValueExtractor<T> {

    private final Map<String, Method> getters;

    public ValueExtractor(@NonNull Iterable<T> data) {
      final Iterator<T> iterator = data.iterator();
      if (!iterator.hasNext()) {
        throw new NoSuchElementException();
      }

      final T first = iterator.next();

      if (first instanceof Map) {
        getters = null;
      } else {
        getters = new HashMap<>();
        for (Method method : first.getClass().getMethods()) {
          final String name = method.getName();
          if (name.startsWith("get") && 0 == method.getParameterCount()) {
            getters.put(name.substring(3, 4).toLowerCase() + name.substring(4), method);
          }
        }
      }
    }

    public Object get(T data, String name) {
      if (null == getters) {
        @SuppressWarnings("unchecked") final Map<String, ?> map = (Map<String, ?>) data;
        return map.get(name);
      }

      if (getters.containsKey(name)) {
        try {
          return getters.get(name).invoke(data);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new NoSuchElementException();
        }
      }

      return null;
    }
  }

}
