///////////////////////////////////////////////////////////////////////////////////////////////
// Octagon Plugin DevOps: DevOps plugin for Octagon application.
// Copyright (C) 2021-2022 the original author or authors.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2
// of the License only.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////

package org.nanoboot.octagon.plugin.devops.webapps;

import org.nanoboot.octagon.plugin.development.classes.Product;
import org.nanoboot.octagon.plugin.devops.misc.Ticket;
import org.nanoboot.octagon.plugin.task.TaskResolution;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.web.html.WebElement;
import org.nanoboot.powerframework.web.html.tags.A;
import org.nanoboot.powerframework.web.html.tags.Div;
import org.nanoboot.powerframework.web.html.tags.Form;
import org.nanoboot.powerframework.web.html.tags.FormMethod;
import org.nanoboot.powerframework.web.html.tags.HiddenInput;
import org.nanoboot.powerframework.web.html.tags.Input;
import org.nanoboot.powerframework.web.html.tags.InputType;
import org.nanoboot.powerframework.web.html.tags.Option;
import org.nanoboot.powerframework.web.html.tags.Select;
import org.nanoboot.powerframework.web.html.tags.SubmitInput;
import org.nanoboot.powerframework.web.html.tags.Table;
import org.nanoboot.powerframework.web.html.tags.Td;
import org.nanoboot.powerframework.web.html.tags.Th;
import org.nanoboot.powerframework.web.html.tags.Tr;
import org.nanoboot.powerframework.xml.Attribute;


