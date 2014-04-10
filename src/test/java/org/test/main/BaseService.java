package org.test.main;

public abstract class BaseService {

  public String findAll(String orderBy) {
    if ("throw".equals(orderBy)) {
      throw new IllegalArgumentException("nah!");
    }
    return "foo";
  }

  public String delete() {
    return "bar";
  }
}
