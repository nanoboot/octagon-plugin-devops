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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nanoboot.octagon.plugin.task.TaskResolution;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.json.JsonObject;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleTicket {

    public String clazz;
    public String id;
    public String name;
    public Integer sortkey;
    public TaskStatus status;
    public TaskResolution resolution;
    public String resolutionComment;

    public SimpleTicket(JsonObject e) {
        try {
        this.clazz = e.getString("clazz");
        this.id = e.getString("id");
        this.name = e.getString("name");
        this.sortkey = e.isNull("sortkey") ? null : e.getInt("sortkey");
        this.status = TaskStatus.valueOf(e.getString("status"));
        this.resolution = e.isNull("resolution") || e.getString("resolution").isEmpty() ? null : TaskResolution.valueOf(e.getString("resolution"));
        this.resolutionComment = e.isNull("resolutionComment") ? null :e.getString("resolutionComment");
        } catch (RuntimeException ex) {
            System.err.println(e.toPrettyString());
            ex.printStackTrace();
            throw ex;
        }
    }

    public JsonObject toJsonObject() {
        JsonObject jo = new JsonObject();
        jo.add("clazz", clazz);
        jo.add("id", id);
        jo.add("name", name);
        jo.add("sortkey", sortkey == null ? null : sortkey.intValue());
        jo.add("status", status == null ? null : status.name());
        jo.add("resolution", resolution == null ? null : resolution.name());
        jo.add("resolutionComment", resolutionComment);
        return jo;
    }
}