/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class Card extends Table {

    private final Ticket ticket;
    private String appName;

    public Card(Ticket t, String appName) {
        this.ticket = t;
        this.appName = appName;
        init();

    }

    private void init() {
        getAttributes().setAllowAnyAttribute(true);
        getAttributes().add("style", "border:2px solid gray;font-size:75%;width:250px;border-spacing:0;background:white;");
        getAttributes().add("onmouseover", "this.style.border='2px solid blue'");
        getAttributes().add("onmouseout", "this.style.border='2px solid gray'");
        add(createTypeRow(ticket));
        Tr productTr = createProductTr();
        if (productTr != null) {
            add(productTr);
        }
        //

        add(createMainTr());

        add(createSecondTr());

        add(createThirdTr());
    }

    public String toHtml() {
        return toString();
    }

    private Tr createTypeRow(Ticket t) {
        Tr tr0 = new Tr();
        ////
        String sortkeyString = t.getTask().getSortkey() == null ? "" : t.getTask().getSortkey().toString();
        Form form = new Form("webapp", FormMethod.GET);
        Input sortkeyInput = new Input(InputType.TEXT, "new_sortkey", sortkeyString);
//        if(getAppName().contains("Simple")) {
//            sortkeyInput.getAttributes().setAllowAnyAttribute(true);
//            sortkeyInput.add(new Attribute("readonly","readonly"));
//        }
        form.add(sortkeyInput);
        sortkeyInput.getAttributes().setAllowAnyAttribute(true);
        sortkeyInput.getAttributes().add("style", "width:25px;");
        //

        form.add(new HiddenInput("webAppClassSimpleName", getAppName()));
        form.add(new HiddenInput("sortkey_ticket", ticket.clazz.getSimpleName() + ":" + ticket.task.getId().toString()));
        if (!getAppName().contains("Simple")) {
            form.add(new HiddenInput("projectId", ticket.task.getProjectId().toString()));
            form.add(new HiddenInput("productId", ticket.task.getProductId().toString()));
        } else {
            form.add(new HiddenInput("userKanbanBoardName", ticket.userKanbanBoardName));
            form.add(new HiddenInput("assigneeId", ticket.assignee.getId().toString()));
        }
        form.add(new HiddenInput("assignee", ticket.assignee.getId().toString()));
        ////

        Div div = new Div();
        div.add(/*new Span(sortkeyString), */form);
        Td sortkeyTd = new Td(div);
        sortkeyTd.getAttributes().setAllowAnyAttribute(true);
        sortkeyTd.getAttributes().add("style", "text-align:left;color:gray;font-style:italic;");
        tr0.add(sortkeyTd);

        Td ticketType = new Td(t.clazz.getSimpleName().toUpperCase());
        tr0.add(ticketType);
        ticketType.getAttributes().setAllowAnyAttribute(true);
        ticketType.getAttributes().add("style", "text-align:right;color:gray;font-style:italic;");
        return tr0;
    }

    private Tr createProductTr() {
        if (this.ticket.getProduct() == null) {
            return null;
        }
        Tr productTr = new Tr();

        A a = new A("read?className=" + Product.class.getSimpleName() + "&id=" + ticket.product.getId(), "link");
        a.setInnerText("link");
        Th th = new Th(ticket.product.getName() + " " + a.toString());

        th.getAttributes().setAllowAnyAttribute(true);
        th.getAttributes().add("colspan", "2");
        th.getAttributes().add("style", "padding:4px;border:0; text-align:center; color: gray; font-style:italic;");
        productTr.add(th);
        return productTr;
    }

    private Tr createMainTr() {
        Tr mainTr = new Tr();

        A a = new A("read?className=" + ticket.clazz.getSimpleName() + "&id=" + ticket.task.getId(), ticket.task.getName());
        a.setInnerText(ticket.task.getName());
        Th th = new Th(a);
        th.getAttributes().add("style", "padding:4px;border:0;");
        th.getAttributes().setAllowAnyAttribute(true);
        th.getAttributes().add("colspan", "2");
        th.getAttributes().add("style", "text-align:left");
        mainTr.add(th);
        return mainTr;
    }

    private Tr createSecondTr() {
        Tr secondTr = new Tr();

        Td alias = new Td(getAppName().contains("Simple") ? "" : ticket.task.getAlias());
        Td assignee = new Td(ticket.assignee.getName() + " " + ticket.assignee.getSurname());
        assignee.getAttributes().setAllowAnyAttribute(true);
        secondTr.add(alias, assignee);
        WebElement[] cells = new WebElement[]{alias, assignee};
        for (WebElement c : cells) {
            c.getAttributes().setAllowAnyAttribute(true);
            if (c == cells[1]) {
                c.getAttributes().add("style", "padding:4px;border:0;text-align:right;");
            } else {
                c.getAttributes().add("style", "padding:4px;border:0;");
            }
        }
        return secondTr;
    }

    private Tr createThirdTr() {
        Tr thirdTr = new Tr();

        Form form = new Form("webapp", FormMethod.GET);
        Select statusChangeSelect = new Select().withNameAndId("statusChange_newStatus");
        Option projectIdSelectEmptyOption = new Option().withEmptyLabel();
        statusChangeSelect.add(projectIdSelectEmptyOption);
        for (TaskStatus ts : ticket.task.getStatus().getAllowedStatusTransitions()) {
            Option option = new Option(ts.name()).withValue(ts.name());
            statusChangeSelect.add(option);
        }

        statusChangeSelect.getAttributes().setAllowAnyAttribute(true);
        statusChangeSelect.add(new Attribute("style", "width:100px"));
        form.add(statusChangeSelect);
        //
        Select resolutionSelect = new Select().withNameAndId("statusChange_newResolution");
        Option resolutionSelectEmptyOption = new Option().withEmptyLabel();
        resolutionSelect.add(resolutionSelectEmptyOption);
        for (TaskResolution tr : TaskResolution.values()) {
            Option option = new Option(tr.name()).withValue(tr.name());
            resolutionSelect.add(option);
        }

        resolutionSelect.getAttributes().setAllowAnyAttribute(true);
        resolutionSelect.add(new Attribute("style", "width:100px"));
        form.add(resolutionSelect);
        //
        Input resolutionCommentInput = new Input(InputType.TEXT, "statusChange_newResolutionComment", "Done.");
        form.add(resolutionCommentInput);
        resolutionCommentInput.getAttributes().setAllowAnyAttribute(true);
        resolutionCommentInput.getAttributes().add("style", "width:100px;");
        //

        form.add(new SubmitInput("Change"));
        form.add(new HiddenInput("webAppClassSimpleName", getAppName()));
        form.add(new HiddenInput("statusChange_ticket", ticket.clazz.getSimpleName() + ":" + ticket.task.getId().toString()));

        if (!getAppName().contains("Simple")) {
            form.add(new HiddenInput("projectId", ticket.task.getProjectId().toString()));
        } else {
            form.add(new HiddenInput("userKanbanBoardName", ticket.userKanbanBoardName));
            form.add(new HiddenInput("assigneeId", ticket.assignee.getId().toString()));
        }

        form.add(new HiddenInput("assignee", ticket.assignee.getId().toString()));
        if (ticket.task.getProductId() != null) {
            form.add(new HiddenInput("productId", ticket.task.getProductId().toString()));
        }
        //
        if (getAppName().contains("Simple")) {
        A removeLink = new A("webapp?webAppClassSimpleName=RemoveSimpleTicket&"
                + "userKanbanBoardName=" + ticket.userKanbanBoardName + "&"
                + "userId=" + ticket.assignee.getId().toString() + "&"
                + "clazz=" + ticket.clazz.getSimpleName() + "&"
                + "id=" + ticket.task.getId().toString(),
                 "[Move to kanban board]");

        removeLink.setInnerText("[Remove]");
        form.getElements().setAllowAnyElement(true);
        form.add(removeLink);
        }
        
        //
        Td statusChange = new Td(form);
        statusChange.getAttributes().setAllowAnyAttribute(true);
        statusChange.getAttributes().add("colspan", "2");
        thirdTr.add(statusChange);
        return thirdTr;
    }

    private String getAppName() {
        return this.appName;
    }

}
