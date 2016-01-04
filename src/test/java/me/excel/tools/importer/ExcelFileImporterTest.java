package me.excel.tools.importer;

import me.excel.tools.factory.AbstractModelFactory;
import me.excel.tools.model.excel.ExcelCell;
import me.excel.tools.model.excel.ExcelRow;
import me.excel.tools.processor.DataProcessor;
import me.excel.tools.utils.AbstractFieldValueSetter;
import org.apache.poi.util.TempFile;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import static org.testng.Assert.assertEquals;

/**
 * Created by hanwen on 16-1-4.
 */
public class ExcelFileImporterTest {
  
  @Test
  public void testProcess() throws Exception {
    URL resource = this.getClass().getResource("test.xlsx");
    File excel = new File(resource.getFile());

    UserFileImporter userFileImporter = new ExcelFileImporter();
    userFileImporter.setModelFactory(new StudentModelFactoryTest(StudentTest.class));
    userFileImporter.addFieldValueSetter(new DateValueSetterTest("student.enrollDate", "yyyy-MM-dd",
    (s, date) -> {
      StudentTest student = (StudentTest) s;
      student.setEnrollDate(date);
    }));

    userFileImporter.process(excel, new StudentDataProcessorTest());
  }

  public class StudentDataProcessorTest implements DataProcessor {

    @Override
    public void handle(List models) {

      assertEquals(models.size(), 2);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      StudentTest model1 = (StudentTest) models.get(0);
      StudentTest model2 = (StudentTest) models.get(1);
      assertEquals(model1.getCode(), "111111");
      assertEquals(model2.getCode(), "2222");

      assertEquals(model1.getName(), "std1");
      assertEquals(model2.getName(), "std2");

      assertEquals(model1.getAge(), new Integer(18));
      assertEquals(model2.getAge(), new Integer(18));

      try {
        assertEquals(model1.getEnrollDate(), sdf.parse("2015-09-01"));
        assertEquals(model2.getEnrollDate(), sdf.parse("2015-09-01"));
      } catch (ParseException e) {
      }
    }
  }

  public class DateValueSetterTest extends AbstractFieldValueSetter {

    private String format;

    private BiConsumer<Object, Date> dateValueSetter;

    public DateValueSetterTest(String matchField, String format, BiConsumer<Object, Date> dateValueSetter) {
      super(matchField);
      this.format = format;
      this.dateValueSetter = dateValueSetter;
    }

    @Override
    public void set(Object data, ExcelCell excelCell) {

      SimpleDateFormat sdf = new SimpleDateFormat(format);
      try {
        dateValueSetter.accept(data, sdf.parse(excelCell.getValue()));
      } catch (ParseException e) {
      }
    }
  }

  public class StudentModelFactoryTest extends AbstractModelFactory {

    public StudentModelFactoryTest(Class modelClazz) {
      super(modelClazz);
    }

    @Override
    public Object create(ExcelRow row) {
      return new StudentTest();
    }
  }

  public class StudentTest {

    private String code;

    private String name;

    private Integer age;

    private Date enrollDate;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public Date getEnrollDate() {
      return enrollDate;
    }

    public void setEnrollDate(Date enrollDate) {
      this.enrollDate = enrollDate;
    }
  }
}