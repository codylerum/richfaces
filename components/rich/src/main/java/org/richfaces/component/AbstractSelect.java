package org.richfaces.component;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.Validator;

import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.cdk.annotations.Tag;
import org.richfaces.cdk.annotations.TagType;
import org.richfaces.component.attribute.AutocompleteProps;
import org.richfaces.component.attribute.CoreProps;
import org.richfaces.component.attribute.DisabledProps;
import org.richfaces.component.attribute.EventsKeyProps;
import org.richfaces.component.attribute.EventsMouseProps;
import org.richfaces.component.attribute.SelectItemsProps;
import org.richfaces.component.attribute.SelectProps;
import org.richfaces.component.util.SelectItemsInterface;
import org.richfaces.context.ExtendedVisitContext;
import org.richfaces.context.ExtendedVisitContextMode;
import org.richfaces.log.Logger;
import org.richfaces.log.RichfacesLogger;
import org.richfaces.renderkit.MetaComponentRenderer;
import org.richfaces.renderkit.SelectManyHelper;
import org.richfaces.validator.SelectLabelValueValidator;
import org.richfaces.view.facelets.AutocompleteHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * The &lt;rich:select&gt; component provides a drop-down list box for selecting a single value from multiple options. The
 * &lt;rich:select&gt; component can be configured as a combo-box, where it will accept typed input. The component also supports
 * keyboard navigation. The &lt;rich:select&gt; component functions similarly to the JSF UISelectOne component.
 * </p>
 *
 * @author abelevich
 * @author <a href="http://community.jboss.org/people/bleathem">Brian Leathem</a>
 */
@JsfComponent(
        tag = @Tag(name = "select", type = TagType.Facelets, handlerClass = AutocompleteHandler.class),
        type = AbstractSelect.COMPONENT_TYPE, family = AbstractSelect.COMPONENT_FAMILY,
        renderer = @JsfRenderer(type = "org.richfaces.SelectRenderer"))
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public abstract class AbstractSelect extends AbstractSelectComponent implements SelectItemsInterface, MetaComponentResolver, MetaComponentEncoder, CoreProps, DisabledProps, EventsKeyProps, EventsMouseProps, SelectProps, AutocompleteProps, SelectItemsProps {
    public static final String ITEMS_META_COMPONENT_ID = "items";
    public static final String COMPONENT_TYPE = "org.richfaces.Select";
    public static final String COMPONENT_FAMILY = "org.richfaces.Select";

    public Object getItemValues() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> requestParameters = facesContext.getExternalContext().getRequestParameterMap();
        String value = AutocompleteMode.lazyClient == getMode() ? "" : requestParameters.get(this.getClientId(facesContext) + "Input");
        return AbstractAutocomplete.getItems(facesContext, this, value);
    }

    /**
     * <p>
     * If "true" Allows the user to type into a text field to scroll through or filter the list
     * </p>
     * <p>
     * Default is "false"
     * </p>
     */
    @Attribute()
    public abstract boolean isEnableManualInput();

    /**
     * <p>
     * If "true" as the user types to narrow the list, automatically select the first element in the list. Applicable only when
     * enableManualInput is "true".
     * </p>
     * <p>
     * Default is "true"
     * </p>
     */
    @Attribute(defaultValue = "true")
    public abstract boolean isSelectFirst();

    /**
     * <p>
     * When "true" display a button to expand the popup list
     * </p>
     * <p>
     * Default is "true"
     * </p>
     */
    @Attribute(defaultValue = "true")
    public abstract boolean isShowButton();

    /**
     * The minimum height ot the list
     */
    @Attribute()
    public abstract String getMinListHeight();

    /**
     * The maximum height of the list
     */
    @Attribute()
    public abstract String getMaxListHeight();

    /**
     * A javascript function used to filter the list of items in the select popup
     */
    @Attribute
    public abstract String getClientFilterFunction();

    @Attribute(hidden = true)
    public abstract String getActiveClass();

    @Attribute(hidden = true)
    public abstract String getChangedClass();

    @Attribute(hidden = true)
    public abstract String getDisabledClass();


    /**
     * Override the validateValue method in cases where the component implements SelectItemsInterface
     *
     * @param facesContext
     * @param value
     */
    @Override
    protected void validateValue(FacesContext facesContext, Object value) {
        if (this instanceof SelectItemsInterface) {
            UISelectItems pseudoSelectItems = SelectManyHelper.getPseudoSelectItems((SelectItemsInterface) this);
            if (pseudoSelectItems != null) {
                this.getChildren().add(pseudoSelectItems);
                super.validateValue(facesContext, value);
                this.getChildren().remove(pseudoSelectItems);
                return;
            }
        }
        super.validateValue(facesContext, value);
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        super.processEvent(event);

        if (event instanceof PostAddToViewEvent) {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            EditableValueHolder component = (EditableValueHolder) event.getComponent();

            Validator validator = facesContext.getApplication().createValidator(SelectLabelValueValidator.ID);
            component.addValidator(validator);
        }
    }

    @Override
    public String resolveClientId(FacesContext facesContext, UIComponent contextComponent, String metaComponentId) {
        if (ITEMS_META_COMPONENT_ID.equals(metaComponentId)) {
            return getClientId(facesContext) + MetaComponentResolver.META_COMPONENT_SEPARATOR_CHAR + metaComponentId;
        }

        return null;
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        if (context instanceof ExtendedVisitContext) {
            ExtendedVisitContext extendedVisitContext = (ExtendedVisitContext) context;
            if (extendedVisitContext.getVisitMode() == ExtendedVisitContextMode.RENDER) {

                VisitResult result = extendedVisitContext.invokeMetaComponentVisitCallback(this, callback,
                        ITEMS_META_COMPONENT_ID);
                if (result == VisitResult.COMPLETE) {
                    return true;
                } else if (result == VisitResult.REJECT) {
                    return false;
                }
            }
        }

        return super.visitTree(context, callback);
    }

    @Override
    public void encodeMetaComponent(FacesContext context, String metaComponentId) throws IOException {
        ((MetaComponentRenderer) getRenderer(context)).encodeMetaComponent(context, this, metaComponentId);
    }

    @Override
    public String substituteUnresolvedClientId(FacesContext facesContext, UIComponent contextComponent, String metaComponentId) {
        return null;
    }
}
