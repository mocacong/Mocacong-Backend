package mocacong.server.support;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.metamodel.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner implements InitializingBean {

    private static final String FOREIGN_KEY_RULE_UPDATE_FORMAT = "SET REFERENTIAL_INTEGRITY %s";
    private static final String TRUNCATE_FORMAT = "TRUNCATE TABLE %s";
    private static final String ID_RESET_FORMAT = "ALTER TABLE %s ALTER COLUMN %s_id RESTART WITH 1";

    @PersistenceContext
    EntityManager entityManager;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        this.tableNames = entityManager.getMetamodel()
                .getEntities()
                .stream()
                .map(Type::getJavaType)
                .map(javaType -> javaType.getAnnotation(Table.class))
                .map(Table::name)
                .collect(Collectors.toList());
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_RULE_UPDATE_FORMAT, "FALSE"))
                .executeUpdate();
        for (final String tableName : tableNames) {
            entityManager.createNativeQuery(String.format(TRUNCATE_FORMAT, tableName)).executeUpdate();
            entityManager.createNativeQuery(String.format(ID_RESET_FORMAT, tableName, tableName)).executeUpdate();
        }
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_RULE_UPDATE_FORMAT, "TRUE"))
                .executeUpdate();
    }
}
