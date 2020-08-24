package repositories;

import javax.persistence.NoResultException;
import java.util.List;

public class EntityRepository<T> extends BaseRepository {

    private final Class<T> classType;

    public EntityRepository(Class<T> classType) {
        super();
        this.classType = classType;
    }

    public void save(T entity) {
        executeAction(repositoryActionResult ->
                super.entityManager.persist(entity)
        );
    }

    public void update(T entity) {
        executeAction(repositoryActionResult ->
                super.entityManager.merge(entity)
        );
    }

    public void delete(T entity) {
        executeAction(repositoryActionResult -> {
            T entityToDelete = super.entityManager.merge(entity);
            super.entityManager.remove(entityToDelete);
        });
    }

    public List<T> findAll() {
        return (List<T>) executeAction(repositoryActionResult ->
                repositoryActionResult.setResult(
                        entityManager.createQuery(String.format("SELECT e FROM %s e", classType.getSimpleName()), classType)
                                .getResultList()
                )).getResult();
    }

    protected String getColumnName(String methodName) {
        String columnNameFromMethod = methodName.replace("findBy", "");
        StringBuilder columnName = new StringBuilder();
        for (int i = 0; i < columnNameFromMethod.length(); i++) {
            char currentChar = columnNameFromMethod.charAt(i);
            if (i == 0) {
                columnName.append(Character.toLowerCase(currentChar));
            } else {
                columnName.append(currentChar);
            }
        }
        return columnName.toString();
    }

    protected  <C> T findByColumn(String columnName, C columnValue) {
        return (T) executeAction(repositoryActionResult -> {
            T entityResult = null;
            try {
                entityResult = this.entityManager.createQuery(
                        String.format("SELECT e FROM %s e WHERE %s = '%s'", classType.getSimpleName(), columnName, columnValue), classType)
                        .getSingleResult();
            } catch (NoResultException ignored) {
                ;
            }
            repositoryActionResult.setResult(entityResult);
        }).getResult();

        //The single quotes can probably produce bugs for types as boolean and numbers
    }
}