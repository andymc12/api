package jaxrs.examples.client.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.Link;
import javax.ws.rs.core.HttpResponse;

import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.Response;

public class ValidatorExample {

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = EmailValidator.class)
    public @interface Email {

        String message() default "{foo.bar.validation.constraints.email}";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class EmailValidator implements ConstraintValidator<Email, String> {

        @Override
        public void initialize(Email email) {
            // no-op
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // Ensure value is a valid e-mail address
            return true;
        }
    }

    public class EmailImpl extends AnnotationLiteral<Email> implements Email {

        private static final long serialVersionUID = -3177939101972190621L;

        @Override
        public String message() {
            return "{javax.validation.constraints.NotNull.message}";
        }

        @Override
        public Class<?>[] groups() {
            return new Class<?>[0];
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Payload>[] payload() {
            return (Class<? extends Payload>[]) new Class<?>[0];
        }
    }

    public void annotationBasedRequestResponseValidation() {
        Client c = ClientFactory.newClient();

        HttpResponse response = c.link("http://example.com/foo/").post().entity("marek.potociar@oracle.com", new EmailImpl()).invoke();

        String userId = response.annotateEntity(new NotNull(), new Pattern("[0-9]+")).getEntity(String.class);
        System.out.println("User id = " + userId);
    }

    public void annotationBasedRequestParameterValidation() {
        Client c = ClientFactory.newClient();

        final Link rootResource = c.link("http://example.com/foo");
        String userId = rootResource.get().queryParam("email", "marek.potociar@oracle.com", new EmailImpl()).invoke(String.class);

        // Path param validation using resource link:
        HttpResponse r1 = rootResource.path("{userId}").pathParam("userId", userId, new Pattern("[0-9]+")).get().invoke();
        assert r1.getStatus() == Response.Status.OK;
        
        // Path param validation using invocation:
        HttpResponse r2 = rootResource.path("{userId}").get().pathParam("userId", userId, new Pattern("[0-9]+")).invoke();
        assert r2.getStatus() == Response.Status.OK;
    }

    public void example2() {
        Client c = ClientFactory.newClient();
        /*
        String response = c.resourceUri("http://example.com/foo/")
        .put()
        .entity("foo@bar.com", EmailValidator.class)
        .invoke(String.class, EmailValidator.class);
         */
    }
}
