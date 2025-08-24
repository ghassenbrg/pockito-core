package io.ghassen.pockito.config;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Hibernate filter definitions for all entities.
 * This prevents duplicate filter definition errors.
 */
@Configuration
@FilterDef(
    name = "archivedFilter", 
    parameters = @ParamDef(name = "archived", type = Boolean.class)
)
public class HibernateFilterConfig {
    // This class only serves to define the shared filter
    // No additional configuration needed
}
