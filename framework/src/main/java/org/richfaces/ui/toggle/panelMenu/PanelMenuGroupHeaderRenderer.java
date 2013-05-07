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

package org.richfaces.ui.toggle.panelMenu;

import org.richfaces.ui.common.PanelIcons;
import org.richfaces.ui.common.TableIconsRendererHelper;
import org.richfaces.ui.common.PanelIcons.State;
import org.richfaces.util.HtmlUtil;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;

class PanelMenuGroupHeaderRenderer extends TableIconsRendererHelper<AbstractPanelMenuGroup> {
    PanelMenuGroupHeaderRenderer(String cssClassPrefix) {
        super("label", cssClassPrefix, "rf-pm-ico");
    }

    private PanelIcons.State getState(AbstractPanelMenuGroup group) {
        if (group.isTopItem()) {
            return PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? State.headerDisabled
                : State.header;
        } else {
            return PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? State.commonDisabled
                : State.common;
        }
    }

    protected void encodeHeaderLeftIcon(ResponseWriter writer, FacesContext context, AbstractPanelMenuGroup group)
        throws IOException {
        String iconCollapsed = PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? group
            .getLeftDisabledIcon() : group.getLeftCollapsedIcon();
        String iconExpanded = PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? group
            .getLeftDisabledIcon() : group.getLeftExpandedIcon();

        if (iconCollapsed == null || iconCollapsed.trim().length() == 0) {
            iconCollapsed = PanelIcons.transparent.toString();
        }

        if (iconExpanded == null || iconExpanded.trim().length() == 0) {
            iconExpanded = PanelIcons.transparent.toString();
        }

        encodeTdIcon(writer, context, HtmlUtil.concatClasses(cssClassPrefix + "-ico", group.getLeftIconClass()), iconCollapsed,
            iconExpanded, getState(group));
    }

    protected void encodeHeaderRightIcon(ResponseWriter writer, FacesContext context, AbstractPanelMenuGroup group)
        throws IOException {
        String iconCollapsed = PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? group
            .getRightDisabledIcon() : group.getRightCollapsedIcon();
        String iconExpanded = PanelMenuItemRenderer.isParentPanelMenuDisabled(group) || group.isDisabled() ? group
            .getRightDisabledIcon() : group.getRightExpandedIcon();

        if (iconCollapsed == null || iconCollapsed.trim().length() == 0) {
            iconCollapsed = PanelIcons.transparent.toString();
        }

        if (iconExpanded == null || iconExpanded.trim().length() == 0) {
            iconExpanded = PanelIcons.transparent.toString();
        }
        // TODO nick - should this be "-ico-exp"? also why expanded icon state is connected with right icon alignment?
        encodeTdIcon(writer, context, HtmlUtil.concatClasses(cssClassPrefix + "-exp-ico", group.getRightIconClass()),
            iconCollapsed, iconExpanded, getState(group));
    }
}
