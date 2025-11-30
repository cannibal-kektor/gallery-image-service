package kektor.innowise.gallery.image.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ImageValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {

    String message() default "Invalid image. Supported types: jpg, jpeg, png";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}