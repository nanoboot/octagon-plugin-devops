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

import java.util.List;
import org.nanoboot.octagon.core.exceptions.OctagonException;
import org.nanoboot.octagon.entity.api.Repository;
import org.nanoboot.octagon.plugin.api.core.WebAppBase;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.devops.misc.TicketRepository;
import org.nanoboot.octagon.plugin.devops.misc.TicketRepositorySimpleImpl;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.json.JsonArray;
import org.nanoboot.powerframework.json.JsonObject;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class MoveSimpleTicketToKanbanBoard extends WebAppBase {

    @Override
    public final String toHtml() {
        String userKanbanBoardName = this.httpServletRequest.getParameter("userKanbanBoardName");
        String userId = this.httpServletRequest.getParameter("userId");
        //
        String clazz = this.httpServletRequest.getParameter("clazz");
        String id = this.httpServletRequest.getParameter("id");

        Repository<UserKanbanBoard> userKanbanBoardRepository = repositoryRegistry.find(UserKanbanBoard.class.getSimpleName());
        Repository<User> userRepository = repositoryRegistry.find(User.class.getSimpleName());

        String[] mandatoryParameters = new String[]{"userKanbanBoardName", "userId", "clazz", "id"};
        for (String mp : mandatoryParameters) {
            String mpe = this.httpServletRequest.getParameter(mp);
            if (mpe == null || mpe.isBlank()) {
                throw new OctagonException("Mandatory parameter " + mp + " is missing.");
            }
        }

        List<UserKanbanBoard> ukbs = userKanbanBoardRepository.list(" name='" + userKanbanBoardName + "' and user_id='" + userId + "'");
        if (ukbs.isEmpty()) {
            throw new OctagonException("There is no UserKanbanBoard with name: " + userKanbanBoardName + " and user id: " + userId);
        }
        UserKanbanBoard ukb = ukbs.get(0);
        
        JsonObject jo = new JsonObject(ukb.getData());
        JsonArray ja = jo.getArray("tickets");
        JsonObject wantedTicket = null;
        for (int i = 0; i < ja.size(); i++) {
            JsonObject ticketAsJsonObject = ja.getObject(i);
            String joClazz = ticketAsJsonObject.getString("clazz");
            String joId = ticketAsJsonObject.getString("id");
            if (joClazz.endsWith("." + clazz) && id.equals(joId)) {
                wantedTicket = ticketAsJsonObject;
                break;
            }
        }
        if (wantedTicket == null) {
            throw new OctagonException("Kanban boarad " + userKanbanBoardName + ":" + userId + " has no ticket with clazz ending with ." + clazz + " and id " + id);
        } else {
            if(!wantedTicket.getString("status").equals("UNCONFIRMED")) {
                throw new OctagonException("Ticket is already in kanban board.");
            }
        }
        
        System.out.println("Current board is: " + new JsonObject(ukb.getData()).toPrettyString());

        TicketRepository ticketRepository = new TicketRepositorySimpleImpl(repositoryRegistry);
        ticketRepository.changeStatus(userKanbanBoardName, userId, clazz, id, TaskStatus.NEW.name(), null, null);

        return "Simple ticket was moved to kanban board.";
    }
}
