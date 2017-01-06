package spread.sheet.w2o.validator;

import org.apache.commons.collections.CollectionUtils;
import spread.sheet.model.core.Row;
import spread.sheet.model.core.Sheet;
import spread.sheet.model.message.ErrorMessage;
import spread.sheet.model.message.ErrorMessageBean;
import spread.sheet.model.message.MessageWriteStrategies;
import spread.sheet.model.meta.SheetMeta;
import spread.sheet.w2o.processor.WorkbookProcessException;
import spread.sheet.w2o.validator.cell.CellValidator;
import spread.sheet.w2o.validator.engine.DependencyCycleCheckEngine;
import spread.sheet.w2o.validator.engine.DependencyEngineHelper;
import spread.sheet.w2o.validator.engine.DependencyValidateEngine;
import spread.sheet.w2o.validator.row.RowValidator;
import spread.sheet.w2o.validator.sheet.SheetValidator;

import java.util.*;

/**
 * Created by hanwen on 15-12-16.
 */
public class DefaultSheetValidateHelper implements SheetValidateHelper {

  private Sheet sheet;

  private SheetMeta sheetMeta;

  /*===============
    validators
   ================*/
  private List<SheetValidator> sheetValidators = new ArrayList<>();
  private Map<String, List<RowValidator>> group2rowValidators = new LinkedHashMap<>();
  private Map<String, List<CellValidator>> group2cellValidators = new LinkedHashMap<>();

  /*==============
    error messages
   ===============*/
  private List<ErrorMessage> errorMessages = new ArrayList<>();

  @Override
  public SheetValidateHelper sheetValidator(SheetValidator... validators) {
    if (validators == null) {
      return this;
    }
    Collections.addAll(this.sheetValidators, validators);
    return this;
  }

  @Override
  public SheetValidateHelper rowValidator(RowValidator... validators) {
    if (validators == null) {
      return this;
    }

    for (RowValidator validator : validators) {
      String group = validator.getGroup();

      if (!group2rowValidators.containsKey(group)) {
        group2rowValidators.put(group, new ArrayList<RowValidator>());
      }
      group2rowValidators.get(group).add(validator);
    }
    return this;
  }

  @Override
  public SheetValidateHelper cellValidator(CellValidator... validators) {
    if (validators == null) {
      return this;
    }
    for (CellValidator validator : validators) {
      String group = validator.getGroup();

      if (!group2cellValidators.containsKey(group)) {
        group2cellValidators.put(group, new ArrayList<CellValidator>());
      }
      group2cellValidators.get(group).add(validator);
    }
    return this;
  }

  @Override
  public SheetValidateHelper sheet(Sheet sheet) {
    this.sheet = sheet;
    return this;
  }

  @Override
  public SheetValidateHelper sheetMeta(SheetMeta sheetMeta) {
    this.sheetMeta = sheetMeta;
    return this;
  }

  public List<ErrorMessage> getErrorMessages() {
    return errorMessages;
  }

  @Override
  public boolean valid() {
    if (sheet == null) {
      throw new WorkbookValidateException("set sheet first");
    }

    if (sheetMeta == null) {
      throw new WorkbookProcessException("set sheet meta first");
    }

    // check dependency of this sheet
    checkValidatorKeyDependency();

    validSheet(sheet);

    if (CollectionUtils.isNotEmpty(errorMessages)) {
      return false;
    }

    for (int i = sheetMeta.getDataStartRowIndex(); i <= sheet.sizeOfRows(); i++) {
      validRowCells(sheet.getRow(i));
    }

    return CollectionUtils.isEmpty(errorMessages);
  }

  /**
   * check if dependency correct
   */
  private void checkValidatorKeyDependency() {
    Map<String, List<DependencyValidator>> validatorMap = buildRelationValidatorMap();

    Map<String, Set<String>> vGraph = DependencyEngineHelper.buildVGraph(validatorMap);
    Set<String> allGroups = vGraph.keySet();
    for (Set<String> dependsOn : vGraph.values()) {
      if (!CollectionUtils.subtract(dependsOn, allGroups).isEmpty()) {
        throw new WorkbookValidateException("depends on missing group.");
      }
    }

    DependencyCycleCheckEngine dependencyCycleCheckEngine = new DependencyCycleCheckEngine(validatorMap);
    if (dependencyCycleCheckEngine.cycling()) {
      throw new WorkbookValidateException("dependency cycling.");
    }
  }

  /*=========================
   below is internal valid
   ==========================*/
  private void validSheet(Sheet sheet) {
    for (SheetValidator validator : sheetValidators) {

      if (!validator.valid(sheet, sheetMeta)) {

        String errorMessage = validator.getErrorMessage();
        this.errorMessages.add(new ErrorMessageBean(MessageWriteStrategies.TEXT_BOX, errorMessage, sheet.getIndex()));
      }
    }
  }

  private void validRowCells(Row row) {
    Map<String, List<DependencyValidator>> validatorMap = buildRelationValidatorMap();

    DependencyValidateEngine dependencyValidateEngine = new DependencyValidateEngine(validatorMap, sheetMeta, row);

    dependencyValidateEngine.valid();
    this.errorMessages.addAll(dependencyValidateEngine.getErrorMessages());
  }

  private Map<String, List<DependencyValidator>> buildRelationValidatorMap() {
    // one key corresponding multi validators, row validators first
    Map<String, List<DependencyValidator>> validatorMap = new LinkedHashMap<>();

    for (Map.Entry<String, List<RowValidator>> entry : group2rowValidators.entrySet()) {
      String group = entry.getKey();
      if (!validatorMap.containsKey(group)) {
        validatorMap.put(group, new ArrayList<DependencyValidator>());
      }
      validatorMap.get(group).addAll(entry.getValue());
    }

    for (Map.Entry<String, List<CellValidator>> entry : group2cellValidators.entrySet()) {
      String group = entry.getKey();
      if (!validatorMap.containsKey(group)) {
        validatorMap.put(group, new ArrayList<DependencyValidator>());
      }
      validatorMap.get(group).addAll(entry.getValue());
    }

    return validatorMap;
  }
}
