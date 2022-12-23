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

import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.nanoboot.octagon.plugin.development.classes.AbstractDevelopmentTask;
import org.nanoboot.octagon.plugin.development.classes.Product;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.AbstractTask;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
@Data
@AllArgsConstructor
@ToString
public class Ticket implements Comparable<Ticket> {

    public Class clazz;
    public AbstractDevelopmentTask task;
    public Product product;
    public User assignee;
    public String userKanbanBoardName;

    @Override
    public int compareTo(Ticket o2) {
        Ticket ticket1 = this;
        Ticket ticket2 = o2;
        Integer sortkey1 = ticket1.getTask().getSortkey() == null ? Integer.MAX_VALUE : ticket1.getTask().getSortkey();
        Integer sortkey2 = ticket2.getTask().getSortkey() == null ? Integer.MAX_VALUE : ticket2.getTask().getSortkey();
        return sortkey1.compareTo(sortkey2);
    }
    public AbstractTask getTask() {
        return task;
    }
}
