/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.validator;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.RequiredValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.test.faces.mock.Environment;
import org.jboss.test.faces.mock.Environment.Feature;
import org.jboss.test.faces.mock.Mock;
import org.jboss.test.faces.mock.MockController;
import org.jboss.test.faces.mock.MockFacesEnvironment;
import org.jboss.test.faces.mock.MockTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MockTestRunner.class)
public class FacesValidatorServiceTest {
    @Mock()
    @Environment({ Feature.APPLICATION })
    protected MockFacesEnvironment environment;
    protected FacesValidatorService serviceImpl;
    @Mock
    protected UIViewRoot viewRoot;
    @Mock
    protected UIInput input;
    protected MockController controller;
    protected Validator validator;

    @Before
    public void setUp() {
        // create service impl.
        serviceImpl = new FacesValidatorServiceImpl();
        expect(environment.getFacesContext().getViewRoot()).andStubReturn(viewRoot);
        expect(viewRoot.getLocale()).andStubReturn(Locale.ENGLISH);
        expect(environment.getApplication().getMessageBundle()).andStubReturn("javax.faces.Messages");
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("label", "foo");
        expect(input.getAttributes()).andStubReturn(attributes);
        expect(input.getRequiredMessage()).andStubReturn(null);
    }

    @After
    public void tearDown() {
        controller.verify();
        serviceImpl = null;
        controller.release();
    }

    @Test
    public void getConverterClass() throws Exception {
        validator = new LengthValidator();
        controller.replay();
        ValidatorDescriptor validatorDescription = serviceImpl.getValidatorDescription(environment.getFacesContext(), input,
                validator, null);
        assertEquals(validator.getClass(), validatorDescription.getImplementationClass());
    }

    @Test
    public void getValidatorMessage() throws Exception {
        validator = new RequiredValidator();
        FacesMessage facesMessage = null;
        controller.replay();
        try {
            validator.validate(environment.getFacesContext(), input, null);
        } catch (ValidatorException e) {
            facesMessage = e.getFacesMessage();
        }
        assertNotNull(facesMessage);
        ValidatorDescriptor validatorDescription = serviceImpl.getValidatorDescription(environment.getFacesContext(), input,
                validator, null);
        String summary = validatorDescription.getMessage().getSummary();
        summary = summary.replace("{0}", "foo");
        assertEquals(facesMessage.getSummary(), summary);
    }

    @Test
    public void validatorWithWriteOnlyProperty() throws Exception {
        validator = new ValidatorWithWriteOnlyProperty();
        controller.replay();
        ((ValidatorWithWriteOnlyProperty) validator).setHandler(123);
        ((ValidatorWithWriteOnlyProperty) validator).setMin(255);
        final ValidatorDescriptor validatorDescription = serviceImpl.getValidatorDescription(environment.getFacesContext(),
                input, validator, null);
        assertFalse(validatorDescription.getAdditionalParameters().containsKey("handler"));
        assertTrue(validatorDescription.getAdditionalParameters().containsKey("min"));
        assertEquals(255, validatorDescription.getAdditionalParameters().get("min"));
    }

    public static class ValidatorWithWriteOnlyProperty implements Validator {

        private Object handler;

        private Integer min;

        public void setHandler(Object handler) {
            this.handler = handler;
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        @Override
        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        }
    }
}
