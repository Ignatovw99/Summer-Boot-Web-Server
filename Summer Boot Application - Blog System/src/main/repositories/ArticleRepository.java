package repositories;

import entities.Article;

public class ArticleRepository extends EntityRepository<Article> {

    public ArticleRepository() {
        super(Article.class);
    }

    public Article findById(String articleId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        String columnName = this.getColumnName(methodName);
        return this.findByColumn(columnName, articleId);
    }
}
