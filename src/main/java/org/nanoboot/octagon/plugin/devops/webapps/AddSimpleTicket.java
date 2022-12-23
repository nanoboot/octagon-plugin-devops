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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.nanoboot.octagon.core.exceptions.OctagonException;
import org.nanoboot.octagon.entity.api.Repository;
import org.nanoboot.octagon.entity.classes.EntityLabel;
import org.nanoboot.octagon.entity.core.ActionType;
import org.nanoboot.octagon.entity.core.Entity;
import org.nanoboot.octagon.plugin.actionlog.api.ActionLogRepository;
import org.nanoboot.octagon.plugin.api.core.WebAppBase;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.devops.misc.SimpleTicket;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.TaskResolution;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.json.JsonObject;
import org.nanoboot.powerframework.web.html.WebElement;
import org.nanoboot.powerframework.web.html.tags.Br;
import org.nanoboot.powerframework.web.html.tags.Div;
import org.nanoboot.powerframework.web.html.tags.Form;
import org.nanoboot.powerframework.web.html.tags.FormMethod;
import org.nanoboot.powerframework.web.html.tags.HiddenInput;
import org.nanoboot.powerframework.web.html.tags.Input;
import org.nanoboot.powerframework.web.html.tags.InputType;
import org.nanoboot.powerframework.web.html.tags.Label;
import org.nanoboot.powerframework.web.html.tags.SubmitInput;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class AddSimpleTicket extends WebAppBase {

    @Override
    public final String toHtml() {
        String userKanbanBoardName = this.httpServletRequest.getParameter("userKanbanBoardName");
        String userId = this.httpServletRequest.getParameter("userId");
        //
        String clazz = this.httpServletRequest.getParameter("clazz");
        String id = this.httpServletRequest.getParameter("id");
        String sortkey = this.httpServletRequest.getParameter("sortkey");
        String status = this.httpServletRequest.getParameter("status");
        String resolution = this.httpServletRequest.getParameter("resolution");
        String resolutionComment = this.httpServletRequest.getParameter("resolutionComment");
        String process = this.httpServletRequest.getParameter("process");

        Repository<UserKanbanBoard> userKanbanBoardRepository = repositoryRegistry.find(UserKanbanBoard.class.getSimpleName());
        Repository<User> userRepository = repositoryRegistry.find(User.class.getSimpleName());

        if (process != null && process.equals("true")) {
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
            System.out.println("Current board is: " + new JsonObject(ukb.getData()).toPrettyString());
            Repository repo = repositoryRegistry.find(clazz);
            Entity entity = repo.read(id);
            SimpleTicket st = new SimpleTicket();
            st.clazz = repo.getClassOfType().getName();
            st.id = id;
            st.name = entity.getName();
            st.sortkey = sortkey == null || sortkey.isBlank() ? null : Integer.valueOf(sortkey);
            //
            st.status = status == null || status.isBlank() ? TaskStatus.UNCONFIRMED : TaskStatus.valueOf(status);
            st.resolution = resolution == null || resolution.isBlank() ? null : TaskResolution.valueOf(status);
            st.resolutionComment = resolutionComment;
            //
            JsonObject dataAsJson = new JsonObject(ukb.getData());
            dataAsJson.getArray("tickets").add(st.toJsonObject());
            ukb.setData(dataAsJson.toPrettyString());

            UserKanbanBoard ukbBefore = userKanbanBoardRepository.read(ukb.getId().toString());
            UserKanbanBoard ukbAfter = ukb;
            if (ukbAfter.getData().equals(ukbBefore.getData())) {
                //nothing to do
                return "Nothing was changed.";
            }

            userKanbanBoardRepository.update(ukbAfter);
            ((ActionLogRepository) actionLogRepository).persistActionLog(ActionType.UPDATE, ukbBefore, ukbAfter, ukbAfter, "A new simple ticket was added to a UserKanbanBoard.");

            return "A new Simple Ticket was created!";
        }
        Div div = new Div();
//        div.add(new WebElement("style","""
//                                   input {width:200px;});
//                                   """));
        //
        Form form = new Form("webapp", FormMethod.GET);

        {
            Label userKanbanBoardLabel = new Label("Kanban board" + ":", "userKanbanBoardName");
            form.add(userKanbanBoardLabel);

            final List<UserKanbanBoard> kanbanBoards = userKanbanBoardRepository
                    .list();
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
        form.add(new Br());
        //
        Label userLabel = new Label("User:", "userId");
        form.add(userLabel);
        List<EntityLabel> userEntityLabels = new ArrayList<>();
        for (User user : userRepository
                .list()) {
            userEntityLabels.add(new EntityLabel(user.getId().toString(), user.getName() + " " + user.getSurname()));
        }
        EntitySelect assigneeIdSelect = new EntitySelect("userId", userId, userEntityLabels);

        form.add(assigneeIdSelect);

        form.add(new Br());
        //
        form.add(new Label("Class:", "clazz"));
        form.add(new Input(InputType.TEXT, "clazz", ""));
        form.add(new Br());
        //
        form.add(new Label("ID:", "id"));
        form.add(new Input(InputType.TEXT, "id", ""));
        form.add(new Br());
        //
        form.add(new Label("Sortkey:", "sortkey"));
        form.add(new Input(InputType.TEXT, "sortkey", ""));
        form.add(new Br());

        //
        form.add(new SubmitInput("Add"));
        form.add(new HiddenInput("webAppClassSimpleName", "AddSimpleTicket"));
        form.add(new HiddenInput("process", "true"));

        div.add(form);
        final String asHtml = div.build();

        return asHtml;
    }
}
