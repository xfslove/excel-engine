package me.excel.tools.validator.cell;

import me.excel.tools.model.excel.ExcelCell;
import me.excel.tools.validator.SkipValidateException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by hanwen on 5/3/16.
 */
public class LocalDateTimeValidator extends AbstractCellValidator {

  private String format;

  public LocalDateTimeValidator(String field, String format) {
    super(field, "格式应该为: " + format, format);
    this.format = format;
  }

  public LocalDateTimeValidator(String field, String format, String errorMessage, String prompt) {
    super(field, errorMessage, prompt);
    this.format = format;
  }

  @Override
  protected boolean customValidate(ExcelCell excelCell) throws SkipValidateException {
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(format);

    try {
      dateTimeFormatter.parseLocalDateTime(excelCell.getValue());
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}