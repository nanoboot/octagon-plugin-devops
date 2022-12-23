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
import org.nanoboot.octagon.core.exceptions.OctagonException;
import org.nanoboot.octagon.core.utils.RegistryImpl;
import org.nanoboot.octagon.entity.api.Repository;
import org.nanoboot.octagon.entity.core.ActionType;
import org.nanoboot.octagon.plugin.actionlog.api.ActionLogRepository;
import org.nanoboot.octagon.plugin.actionlog.classes.ActionLog;
import org.nanoboot.octagon.plugin.development.classes.AbstractDevelopmentTask;
import org.nanoboot.octagon.plugin.development.classes.Bug;
import org.nanoboot.octagon.plugin.development.classes.DevSubTask;
import org.nanoboot.octagon.plugin.development.classes.DevTask;
import org.nanoboot.octagon.plugin.development.classes.Enhancement;
import org.nanoboot.octagon.plugin.development.classes.Epic;
import org.nanoboot.octagon.plugin.development.classes.Incident;
import org.nanoboot.octagon.plugin.development.classes.NewFeature;
import org.nanoboot.octagon.plugin.development.classes.Problem;
import org.nanoboot.octagon.plugin.development.classes.Product;
import org.nanoboot.octagon.plugin.development.classes.Project;
import org.nanoboot.octagon.plugin.development.classes.Proposal;
import org.nanoboot.octagon.plugin.development.classes.Story;
import org.nanoboot.octagon.plugin.main.classes.User;
import org.nanoboot.octagon.plugin.task.TaskResolution;
import org.nanoboot.octagon.plugin.task.TaskStatus;
import org.nanoboot.powerframework.time.moment.UniversalDateTime;


/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.1.0
 */
public class TicketRepositoryFullImpl implements TicketRepository {

    private final RegistryImpl<Repository> repositoryRegistry;
    private final List<Repository<? extends AbstractDevelopmentTask>> ticketRepositoryRegistries = new ArrayList<>();
    private final Map<String, Product> productCache = new HashMap<>();
    private final Map<String, User> userCache = new HashMap<>();
    private final Repository<Product> productRepository;
    private final Repository<Project> projectRepository;
    private final Repository<User> userRepository;
    private final Repository<ActionLog> actionLogRepository;

    public TicketRepositoryFullImpl(RegistryImpl<Repository> repositoryRegistry) {
        this.repositoryRegistry = repositoryRegistry;
        this.productRepository = repositoryRegistry.find(Product.class.getSimpleName());
        this.projectRepository = repositoryRegistry.find(Project.class.getSimpleName());
        this.userRepository = repositoryRegistry.find(User.class.getSimpleName());
        this.actionLogRepository = repositoryRegistry.find(ActionLog.class.getSimpleName());
        //
        Repository<Epic> epicRepository = repositoryRegistry.find(Epic.class.getSimpleName());
        Repository<Story> storyRepository = repositoryRegistry.find(Story.class.getSimpleName());
        Repository<DevTask> devTaskRepository = repositoryRegistry.find(DevTask.class.getSimpleName());
        Repository<DevSubTask> devSubTaskRepository = repositoryRegistry.find(DevSubTask.class.getSimpleName());
        Repository<Bug> bugRepository = repositoryRegistry.find(Bug.class.getSimpleName());
        //
        Repository<Enhancement> enhancementRepository = repositoryRegistry.find(Enhancement.class.getSimpleName());
        Repository<NewFeature> newFeatureRepository = repositoryRegistry.find(NewFeature.class.getSimpleName());
        Repository<Proposal> proposalRepository = repositoryRegistry.find(Proposal.class.getSimpleName());
        Repository<Problem> problemRepository = repositoryRegistry.find(Problem.class.getSimpleName());
        Repository<Incident> incidentRepository = repositoryRegistry.find(Incident.class.getSimpleName());

        //
        ticketRepositoryRegistries.add(epicRepository);
        ticketRepositoryRegistries.add(storyRepository);
        ticketRepositoryRegistries.add(devTaskRepository);
        ticketRepositoryRegistries.add(devSubTaskRepository);
        ticketRepositoryRegistries.add(bugRepository);
        //
        ticketRepositoryRegistries.add(enhancementRepository);
        ticketRepositoryRegistries.add(newFeatureRepository);
        ticketRepositoryRegistries.add(proposalRepository);
        ticketRepositoryRegistries.add(problemRepository);
        ticketRepositoryRegistries.add(incidentRepository);
    }

