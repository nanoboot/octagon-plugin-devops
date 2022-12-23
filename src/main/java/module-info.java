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

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
module octagon.plugin.devops {
    exports org.nanoboot.octagon.plugin.devops.classes;
    requires lombok;
    requires octagon.entity;
    requires octagon.core;
    requires octagon.plugin.task;
    requires powerframework.time;
    requires powerframework.xml;
    requires powerframework.web;
    requires org.mybatis;
    requires java.sql;
    requires powerframework.json;
    requires octagon.plugin.api;
    requires octagon.plugin.development;
    requires octagon.plugin.main;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
}
