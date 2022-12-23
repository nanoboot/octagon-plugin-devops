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

package org.nanoboot.octagon.plugin.devops.classes;

import org.nanoboot.octagon.entity.core.Entity;
import org.nanoboot.octagon.entity.core.EntityAttribute;
import lombok.Data;

import java.util.*;
import org.nanoboot.octagon.entity.core.EntityAttributeType;
import org.nanoboot.octagon.plugin.devops.misc.SimpleTicket;
import org.nanoboot.powerframework.json.JsonArray;
import org.nanoboot.powerframework.json.JsonObject;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
@Data
public class UserKanbanBoard implements Entity {

    private static List<EntityAttribute> SCHEMA;

    /**
     * UUID identification of this entity.
     */
    private UUID id;
    private String name;
    private UUID projectId;
    private UUID userId;
    private String data;

    public void validate() {

    }

    @Override
    public void loadFromMap(Map<String, String> map) {
        setName(getStringParam("name", map));
        setProjectId(getUuidParam("projectId", map));
        setUserId(getUuidParam("userId", map));
        setData(getStringParam("data", map));
    }

    @Override
    public Class getEntityClass() {
        return getClass();
    }

    @Override
    public String[] toStringArray() {
        return new String[]{
            id == null ? "" : id.toString(),
            name == null ? "" : name,
            projectId == null ? "" : projectId.toString(),
            userId == null ? "" : userId.toString(),
            data == null ? "" : data,};
    }

    @Override
    public List<EntityAttribute> getSchema() {
        if (SCHEMA == null) {
            SCHEMA = new ArrayList<>();

            SCHEMA.add(EntityAttribute.getIdEntityAttribute());
            SCHEMA.add(new EntityAttribute("name").withMandatory(true).withReadonly(Boolean.TRUE));
            SCHEMA.add(new EntityAttribute("projectId", "project", "getProjects").withMandatory(Boolean.TRUE).withReadonly(Boolean.TRUE));
            SCHEMA.add(new EntityAttribute("userId", "user", "getUsers").withMandatory(Boolean.TRUE).withReadonly(Boolean.TRUE));
            SCHEMA.add(new EntityAttribute("data", EntityAttributeType.TEXT_AREA));
        }
        return SCHEMA;
    }

    public List<SimpleTicket> listTickets() {
        List<SimpleTicket> tickets = new ArrayList<>();
        if(data == null || data.isBlank()) {
            return tickets;
        }
        
        JsonObject jo = new JsonObject(data);
        JsonArray ja = jo.getArray("tickets");
        for (int i = 0; i < ja.size(); i++) {
            JsonObject e = ja.getObject(i);
            SimpleTicket st = new SimpleTicket(e);
            tickets.add(st);
            System.out.println("Found simple ticket: \n\n" + st.toJsonObject().toPrettyString());
        }
        
        return tickets;

    }
    @Override
    public final boolean doLogActions() {
        return false;
    }

}
