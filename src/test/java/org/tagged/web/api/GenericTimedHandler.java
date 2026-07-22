package org.tagged.web.api;

public interface GenericTimedHandler<T> {

  T handle(T value);
}
