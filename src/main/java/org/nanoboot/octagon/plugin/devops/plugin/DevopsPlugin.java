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

package org.nanoboot.octagon.plugin.devops.plugin;

import java.util.Properties;
import org.nanoboot.octagon.plugin.api.core.Plugin;
import org.nanoboot.octagon.plugin.api.core.PluginStub;
import org.nanoboot.octagon.plugin.api.core.PluginStubImpl;
import lombok.Getter;
import org.nanoboot.octagon.plugin.devops.classes.UserKanbanBoard;
import org.nanoboot.octagon.plugin.devops.persistence.impl.mappers.UserKanbanBoardMapper;
import org.nanoboot.octagon.plugin.devops.persistence.impl.repos.UserKanbanBoardRepositoryImplSQLiteMyBatis;
import org.nanoboot.octagon.plugin.devops.webapps.AddSimpleTicket;
import org.nanoboot.octagon.plugin.devops.webapps.KanbanBoard;
import org.nanoboot.octagon.plugin.devops.webapps.MoveSimpleTicketToKanbanBoard;
import org.nanoboot.octagon.plugin.devops.webapps.RemoveSimpleTicket;
import org.nanoboot.octagon.plugin.devops.webapps.SimpleKanbanBoard;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class DevopsPlugin implements Plugin {
    
    private static final String DEVELOPMENT = "development";
    private static final String DEVOPS = "devops";
    @Getter
    private PluginStub pluginStub = new PluginStubImpl();
    
    @Override
    public String getGroup() {
        return DEVELOPMENT;
    }
    
    @Override
    public String getId() {
        return DEVOPS;
    }
    
    @Override
    public String getVersion() {
        return "0.3.0";
    }
    
    @Override
    public String init(Properties propertiesConfiguration) {
        for (Object objectKey : propertiesConfiguration.keySet()) {
            String key = (String) objectKey;
            String value = propertiesConfiguration.getProperty(key);
            System.out.println("Found configuration entry for plugin devops: " + key + "=" + value);
        }
        pluginStub.registerEntityGroup(DEVOPS, 22);
        
        int sortkeyInGroup = 10;
        pluginStub
                .registerEntity(
                        DEVOPS,
                        UserKanbanBoard.class,
                        UserKanbanBoardMapper.class,
                        UserKanbanBoardRepositoryImplSQLiteMyBatis.class, sortkeyInGroup++, true);
        pluginStub.registerWebApp(KanbanBoard.class.getName());
        pluginStub.registerWebApp(SimpleKanbanBoard.class.getName());
        pluginStub.registerWebApp(AddSimpleTicket.class.getName());
        pluginStub.registerWebApp(MoveSimpleTicketToKanbanBoard.class.getName());
        pluginStub.registerWebApp(RemoveSimpleTicket.class.getName());
        
        
        return null;
    }
    
    @Override
    public String getDependsOn() {
        return "task";
    }
    
    @Override
    public boolean hasMigrationSchema() {
        return true;
    }
}
