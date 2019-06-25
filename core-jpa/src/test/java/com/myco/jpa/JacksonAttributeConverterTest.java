package com.myco.jpa;

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class JacksonAttributeConverterTest {

  static class TestModel {
    private String id;
    private String foo;
    private String goo;

    @Override public String toString() {
      return "TestModel{" + "id='" + id + '\'' + ", foo='" + foo + '\'' + ", goo=" + goo + '}';
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TestModel)) return false;
      TestModel testModel = (TestModel) o;
      return Objects.equals(id, testModel.id);
    }

    @Override public int hashCode() {
      return Objects.hash(id);
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getFoo() {
      return foo;
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }

    public String getGoo() {
      return goo;
    }

    public void setGoo(String goo) {
      this.goo = goo;
    }
  }

  static class TestConverter extends JacksonAttributeConverter<TestModel> {
    TestConverter() {
      super(TestModel.class);
    }
  }

  @Test
  public void test() {

    TestConverter converter = new TestConverter();

    TestModel testModel = new TestModel();
    testModel.setId("id");
    testModel.setGoo("goo");
    testModel.setFoo("foo");

    String testModelString = converter.convertToDatabaseColumn(testModel);

    TestModel testModel2 = converter.convertToEntityAttribute(testModelString);

    Assert.assertEquals("Models not equal", testModel, testModel2);
  }
}
