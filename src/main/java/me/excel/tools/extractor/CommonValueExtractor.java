package me.excel.tools.extractor;

import java.util.function.Function;

/**
 * Created by hanwen on 5/3/16.
 */
public class CommonValueExtractor<D> extends AbstractCellValueExtractor {

  protected Function<D, String> stringValueGetter;

  public CommonValueExtractor(String matchField, Function<D, String> stringValueGetter) {
    super(matchField);
    this.stringValueGetter = stringValueGetter;
  }

  @Override
  public String getStringValue(Object data, String field) {
    if (stringValueGetter == null) {
      return null;
    }
    return stringValueGetter.apply((D) data);
  }
}