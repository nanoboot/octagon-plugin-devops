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

import java.util.List;
import org.nanoboot.octagon.plugin.development.classes.Product;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.powerframework.time.moment.UniversalDateTime;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public interface TicketRepository {

    void changeSortkey(String classSimpleName, String id, String newSortkey);

    void changeSortkey(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newSortkey);
    void changeStatus(String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment);
    void changeStatus(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment);
    
    User findAssignee(String userId);

    Product findProduct(String productId);

    default List<Ticket> listTickets(String projectId, String assigneeUserId, String productId) {
        return listTickets(projectId, assigneeUserId, productId, null);
    }


    List<Ticket> listTickets(String projectId, String assigneeUserId, String productIdIn, String onlyForStatus);
    UniversalDateTime getDateOfLastModification(String type, String objectId);
}