    @Override
    public List<Ticket> listTickets(String projectId, String assigneeUserId, String productIdIn, String onlyForStatus) {
        List<Ticket> tickets = new ArrayList<>();
        final boolean assigneeNotDefined = assigneeUserId == null || assigneeUserId.isBlank();
        String sqlPart = assigneeNotDefined ? "IS NOT NULL" : "='" + assigneeUserId + "' ";
        String sqlWherePart = " PROJECT_ID='" + projectId + "'" + (assigneeNotDefined ? "" :" AND ASSIGNEE ='" + assigneeUserId + "'");
        if (onlyForStatus != null) {
            sqlWherePart = sqlWherePart + " AND STATUS='" + onlyForStatus + "'";
        }
        for (Repository<? extends AbstractDevelopmentTask> r : ticketRepositoryRegistries) {
            for (AbstractDevelopmentTask adt : r.list(sqlWherePart)) {
                final String productId = adt.getProductId() == null ? null : adt.getProductId().toString();
                if (productIdIn != null && !productIdIn.isBlank() && !productIdIn.equals(productId)) {
                    System.out.println("productIdIn=" + productIdIn + " productId=" + productId);
                    continue;
                }
                Product product = findProduct(productId);
                //
                final String userId = adt.getAssignee() == null ? null : adt.getAssignee().toString();
                User assignee = findAssignee(userId);

                //
                Ticket ticket = new Ticket(r.getClassOfType(), adt, product, assignee, null);
                tickets.add(ticket);

            }
        }
        return tickets;
    }

    @Override
    public Product findProduct(String productId) {
        if (!productCache.containsKey(productId)) {
            productCache.put(productId, productRepository.read(productId));
        }
        Product product = productCache.get(productId);
        return product;
    }

    @Override
    public User findAssignee(String userId) {
        if(userId == null || userId.isBlank()) {
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
    public void changeStatus(String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment) {
        Repository<AbstractDevelopmentTask> repo = null;
        for (Repository<? extends AbstractDevelopmentTask> repoIterated : ticketRepositoryRegistries) {
            if (repoIterated.getClassOfType().getSimpleName().equals(classSimpleName)) {
                repo = (Repository<AbstractDevelopmentTask>) repoIterated;
                break;
            }
        }
        AbstractDevelopmentTask taskBefore = repo.read(id);
        AbstractDevelopmentTask taskAfter = repo.read(id);
        if(newStatus.equals(taskBefore.getStatus().name())) {
            //nothing to do
            return;
        }
        taskAfter.setStatus(TaskStatus.valueOf(newStatus));
        if (newResolution != null && !newResolution.isBlank()) {
            taskAfter.setResolution(TaskResolution.valueOf(newResolution));
            taskAfter.setResolutionComment(newResolutionComment);
        }
        if (!taskAfter.getStatus().isClosed()) {
            taskAfter.setResolution(null);
            taskAfter.setResolutionComment(null);
        }
        if(taskBefore.toString().equals(taskAfter.toString())) {
            System.err.println("Nothing to do.");
            return;
        }
        repo.update(taskAfter);
        ((ActionLogRepository) actionLogRepository).persistActionLog(ActionType.UPDATE, taskBefore, taskAfter, taskAfter, "Status changed by DevOps Kanban Board webapp");
    }

    @Override
    public void changeSortkey(String classSimpleName, String id, String newSortkey) {
        Repository<AbstractDevelopmentTask> repo = null;
        for (Repository<? extends AbstractDevelopmentTask> repoIterated : ticketRepositoryRegistries) {
            if (repoIterated.getClassOfType().getSimpleName().equals(classSimpleName)) {
                repo = (Repository<AbstractDevelopmentTask>) repoIterated;
                break;
            }
        }
        AbstractDevelopmentTask taskBefore = repo.read(id);
        AbstractDevelopmentTask taskAfter = repo.read(id);
        boolean newSorkeyIsNullOrBlank = newSortkey == null || newSortkey.isBlank();
        if(newSorkeyIsNullOrBlank && taskBefore.getSortkey() == null) {
            //nothing to do
            System.out.println("newSorkeyIsNullOrBlank && taskBefore.getSortkey() == null Nothing to do. Returning.");
            return;
        }
        if(taskBefore.getSortkey()!=null && !newSorkeyIsNullOrBlank && newSortkey.equals(taskBefore.getSortkey().toString())) {
            //nothing to do
            System.out.println("!newSorkeyIsNullOrBlank && newSortkey.equals(taskBefore.getSortkey().toString()) Nothing to do. Returning.");
            return;
        }
        taskAfter.setSortkey(newSorkeyIsNullOrBlank ? null : Integer.valueOf(newSortkey));
        
        if(taskBefore.toString().equals(taskAfter.toString())) {
            //nothing to do
            System.out.println("taskBefore.toString().equals(taskAfter.toString()). Nothing to do. Exiting.");
            return;
        }
        repo.update(taskAfter);
        ((ActionLogRepository) actionLogRepository).persistActionLog(ActionType.UPDATE, taskBefore, taskAfter, taskAfter, "Sortkey changed by DevOps Kanban Board webapp");
    }

    @Override
    public UniversalDateTime getDateOfLastModification(String type, String objectId) {
        return ((ActionLogRepository) actionLogRepository).getDateOfLastModification(type, objectId);
    }

    @Override
    public void changeSortkey(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newSortkey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeStatus(String userKanbanBoardName, String assingeeUserId, String classSimpleName, String id, String newStatus, String newResolution, String newResolutionComment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
