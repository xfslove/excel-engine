package me.excel.tools.importer;

import me.excel.tools.factory.ModelFactory;
import me.excel.tools.model.excel.ExcelCell;
import me.excel.tools.model.excel.ExcelSheet;
import me.excel.tools.processor.DataProcessor;
import me.excel.tools.transfer.ExcelFileTransfer;
import me.excel.tools.utils.FieldValueSetter;
import me.excel.tools.utils.ReflectionValueSetter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * excel 文件导入器
 *
 * Created by hanwen on 15-12-16.
 */
public class ExcelFileImporter extends ExcelFileTransfer implements UserFileImporter {

  protected ModelFactory modelFactory;

  protected List<FieldValueSetter> fieldValueSetters = new ArrayList<>();

  protected ReflectionValueSetter reflectionValueSetter = new ReflectionValueSetter();

  @Override
  public void process(File file, DataProcessor dataProcessor) throws IOException {

    if (file == null) {
      throw new IllegalArgumentException("file is null");
    }
    if (dataProcessor == null) {
      throw new IllegalArgumentException("dataProcessor is null");
    }

    FileInputStream inputStream = new FileInputStream(file);

    transfer(inputStream);

    if (excelWorkbook.sizeOfSheets() != 1) {
      throw new IllegalArgumentException("excel workbook only supported size of sheet is one");
    }

    ExcelSheet excelSheet = excelWorkbook.getSheet(0);

    List models = new ArrayList<>();
    excelSheet.getDataRows().forEach(row -> {
      Object model = modelFactory.create(row);

      dataProcessor.preProcessing(model);

      reflectionValueSetter.set(model, row.getCells());

      for (ExcelCell excelCell : row.getCells()) {

        for (FieldValueSetter customValueSetter : fieldValueSetters) {
          if (customValueSetter.matches(excelCell)) {
            customValueSetter.set(model, excelCell);
            break;
          }
        }

      }

      dataProcessor.postProcessing(model);

      models.add(model);
    });

    dataProcessor.handle(models);
  }

  @Override
  public void addFieldValueSetter(FieldValueSetter... setters) {
    if (setters == null) {
      return;
    }
    for (FieldValueSetter setter : setters) {
      this.fieldValueSetters.add(setter);
    }
  }

  @Override
  public void setModelFactory(ModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }
}
