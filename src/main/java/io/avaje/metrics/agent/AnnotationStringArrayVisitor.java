package io.avaje.metrics.agent;

import io.avaje.metrics.agent.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;

import static io.avaje.metrics.agent.Transformer.ASM_VERSION;

final class AnnotationStringArrayVisitor extends AnnotationVisitor {

  private final List<String> values = new ArrayList<>();
  private final StringArrayConsumer consumer;

  AnnotationStringArrayVisitor(AnnotationVisitor av, StringArrayConsumer consumer) {
    super(ASM_VERSION, av);
    this.consumer = consumer;
  }

  @Override
  public void visit(String name, Object value) {
    if (value instanceof String) {
      values.add((String) value);
    }
    super.visit(name, value);
  }

  @Override
  public void visitEnd() {
    consumer.accept(values.toArray(new String[values.size()]));
    super.visitEnd();
  }

  interface StringArrayConsumer {
    void accept(String[] values);
  }
}
