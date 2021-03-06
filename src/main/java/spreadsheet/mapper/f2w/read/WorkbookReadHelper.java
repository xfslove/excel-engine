package spreadsheet.mapper.f2w.read;

import spreadsheet.mapper.model.core.Workbook;

import java.io.InputStream;

/**
 * workbook read helper
 * <p>
 * Created by hanwen on 2016/12/30.
 */
public interface WorkbookReadHelper {

  /**
   * read supplied stream to {@link Workbook}
   *
   * @param inputStream auto close
   * @return {@link Workbook}
   */
  Workbook read(InputStream inputStream);
}
