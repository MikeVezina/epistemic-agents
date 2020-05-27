package utils.converters;

import org.junit.jupiter.params.converter.ConvertWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Annotation that converts the test parameter input to a Literal object using {@link jason.asSyntax.ASSyntax#parseLiteral(String)}.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(LiteralConverter.class)
public @interface LiteralArg {
}
