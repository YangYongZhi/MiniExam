package com.maiyoule.miniexam.db;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.dialect.Dialect;

public class Db2Dialect extends Dialect {

	@Override
	public String forDbDeleteById(String tableName, String primaryKey) {
		StringBuilder sql = new StringBuilder("delete from ");
		sql.append(tableName.trim());
		sql.append(" where ").append(primaryKey).append(" = ?");
		return sql.toString();
	}

	@Override
	public String forDbFindById(String tableName, String primaryKey,String columns) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(" from ");
		sql.append(tableName.trim());
		sql.append(" where ").append(primaryKey).append(" = ?");
		return sql.toString();
	}

	@Override
	public void forDbSave(StringBuilder sql, List<Object> paras,
			String tableName, Record record) {
		sql.append("insert into ");
		sql.append(tableName.trim()).append("(");
		StringBuilder temp = new StringBuilder();
		temp.append(") values(");
		
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append(e.getKey());
			temp.append("?");
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(")");
	}

	@Override
	public void forDbUpdate(String tableName, String primaryKey, Object id,
			Record record, StringBuilder sql, List<Object> paras) {
		sql.append("update ").append(tableName.trim()).append(" set ");
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!primaryKey.equalsIgnoreCase(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ").append(primaryKey).append(" = ?");
		paras.add(id);
	}

	@Override
	public String forModelDeleteById(Table tInfo) {
		String pKey = tInfo.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from ");
		sql.append(tInfo.getName());
		sql.append(" where ").append(pKey).append(" = ?");
		return sql.toString();
	}

	@Override
	public String forModelFindById(Table tInfo, String columns) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(" from ");
		sql.append(tInfo.getName());
		sql.append(" where ").append(tInfo.getPrimaryKey()).append(" = ?");
		return sql.toString();
	}

	@Override
	public void forModelSave(Table table, Map<String, Object> attrs,
			StringBuilder sql, List<Object> paras) {
		sql.append("insert into ").append(table.getName()).append("(");
		StringBuilder temp = new StringBuilder(") values(");
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append(colName);
				temp.append("?");
				paras.add(e.getValue());
			}
		}
		sql.append(temp.toString()).append(")");
		
	}

	@Override
	public void forModelUpdate(Table tableInfo, Map<String, Object> attrs,
			Set<String> modifyFlag, String pKey, Object id, StringBuilder sql,
			List<Object> paras) {
		sql.append("update ").append(tableInfo.getName()).append(" set ");
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (!pKey.equalsIgnoreCase(colName) && modifyFlag.contains(colName) && tableInfo.hasColumnLabel(colName)) {
				if (paras.size() > 0)
					sql.append(", ");
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ").append(pKey).append(" = ?");
		paras.add(id);
	}

	@Override
	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize,
			String select, String sqlExceptSelect) {
		
	}

	@Override
	public String forTableBuilderDoBuild(String tableName) {
		return "select * from " + tableName + " where 1 = 2";
	}


}
