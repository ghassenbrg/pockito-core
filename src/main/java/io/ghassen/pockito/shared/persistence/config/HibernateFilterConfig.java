package io.ghassen.pockito.shared.persistence.config;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.context.annotation.Configuration;

@Configuration
@FilterDef(
    name = "archivedFilter", 
    parameters = @ParamDef(name = "archived", type = Boolean.class)
)
public class HibernateFilterConfig {
}


