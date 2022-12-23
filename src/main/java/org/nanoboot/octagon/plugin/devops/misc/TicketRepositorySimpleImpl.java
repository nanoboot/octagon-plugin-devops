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

package org.nanoboot.octagon.plugin.devops.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nanoboot.octagon.core.exceptions.OctagonException;
import org.nanoboot.octagon.core.utils.RegistryImpl;
import org.nanoboot.octagon.entity.api.Repository;
import org.nanoboot.octagon.entity.core.ActionType;
import org.nanoboot.octagon.plugin.actionlog.api.ActionLogRepository;

import org.nanoboot.octagon.plugin.actionlog.classes.ActionLog;
import org.nanoboot.octagon.plugin.development.classes.Product;
import org.nanoboot.octagon.plugin.development.classes.SimpleTask;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.TaskResolution;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.json.JsonArray;
import org.nanoboot.powerframework.json.JsonObject;
import org.nanoboot.powerframework.time.moment.UniversalDateTime;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class TicketRepositorySimpleImpl implements TicketRepository {

    private final RegistryImpl<Repository> repositoryRegistry;

    private final Map<String, User> userCache = new HashMap<>();
    private final Repository<UserKanbanBoard> userKanbanBoardRepository;
    private final Repository<User> userRepository;
    private final Repository<ActionLog> actionLogRepository;

    public TicketRepositorySimpleImpl(RegistryImpl<Repository> repositoryRegistry) {
        this.repositoryRegistry = repositoryRegistry;
        this.userKanbanBoardRepository = repositoryRegistry.find(UserKanbanBoard.class.getSimpleName());
        this.userRepository = repositoryRegistry.find(User.class.getSimpleName());
        this.actionLogRepository = repositoryRegistry.find(ActionLog.class.getSimpleName());
    }

    @Override
    public List<Ticket> listTickets(String projectId, String assigneeUserId, String productIdIn, String onlyForStatus) {
        if (assigneeUserId != null && assigneeUserId.isBlank()) {
            assigneeUserId = null;
        }

        String kanbanBoardName = projectId;
        List<UserKanbanBoard> userKanbanBoards = new ArrayList<>();

        String ukbWherePart = " name = '" + kanbanBoardName + "' " + (assigneeUserId == null ? "" : (" and user_id='" + assigneeUserId + "' "));

        List<SimpleTicket> simpleTickets = new ArrayList<>();
        for (UserKanbanBoard ukb : userKanbanBoardRepository.list(ukbWherePart)) {
            simpleTickets.addAll(ukb.listTickets());
        }

        List<Ticket> tickets = new ArrayList<>();
        for (SimpleTicket st : simpleTickets) {

            Class clazz;
            try {
                clazz = Class.forName(st.clazz);
            } catch (ClassNotFoundException ex) {
                throw new OctagonException(ex);
            }
            SimpleTask simpleTask = new SimpleTask(st.getClass());

//            this.name = e.getString("name");
//            this.sortkey = e.getInt("sortkey");
//            this.status = TaskStatus.valueOf(e.getString("status"));
//            this.resolution = e.getString("resolution").isEmpty() ? null : TaskResolution.valueOf(e.getString("resolution"));
//            this.resolutionComment = e.getString("resolutionComment");
//            
            User user = findAssignee(assigneeUserId);
            simpleTask.setId(UUID.fromString(st.id));
            simpleTask.setName(st.name);
            simpleTask.setSortkey(st.sortkey);
            simpleTask.setStatus(st.status);
            simpleTask.setResolution(st.resolution);
            simpleTask.setResolutionComment(st.resolutionComment);
            simpleTask.setAssignee(user.getId());

            Ticket ticket = new Ticket(clazz, simpleTask, null, user, kanbanBoardName);
            tickets.add(ticket);
        }
        return tickets;

//        
//            for (AbstractDevelopmentTask adt : r.list(sqlWherePart)) {
//                final String productId = adt.getProductId() == null ? null : adt.getProductId().toString();
//                if (productIdIn != null && !productIdIn.isBlank() && !productIdIn.equals(productId)) {
//                    System.out.println("productIdIn=" + productIdIn + " productId=" + productId);
//                    continue;
//                }
//                Product product = findProduct(productId);
//                //
//                final String userId = adt.getAssignee() == null ? null : adt.getAssignee().toString();
//                User assignee = findAssignee(userId);
//
//                //
//                Ticket ticket = new Ticket(r.getClassOfType(), adt, product, assignee, 0);
//                tickets.add(ticket);
//
//            }
    }

    @Override
    public Product findProduct(String productId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User findAssignee(String userId) {
        if (userId == null || userId.isBlank()) {
            User user = new User();
            user.setId(null);
            user.setNick("no_one_yet");
            user.setName("No");
            user.setSurname("one yet");
            return user;
        }
        //
        if (!userCache.containsKey(userId)) {
            userCache.put(userId, userRepository.read(userId));
        }
        User user = userCache.get(userId);
        return user;
    }

    @Override
    public void changeStatus(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment) {
        UserKanbanBoard ukb = loadUkb(userKanbanBoardName, assingeeUserId);
        JsonObject jo = new JsonObject(ukb.getData());
        JsonArray ja = jo.getArray("tickets");
        JsonObject wantedTicket = null;
        for (int i = 0; i < ja.size(); i++) {
            JsonObject ticketAsJsonObject = ja.getObject(i);
            String joClazz = ticketAsJsonObject.getString("clazz");
            String joId = ticketAsJsonObject.getString("id");
            if (joClazz.endsWith("." + classSimpleName) && id.equals(joId)) {
                wantedTicket = ticketAsJsonObject;
                break;
            }
        }
        if (wantedTicket == null) {
            throw new OctagonException("Kanban boarad " + userKanbanBoardName + ":" + assingeeUserId + " has no ticket with clazz ending with ." + classSimpleName + " and id " + id);
        }

        wantedTicket.update("status", newStatus);
        
        if (TaskStatus.valueOf(newStatus).isClosed()) {
            wantedTicket.update("resolution", newResolution);
            wantedTicket.update("resolutionComment", newResolutionComment);
        } else {
            wantedTicket.update("resolution", null);
            wantedTicket.update("resolutionComment", null);
        }

        ukb.setData(jo.toString());
        UserKanbanBoard ukbBefore = userKanbanBoardRepository.read(ukb.getId());
        userKanbanBoardRepository.update(ukb);
        ((ActionLogRepository) actionLogRepository).persistActionLog(ActionType.UPDATE, ukbBefore, ukb, ukb, "A simple ticket was updated in a UserKanbanBoard.");

    }

    @Override
    public void changeSortkey(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newSortkey) {
        UserKanbanBoard ukb = loadUkb(userKanbanBoardName, assingeeUserId);
        JsonObject jo = new JsonObject(ukb.getData());
        JsonArray ja = jo.getArray("tickets");
        JsonObject wantedTicket = null;
        for (int i = 0; i < ja.size(); i++) {
            JsonObject ticketAsJsonObject = ja.getObject(i);
            String joClazz = ticketAsJsonObject.getString("clazz");
            String joId = ticketAsJsonObject.getString("id");
            if (joClazz.endsWith("." + classSimpleName) && id.equals(joId)) {
                wantedTicket = ticketAsJsonObject;
                break;
            }
        }
        if (wantedTicket == null) {
            throw new OctagonException("Kanban board " + userKanbanBoardName + ":" + assingeeUserId + " has no ticket with clazz ending with ." + classSimpleName + " and id " + id);
        }
        Integer intNewSortkey = newSortkey == null ? null : Integer.valueOf(newSortkey);
        wantedTicket.update("sortkey", intNewSortkey);

        ukb.setData(jo.toString());
        UserKanbanBoard ukbBefore = userKanbanBoardRepository.read(ukb.getId());
        userKanbanBoardRepository.update(ukb);
        ((ActionLogRepository) actionLogRepository).persistActionLog(ActionType.UPDATE, ukbBefore, ukb, ukb, "A simple ticket was updated in a UserKanbanBoard.");

    }

    private UserKanbanBoard loadUkb(String userKanbanBoardName, String assingeeUserId) throws OctagonException {
        List<UserKanbanBoard> ukbs = userKanbanBoardRepository.list(" name='" + userKanbanBoardName + "' and user_id='" + assingeeUserId + "' ");
        if (ukbs.size() > 2) {
            throw new OctagonException("ukbs.size() > 2");
        }
        if (ukbs.isEmpty()) {
            throw new OctagonException("There is no kanbanboard with name " + userKanbanBoardName + " and user id " + assingeeUserId + ".");
        }
        UserKanbanBoard ukb = ukbs.get(0);
        return ukb;
    }

    @Override
    public UniversalDateTime getDateOfLastModification(String type, String objectId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeSortkey(String classSimpleName, String id, String newSortkey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeStatus(String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
