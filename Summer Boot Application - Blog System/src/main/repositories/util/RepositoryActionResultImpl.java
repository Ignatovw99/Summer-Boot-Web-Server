package repositories.util;

public class RepositoryActionResultImpl implements RepositoryActionResult {

    private Object result;

    public RepositoryActionResultImpl() {
    }

    public Object getResult() {
        return this.result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
