package typo.dsl;

import typo.runtime.Fragment;
import typo.runtime.ResultSetParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * SQL implementation of DeleteBuilder that generates and executes DELETE queries.
 */
public class DeleteBuilderSql<Fields, Row> implements DeleteBuilder<Fields, Row> {
    private final String tableName;
    private final RenderCtx renderCtx;
    private final Structure<Fields, Row> structure;
    private final DeleteParams<Fields> params;

    public DeleteBuilderSql(
            String tableName,
            RenderCtx renderCtx,
            Structure<Fields, Row> structure,
            DeleteParams<Fields> params) {
        this.tableName = tableName;
        this.renderCtx = renderCtx;
        this.structure = structure;
        this.params = params;
    }

    @Override
    public DeleteBuilder<Fields, Row> where(Function<Fields, SqlExpr<Boolean>> predicate) {
        DeleteParams<Fields> newParams = params.where(predicate);
        return new DeleteBuilderSql<>(tableName, renderCtx, structure, newParams);
    }
    
    @Override
    public int execute(Connection connection) {
        Fragment query = sql();
        try (PreparedStatement ps = connection.prepareStatement(query.render())) {
            query.set(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute delete: " + query.render(), e);
        }
    }
    
    @Override
    public List<Row> executeReturning(Connection connection, ResultSetParser<List<Row>> parser) {
        Fragment query = sql().append(Fragment.lit(" RETURNING *"));
        try (PreparedStatement ps = connection.prepareStatement(query.render())) {
            query.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return parser.apply(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute delete returning: " + query.render(), e);
        }
    }
    
    @Override
    public Fragment sql() {
        AtomicInteger counter = new AtomicInteger(0);
        Fields fields = structure.fields();
        
        // Build DELETE clause
        Fragment delete = Fragment.lit("DELETE FROM " + tableName);
        
        // Build WHERE clause
        if (!params.where().isEmpty()) {
            List<SqlExpr<Boolean>> filters = new ArrayList<>();
            for (Function<Fields, SqlExpr<Boolean>> whereFunc : params.where()) {
                filters.add(whereFunc.apply(fields));
            }
            
            SqlExpr<Boolean> combined = filters.get(0);
            for (int i = 1; i < filters.size(); i++) {
                combined = combined.and(filters.get(i), Bijection.asBool());
            }
            
            delete = delete.append(Fragment.lit(" WHERE "))
                .append(combined.render(renderCtx, counter));
        }
        
        return delete;
    }
}