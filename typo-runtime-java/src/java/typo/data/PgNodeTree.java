package typo.data;

/**
 * pg_node_tree stores PostgreSQL's internal parse tree representation.
 * 
 * This type represents PostgreSQL's nodeToString() output format, which is used
 * internally to store parsed SQL expressions, view definitions, default values,
 * check constraints, etc. in the system catalogs.
 * 
 * The format consists of nested nodes with the structure:
 * - Nodes: {NODETYPE :field1 value1 :field2 value2 ...}
 * - Lists: (item1 item2 item3)
 * - Empty values: <>
 * 
 * Example: {QUERY :commandType 1 :querySource 0 :canSetTag true :utilityStmt <>}
 * 
 * Note: This is a PostgreSQL internal format that may change between versions.
 * Direct manipulation is not recommended. Use pg_get_expr() and similar functions
 * when possible to work with the parsed representation.
 */
public record PgNodeTree(String value) {
}