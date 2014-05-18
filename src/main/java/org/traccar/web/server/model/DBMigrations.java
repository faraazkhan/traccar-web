/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.model;

import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.User;
import org.traccar.web.shared.model.UserSettings;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class DBMigrations {
    public void migrate(EntityManager em) throws Exception {
        for (Migration migration : new Migration[] {
                new CreateAdmin(),
                new SetUpdateInterval(),
                new SetTimePrintInterval()
        }) {
            em.getTransaction().begin();
            try {
                migration.migrate(em);
                em.getTransaction().commit();
            } catch (Exception ex) {
                em.getTransaction().rollback();
                throw ex;
            }
        }
    }

    interface Migration {
        void migrate(EntityManager em) throws Exception;
    }

    /**
     * Create Administrator account
     */
    static class CreateAdmin implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<User> query = em.createQuery("SELECT x FROM User x WHERE x.login = 'admin'", User.class);
            List<User> results = query.getResultList();
            if (results.isEmpty()) {
                User user = new User();
                user.setLogin("admin");
                user.setPassword("admin");
                user.setAdmin(true);
                em.persist(user);
            }
        }
    }

    /**
     * Set up update interval in application settings
     */
    static class SetUpdateInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.updateInterval = :ui WHERE S.updateInterval IS NULL")
                    .setParameter("ui", ApplicationSettings.DEFAULT_UPDATE_INTERVAL)
                    .executeUpdate();
        }
    }

    /**
     * set up time print interval in user settings
     */
    static class SetTimePrintInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.timePrintInterval = :tpi WHERE S.timePrintInterval IS NULL")
                    .setParameter("tpi", UserSettings.DEFAULT_TIME_PRINT_INTERVAL)
                    .executeUpdate();
        }
    }
}