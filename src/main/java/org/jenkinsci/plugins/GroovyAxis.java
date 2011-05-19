package org.jenkinsci.plugins;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.util.FormFieldValidator;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroovyAxis extends Axis {

    private String groovyString;

    @DataBoundConstructor
    public GroovyAxis(String name, String valueString) {
        super(name, evaluateGroovy(valueString));
        groovyString = valueString;
    }

    public String getGroovyString() {
        return groovyString;
    }

    static private List<String> evaluateGroovy(String groovyExpression) {
        GroovyShell shell = new GroovyShell();
        Object result = shell.evaluate(groovyExpression);

        List<String> values = Lists.newArrayList();

        if (result instanceof ArrayList<?>) {
            ArrayList<?> objects = (ArrayList<?>) result;

            for (Object object : objects) {
                if (object instanceof String) {
                    values.add((String) object);
                }
            }
        }

        if (values.isEmpty()) {
            values.add("default");
        }

        return values;
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {

        @Override
        public String getDisplayName() {
            return "GroovyAxis";
        }

        public FormValidation doTestGroovyScript(StaplerRequest req,
                                     StaplerResponse rsp,
        @QueryParameter("valueString") final String valueString) throws IOException, ServletException {
            return FormValidation.ok("[ " + Joiner.on(", ").join(GroovyAxis.evaluateGroovy(valueString)) + " ]");
        }

    }

}
