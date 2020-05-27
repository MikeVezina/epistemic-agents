package utils.converters;

import epistemic.wrappers.NormalizedWrappedLiteral;
import org.junit.jupiter.params.converter.ConvertWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Annotation that converts the test parameter input to a WrappedLiteral object using {@link WrappedLiteralConverter}.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(NormalizedWrappedLiteralConverter.class)
public @interface NormWrappedLiteralArg {
}
