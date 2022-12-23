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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.nanoboot.octagon.core.exceptions.OctagonException;
import org.nanoboot.octagon.entity.api.Repository;
import org.nanoboot.octagon.entity.classes.EntityLabel;
import org.nanoboot.octagon.plugin.api.core.WebAppBase;
import org.nanoboot.octagon.plugin.development.classes.*;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.devops.misc.Ticket;
import org.nanoboot.octagon.plugin.devops.misc.TicketRepository;
import org.nanoboot.octagon.plugin.devops.misc.TicketRepositoryFullImpl;
import org.nanoboot.octagon.plugin.devops.misc.TicketRepositorySimpleImpl;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.time.duration.Period;
import org.nanoboot.powerframework.time.moment.UniversalDateTime;
import org.nanoboot.powerframework.time.utils.TimeUnit;
import org.nanoboot.powerframework.web.html.WebElement;
import org.nanoboot.powerframework.web.html.tags.A;
import org.nanoboot.powerframework.web.html.tags.Br;
import org.nanoboot.powerframework.web.html.tags.Div;
import org.nanoboot.powerframework.web.html.tags.Form;
import org.nanoboot.powerframework.web.html.tags.FormMethod;
import org.nanoboot.powerframework.web.html.tags.H2;
import org.nanoboot.powerframework.web.html.tags.H3;
import org.nanoboot.powerframework.web.html.tags.HiddenInput;
import org.nanoboot.powerframework.web.html.tags.Hr;
import org.nanoboot.powerframework.web.html.tags.Label;
import org.nanoboot.powerframework.web.html.tags.Li;
import org.nanoboot.powerframework.web.html.tags.Option;
import org.nanoboot.powerframework.web.html.tags.Select;
import org.nanoboot.powerframework.web.html.tags.Span;
import org.nanoboot.powerframework.web.html.tags.SubmitInput;
import org.nanoboot.powerframework.web.html.tags.Table;
import org.nanoboot.powerframework.web.html.tags.Td;
import org.nanoboot.powerframework.web.html.tags.Th;
import org.nanoboot.powerframework.web.html.tags.Tr;
import org.nanoboot.powerframework.web.html.tags.Ul;
import org.nanoboot.powerframework.xml.Attribute;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public abstract class AbstractKanbanBoard extends WebAppBase {

    @Override
    public final String toHtml() {
        String projectId = this.httpServletRequest.getParameter("projectId");
        String userKanbanBoardName = this.httpServletRequest.getParameter("userKanbanBoardName");
        String assigneeId = this.httpServletRequest.getParameter("assigneeId");
        String productId = this.httpServletRequest.getParameter("productId");
        String statusChange_newStatus = this.httpServletRequest.getParameter("statusChange_newStatus");
        String statusChange_ticket = this.httpServletRequest.getParameter("statusChange_ticket");
        String statusChange_newResolution = this.httpServletRequest.getParameter("statusChange_newResolution");
        String statusChange_newResolutionComment = this.httpServletRequest.getParameter("statusChange_newResolutionComment");
        String newSortkey = this.httpServletRequest.getParameter("new_sortkey");
        String sortkeyTicket = this.httpServletRequest.getParameter("sortkey_ticket");
        String moveToKanbanBoard =this.httpServletRequest.getParameter("moveToKanbanBoard");
        String remove =this.httpServletRequest.getParameter("remove");
        String clazzScope =this.httpServletRequest.getParameter("clazzScope");
        ClassScope classScope = clazzScope == null ? ClassScope.ALL : ClassScope.valueOf(clazzScope.toUpperCase());
        
        //
        TicketRepository ticketRepository = isFull() ? new TicketRepositoryFullImpl(repositoryRegistry) : new TicketRepositorySimpleImpl(repositoryRegistry);
        //
        if (statusChange_newStatus != null && !statusChange_newStatus.isBlank()) {
            String[] array = statusChange_ticket.split(":");
            String clazzSimpleName = array[0];
            String ticketId = array[1];
            if(isFull()) {
                ticketRepository.changeStatus(clazzSimpleName, ticketId, statusChange_newStatus, statusChange_newResolution, statusChange_newResolutionComment);
            } else {
                ticketRepository.changeStatus(userKanbanBoardName, assigneeId, clazzSimpleName, ticketId, statusChange_newStatus, statusChange_newResolution, statusChange_newResolutionComment);
            }
            
        }
        if (sortkeyTicket != null) {
            System.out.println("Going to change sort key.");
            String[] array = sortkeyTicket.split(":");
            String clazzSimpleName = array[0];
            String ticketId = array[1];
            if(isFull()) {
            ticketRepository.changeSortkey(clazzSimpleName, ticketId, newSortkey);
            } else {
            ticketRepository.changeSortkey(userKanbanBoardName, assigneeId, clazzSimpleName, ticketId, newSortkey);    
            }
        } else {
            System.out.println("Going to change sort key cancelled, nothing to do.");
        }
        //
        Repository<Project> projectRepository = repositoryRegistry.find(Project.class.getSimpleName());
        Repository<UserKanbanBoard> userKanbanBoardRepository = repositoryRegistry.find(UserKanbanBoard.class.getSimpleName());
        Repository<User> userRepository = repositoryRegistry.find(User.class.getSimpleName());
        Repository<Product> productRepository = repositoryRegistry.find(Product.class.getSimpleName());
        if (projectId != null && !projectId.isBlank()) {
            Project projectTmp = projectRepository.read(projectId);
            if (projectTmp == null) {
                throw new OctagonException("There is no project with id " + projectId);
            }
        }
        //
        Div div = new Div();
        div.add(new WebElement("style", """
                                       td, th, tr {
                                           border: 0px solid black;
                                           padding: 5px;
                                       }
                                       tr:hover {
                                           background-color: inherit;
                                       }
                                       
                                       .devops-ticket-card:hover{
                                       border: 2px solid blue;
                                       }
                                        /* Forms */
                                        /* Style inputs, select elements and textareas */
                                        input[type=text], select, textarea {
                                            box-sizing: inherit;
                                            resize: inherit;
                                        }
                                       """));
        H2 h2 = new H2(this.getAppName());
        div.add(h2);

        Form form = new Form("webapp", FormMethod.GET);
        div.add(form);
        if (getKanbanBoardType() == KanbanBoardType.FULL) {
            Label projectLabel = new Label(Project.class.getSimpleName() + ":", "projectId");
            form.add(projectLabel);

            final List<Project> projects = projectRepository
                    .list("""
                                      id in (
                                      select distinct project_id from epic where assignee is not null union
                                      select distinct project_id from story where assignee is not null union
                                      select distinct project_id from dev_task where assignee is not null union
                                      select distinct project_id from dev_sub_task where assignee is not null union
                                      select distinct project_id from bug where assignee is not null union
                                      select distinct project_id from problem where assignee is not null union
                                      select distinct project_id from incident where assignee is not null union
                                      select distinct project_id from proposal where assignee is not null union
                                      select distinct project_id from new_feature where assignee is not null union
                                      select distinct project_id from enhancement where assignee is not null
                                      )
                                      """);

            List<EntityLabel> projectEntityLabels = new ArrayList<>();
            for (Project project : projects) {
                projectEntityLabels.add(new EntityLabel(project.getId().toString(), project.getName()));
            }
            EntitySelect projectIdSelect = new EntitySelect("projectId", projectId, projectEntityLabels);
            form.add(projectIdSelect);
        } else {
            Label userKanbanBoardLabel = new Label("Kanban board" + ":", "userKanbanBoardName");
            form.add(userKanbanBoardLabel);

            final List<UserKanbanBoard> kanbanBoards = userKanbanBoardRepository
                    .list(assigneeId == null || assigneeId.isBlank() ? " 1 = 1 " : (" user_id='" + assigneeId + "'"));
            List<EntityLabel> userKanbanBoardEntityLabels = new ArrayList<>();
            Set<String> kanbanBoardNameSet = new HashSet<>();
            for (UserKanbanBoard kb : kanbanBoards) {
                if (kanbanBoardNameSet.contains(kb.getName())) {
                    continue;
                }
                userKanbanBoardEntityLabels.add(new EntityLabel(kb.getName(), kb.getName()));
                kanbanBoardNameSet.add(kb.getName());
            }
            EntitySelect kanbanBoardSelect = new EntitySelect("userKanbanBoardName", userKanbanBoardName, userKanbanBoardEntityLabels);
            form.add(kanbanBoardSelect);
        }
        //
        Label assigneeLabel = new Label("Assignee:", "assigneeId");
        form.add(assigneeLabel);
        List<EntityLabel> assigneeEntityLabels = new ArrayList<>();
        for (User user : userRepository
                .list(/*"""
                      id in (
                      select distinct assignee from epic union
                      select distinct assignee from story union
                      select distinct assignee from dev_task union
                      select distinct assignee from dev_sub_task union
                      select distinct assignee from bug union
                      select distinct assignee from problem union
                      select distinct assignee from incident union
                      select distinct assignee from proposal union
                      select distinct assignee from new_feature union
                      select distinct assignee from enhancement
                      )
                      """*/)) {
            assigneeEntityLabels.add(new EntityLabel(user.getId().toString(), user.getName() + " " + user.getSurname()));
        }
        EntitySelect assigneeIdSelect = new EntitySelect("assigneeId", assigneeId, assigneeEntityLabels);

        form.add(assigneeIdSelect);
        ////
        //
        if (getKanbanBoardType() == KanbanBoardType.FULL) {
            Label productLabel = new Label("Product:", "productId");
            form.add(productLabel);
            List<EntityLabel> productEntityLabels = new ArrayList<>();
            for (Product product : productRepository
                    .list(/*"""
                      id in (
                      select distinct product_id from epic wunion
                      select distinct product_id from story union
                      select distinct product_id from dev_task union
                      select distinct product_id from dev_sub_task union
                      select distinct product_id from bug union
                      select distinct product_id from problem union
                      select distinct product_id from incident union
                      select distinct product_id from proposal union
                      select distinct product_id from new_feature union
                      select distinct product_id from enhancement
                      )
                      """*/)) {
                productEntityLabels.add(new EntityLabel(product.getId().toString(), product.getName()));
            }
            EntitySelect productIdSelect = new EntitySelect("productId", productId, productEntityLabels);
            form.add(productIdSelect);
        }
        //
        if (getKanbanBoardType() == KanbanBoardType.FULL) {
            Label clazzScopeLabel = new Label("Class scope:", "clazzScope");
            form.add(clazzScopeLabel);
            Select clazzScopeSelect = new Select().withNameAndId("clazzScope");
            clazzScopeSelect.getAttributes().setAllowAnyAttribute(true);
            clazzScopeSelect.add(new Attribute("style","width:200px"));
            Option all = new Option("all").withLabel("all").withValue("all");
            Option storiesAndEpics = new Option("stories / epics").withLabel("stories / epics").withValue("stories_epics");
            Option nonStoriesAndEpics = new Option("non stories / epics").withLabel("non stories / epics").withValue("non_stories_epics");
            switch(classScope) {
                case ALL : all.withSelected();break;
                case STORIES_EPICS : storiesAndEpics.withSelected();break;
                case NON_STORIES_EPICS : nonStoriesAndEpics.withSelected();break;
                default : throw new OctagonException("Unknown ClassScope " + classScope.name());
            }
            clazzScopeSelect.add(all, storiesAndEpics, nonStoriesAndEpics);
            form.add(clazzScopeSelect);
        }
        //
        form.add(new Br());
        form.add(new SubmitInput("View"));
        form.add(new HiddenInput("webAppClassSimpleName", getAppName()));
        div.add(new Hr());
        boolean nothingToView;
        if (getKanbanBoardType() == KanbanBoardType.FULL) {
            nothingToView = projectId == null || projectId.isBlank();
        } else {
            nothingToView = userKanbanBoardName == null || userKanbanBoardName.isBlank();
        }
        if (nothingToView) {
            div.add(new Span((getKanbanBoardType() == KanbanBoardType.FULL ? "Project ID" : "Kanban board name") + " was not selected. Nothing to view."));
        } else {

            List<Ticket> tickets = ticketRepository.listTickets(isFull() ? projectId : userKanbanBoardName, assigneeId, productId);
            if(classScope == ClassScope.STORIES_EPICS) {
                List<Ticket> toRemove = new ArrayList<>();
                for(Ticket tt:tickets) {
                    if(!(tt.clazz.getSimpleName().equals("Story") || tt.clazz.getSimpleName().equals("Epic"))) {
                        toRemove.add(tt);
                    }
                }
                tickets.removeAll(toRemove);
            }
            if(classScope == ClassScope.NON_STORIES_EPICS) {
                List<Ticket> toRemove = new ArrayList<>();
                for(Ticket tt:tickets) {
                    if(tt.clazz.getSimpleName().equals("Story") || tt.clazz.getSimpleName().equals("Epic")) {
                        toRemove.add(tt);
                    }
                }
                tickets.removeAll(toRemove);
            }
            List<Ticket> allTickets = new ArrayList<>(tickets);
            System.out.println("%%%Found " + allTickets.size() + " tickets.");

            List<Ticket> unconfirmedTickets = new ArrayList<>();
            for (Ticket t : allTickets) {
                System.out.println("^^^" + t.toString());
                if (t.task.getStatus() == TaskStatus.UNCONFIRMED) {
                    unconfirmedTickets.add(t);
                }
            }

            tickets.removeAll(unconfirmedTickets);
            System.out.println("unconfirmedTickets.size()=" + unconfirmedTickets.size());

            List<Ticket> unassignedTickets = new ArrayList<>();
            for (Ticket t : allTickets) {
                if (t.task.getAssignee() == null) {
                    unassignedTickets.add(t);
                }
            }

            tickets.removeAll(unassignedTickets);
            System.out.println("unassignedTickets.size()=" + unassignedTickets.size());
            
            if(isFull()) {
            List<Ticket> closedOldTickets = new ArrayList<>();
            UniversalDateTime udtNow = UniversalDateTime.now();
            for (Ticket t : allTickets) {
                if (t.task.getStatus().isClosed()) {
                    UniversalDateTime udt = ticketRepository.getDateOfLastModification(t.clazz.getSimpleName(), t.task.getId().toString());
                    
                    if (udt != null && new Period(udt, udtNow).getDuration().toTotal(TimeUnit.DAY) > 90) {
                        closedOldTickets.add(t);
                    }
                }
            }

            tickets.removeAll(closedOldTickets);
            System.out.println("closedOldTickets.size()=" + closedOldTickets.size());
            }
            
            createUlList("Backlog", unconfirmedTickets, div);
            if (assigneeId == null || assigneeId.isBlank()) {
                List<Ticket> aList = new ArrayList<>();
                for(Ticket ttt:unassignedTickets) {
                    if(ttt.task.getStatus().isClosed()) {
                        continue;
                    }
                    aList.add(ttt);
                }
                createUlList("Unassigned", aList, div);
            }
            div.add(new Hr());
//            if (isSimple()) {
//                TaskStatus[] statuses = TaskStatus.values();
//                for (int i = 0; i < 30; i++) {
//                    SimpleTask task = new SimpleTask(GitlabRepo.class);
//                    task.setId(UUID.randomUUID());
//
//                    task.setStatus(statuses[(int) (Math.random() * statuses.length - 1)]);
//                    task.setAssignee(UUID.randomUUID());
//                    task.setName("A super task");
//                    User user = new User();
//                    user.setId(UUID.randomUUID());
//                    user.setName("Tonda");
//                    user.setSurname("Fousek");
//                    tickets.add(new Ticket(GitlabRepo.class, task, null, user, i));
//                }
//                System.out.println("tickets.size=" + tickets.size());
//            }

            Map<String, List<Ticket>> ticketsByAssignee = new HashMap<>();
            for (Ticket t : tickets) {
                String id = t.getAssignee().getId().toString();
                if (!ticketsByAssignee.containsKey(id)) {
                    ticketsByAssignee.put(id, new ArrayList<Ticket>());
                }
                ticketsByAssignee.get(id).add(t);
            }
            for (String key : ticketsByAssignee.keySet()) {
                List<Ticket> tmpListOfTickets = ticketsByAssignee.get(key);
                div.add(new H3(tmpListOfTickets.get(0).assignee.getName() + " " + tmpListOfTickets.get(0).assignee.getSurname() + " "));
                if(isFull()) {
                div.add(new WebElement("p","<i>Note: closed tickets older than 3 months are not shown.</i>"));
                }
                createKanbanBoardTable(tmpListOfTickets, div);
            }
        }
        final String asHtml = div.build();

        return asHtml;
    }

    private void createUlList(String header, List<Ticket> tickets, Div div) {
        Collections.sort(tickets);
        div.add(new H3(header));
        Ul ul = new Ul();
        for (Ticket ticket : tickets) {
            String ticketLinkInnerText = ticket.task.getAlias() + " : " + ticket.task.getName();
            A ticketLink = new A("read?className=" + ticket.clazz.getSimpleName() + "&id=" + ticket.task.getId(), ticketLinkInnerText);
            ticketLink.setInnerText(ticketLinkInnerText);
            //
            A moveToKanbanBoardLink = null;
            A removeLink = null;
            if(isSimple()) {
            moveToKanbanBoardLink = new A("webapp?webAppClassSimpleName=MoveSimpleTicketToKanbanBoard&"
                    + "userKanbanBoardName=" + ticket.userKanbanBoardName + "&"
                    + "userId=" + ticket.assignee.getId().toString() + "&"
                    + "clazz=" + ticket.clazz.getSimpleName() + "&"
                    + "id=" + ticket.task.getId().toString()
                    , "[Move to kanban board]");
        
            moveToKanbanBoardLink.setInnerText("[Move to kanban board]");
            //
            removeLink = new A("webapp?webAppClassSimpleName=RemoveSimpleTicket&"
                    + "userKanbanBoardName=" + ticket.userKanbanBoardName + "&"
                    + "userId=" + ticket.assignee.getId().toString() + "&"
                    + "clazz=" + ticket.clazz.getSimpleName() + "&"
                    + "id=" + ticket.task.getId().toString()
                    , "[Move to kanban board]");
        
            removeLink.setInnerText("[Remove]");
            }
            //

            StringBuilder sb = new StringBuilder();

            sb.append(": ").append(ticket.getClazz().getSimpleName()).append(" ");
            if (ticket.product != null) {
                sb.append(ticket.product.getName()).append(" ");
            }
            sb.append(ticket.assignee.getNick()).append(" ");
            sb.append(ticket.task.getStatus()).append(" ");

            Li li = new Li(ticketLink.toString() + " " + (isSimple() ? (moveToKanbanBoardLink.toString() + " " + removeLink.toString()) : "") + " " + sb.toString());
            ul.add(li);
        }
        div.add(ul);
    }

    private void createKanbanBoardTable(List<Ticket> tickets, Div div) {
        System.out.println("Going to create a kanban board with " + tickets.size() + " tickets.");
        Set<TaskStatus> statusesInTicketList = new HashSet<>();
        for (Ticket t : tickets) {
            statusesInTicketList.add(t.getTask().getStatus());
        }
        List<TaskStatus> statusesToBeUsed = new ArrayList<>();
        for (TaskStatus ts : TaskStatus.values()) {
            if (statusesInTicketList.contains(ts)) {
                statusesToBeUsed.add(ts);
            }
        }
        Map<TaskStatus, List<Ticket>> ticketsByStatus = new HashMap<>();
        for (Ticket t : tickets) {
            if (!ticketsByStatus.containsKey(t.task.getStatus())) {
                ticketsByStatus.put(t.task.getStatus(), new ArrayList<Ticket>());
            }
            ticketsByStatus.get(t.task.getStatus()).add(t);
        }
        Table table = new Table();
        table.getAttributes().setAllowAnyAttribute(true);
        table.add(new Attribute("style", "background:silver;border:5px solid white;border-spacing: 0;"));
        Tr headerTr = new Tr();
        table.add(headerTr);
        for (TaskStatus ts : statusesToBeUsed) {
            Th th = new Th(ts.name());
            th.getAttributes().setAllowAnyAttribute(true);
            th.add(new Attribute("style", "width:250px;border:5px solid white;"));
            headerTr.add(th);
        }
        Tr cardsTr = new Tr();
        table.add(cardsTr);
        for (TaskStatus ts : statusesToBeUsed) {
            Td td = new Td(createCardsColumn(ticketsByStatus.get(ts)));
            td.getAttributes().setAllowAnyAttribute(true);
            td.add(new Attribute("style", "border:5px solid white;vertical-align: top;"));
            cardsTr.add(td);
        }
        div.add(table);
    }

    private String createCardsColumn(List<Ticket> tickets) {
        Table table = new Table();
        table.getAttributes().setAllowAnyAttribute(true);
        table.getAttributes().add("style", "border:0px solid white;border-spacing: 0;");

        Collections.sort(tickets);
        for (Ticket t : tickets) {
            Tr tr = new Tr();
            table.add(tr);
            Td td = new Td(createCard(t));
            td.getAttributes().setAllowAnyAttribute(true);
            td.getAttributes().add("style", "border:0px solid blue");
            td.getAttributes().add("class", "devops-ticket-card");
            tr.add(td);
        }
        return table.toString();
    }

    private String createCard(Ticket t) {
        return new Card(t, getAppName()).toHtml();
    }

    public boolean isFull() {
        return getKanbanBoardType() == KanbanBoardType.FULL;
    }

    public boolean isSimple() {
        return getKanbanBoardType() == KanbanBoardType.SIMPLE;
    }

    public abstract KanbanBoardType getKanbanBoardType();

    public abstract String getAppName();
}
