package repositories;

import entities.User;

public class UserRepository extends EntityRepository<User> {

    public UserRepository() {
        super(User.class);
    }

    //TODO: PRODUCE A METHOD FOR GETTING A COLUMN FIELD OF METHOD IN ENTITY REPOSITORY
    public User findByUsername(String username) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        String columnName = this.getColumnName(methodName);
        return this.findByColumn(columnName, username);
    }

    public User findById(String userId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        String columnName = this.getColumnName(methodName);
        return this.findByColumn(columnName, userId);
    }
}
