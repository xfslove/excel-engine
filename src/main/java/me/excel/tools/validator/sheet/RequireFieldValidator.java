package me.excel.tools.validator.sheet;

import me.excel.tools.model.excel.Cell;
import me.excel.tools.model.excel.Row;
import me.excel.tools.model.excel.Sheet;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 * required field validator
 *
 * all validators matches by field,
 * if field lost means all the ({@link me.excel.tools.validator.cell.CellValidator} and {@link me.excel.tools.setter.FieldValueSetter}) of this field will skip.
 * this validator useful to detect if excel files contains all the fields you want afterSheet.
 *
 * eg: class A has fields [A, B...].
 * if you want modify A, B, supplied the {@link RequireFieldValidator#requireFields} as [A, B]. when the excel files lost A or B,
 * this validator will get false.
 * </pre>
 * Created by hanwen on 4/26/16.
 */
public class RequireFieldValidator implements SheetValidator {

  private List<String> requireFields = new ArrayList<>();

  private String errorMessage;

  public RequireFieldValidator(String... requireFields) {
    Collections.addAll(this.requireFields, requireFields);
    this.errorMessage = "不包含所有要求的字段";
  }

  public RequireFieldValidator(String errorMessage, String... requireFields) {
    Collections.addAll(this.requireFields, requireFields);
    this.errorMessage = errorMessage;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public boolean valid(Sheet sheet) {
    Row fieldRow = sheet.getFieldRow();

    List<String> fields = new ArrayList<>();

    for (Cell fieldCell : fieldRow.getCells()) {
      fields.add(fieldCell.getValue());
    }

    return CollectionUtils.subtract(requireFields, fields).isEmpty();

  }

}
