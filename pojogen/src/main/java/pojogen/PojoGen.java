package pojogen;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PojoGen {

	Connection conn;

	String catalogName;

	List<Pair<String, String>> tables = new ArrayList<>();

	String projectPackageName;

	String entitiesSubPackageName = "model";

	public String buildPojo(String schemaName, String tableName, boolean buildRefs) throws SQLException {

		StringBuilder sb = new StringBuilder();

		sb.append("package ").append(projectPackageName).append(".").append(entitiesSubPackageName).append(";\n\n");
		sb.append("import lombok.Data;\n");
		sb.append("import java.io.Serializable;\n");
		sb.append("import java.util.ArrayList;\n");
		sb.append("import java.util.List;\n");
		sb.append("\n");
		sb.append("import javax.persistence.Column;\n");
		sb.append("import javax.persistence.CascadeType;\n");
		sb.append("import javax.persistence.Column;\n");
		sb.append("import javax.persistence.Entity;\n");
		sb.append("import javax.persistence.GeneratedValue;\n");
		sb.append("import javax.persistence.GenerationType;\n");
		sb.append("import javax.persistence.OneToMany;\n");
		sb.append("import javax.persistence.ManyToOne;\n");
		sb.append("import javax.persistence.Temporal;\n");
		sb.append("import javax.persistence.TemporalType;\n");
		sb.append("import javax.persistence.Id;\n");
		sb.append("import javax.persistence.Table;\n");
		sb.append("\n");
		if (haveColumnCodigo(schemaName, tableName) && haveColumnNombre(schemaName, tableName)) {
			sb.append("import ").append(projectPackageName).append(".").append("model.CodigoNombre;\n");
		}
		sb.append("import ").append(projectPackageName).append(".").append("model.IWithIntegerId;\n");
		if (haveColumnObservaciones(schemaName, tableName)) {
			sb.append("import ").append(projectPackageName).append(".").append("model.IWithObservaciones;\n");
		}
		sb.append("\n");
		sb.append("import lombok.Data;\n");
		sb.append("import lombok.EqualsAndHashCode;\n");
		sb.append("\n");

		sb.append("@Entity\n");
		sb.append("@Table(");
		if (schemaName != null && !schemaName.isEmpty()) {
			sb.append("schema = \"").append(schemaName).append("\", ");
		}
		sb.append("name = \"").append(tableName).append("\")\n");
		sb.append("@Data\n");
		sb.append("@EqualsAndHashCode");
		if (haveColumnCodigo(schemaName, tableName) && haveColumnNombre(schemaName, tableName)) {
			sb.append("(callSuper = true)");
		}
		sb.append("\n");

		String className = getTableClassName(tableName);

		sb.append("public class ").append(className).append(" ");

		if (haveColumnCodigo(schemaName, tableName) && haveColumnNombre(schemaName, tableName)) {
			sb.append("extends CodigoNombre ");
		}

		sb.append("implements ");
		if (haveColumnObservaciones(schemaName, tableName)) {
			sb.append("IWithObservaciones, ");

		}

		sb.append("Serializable {\n\n");

		sb.append("\tprivate static final long serialVersionUID = 1L;\n");
		sb.append("\n\n");

		DatabaseMetaData metaData = conn.getMetaData();

		List<String> tableRefs = new ArrayList<>();

		try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, "%")) {
			while (rs.next()) {

				String columnName = rs.getString("COLUMN_NAME");
				String refTable = fkTableName(schemaName, tableName, columnName);
				if (refTable != null) {
					sb.append("\t// ref ").append(refTable).append("\n");
					sb.append("\t@ManyToOne\n");
					className = getTableClassName(refTable);
					tableRefs.add(refTable);
					sb.append("\t").append(className).append(" ");
					String varName = rs.getString("COLUMN_NAME");
					if (varName.toLowerCase().endsWith("_id")) {
						varName = varName.substring(0, varName.length() - 3);
					} else if (varName.toLowerCase().endsWith("id")) {
						varName = varName.substring(0, varName.length() - 2);
					} else if (varName.toLowerCase().startsWith("id")) {
						varName = varName.substring(2);
					}
					varName = getVarName(varName);
					sb.append(varName);
				} else {

					if (isPk(schemaName, tableName, columnName)) {
						sb.append("\t@Id\n");
						if (isAutoIncrement(schemaName, tableName, columnName)) {
							sb.append("\t@GeneratedValue(strategy = GenerationType.IDENTITY)\n");

						}
					}

					className = SQLToJavaClass(rs.getInt("DATA_TYPE")).getName();
					if (className.startsWith("java.lang.")) {
						className = className.substring(10);
					}
					if (className.equals("java.util.Date")) {
						sb.append("\t@Temporal(TemporalType.DATE)\n");
					}
					if (className.equals("java.sql.Timestamp")) {
						sb.append("\t@Temporal(TemporalType.TIMESTAMP)\n");
						className = "java.util.Date";
					}

					if (className.equals("[B")) {
						className = "byte[]";
					}
					
					sb.append("\t@Column(name = \"").append(columnName).append("\")\n");
					sb.append("\t").append(className).append(" ");
					sb.append(getVarName(columnName));
				}
				sb.append(";\n\n");
			}
		}

		if (buildRefs) {
			List<String[]> tablesRef = exportedRef(schemaName, tableName);

//		sb.append("// exported\n");
			for (String[] ref : tablesRef) {
				sb.append("\t// ref ").append(Arrays.toString(ref)).append("\n");
				sb.append("\t@EqualsAndHashCode.Exclude\n").append("\t@ToString.Exclude\n")
						.append("\t@Getter(value = AccessLevel.NONE)\n").append("\t@OneToMany(mappedBy = \"")
						.append(getVarName(getTableClassName(ref[2])))
						.append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n").append("\tprivate List<")
						.append(getTableClassName(ref[6])).append("> ")
						.append(getPluralVarName(getTableClassName(ref[6]))).append(";\n").append("\t\n");
			}
//
//		sb.append("imported\n");
//		for (String[] ref : importedRef(schemaName, tableName)) {
//			sb.append(Arrays.toString(ref));
//			sb.append("\n");
//		}

			for (String[] ref : tablesRef) {
				sb.append("\tpublic List<").append(getTableClassName(ref[6])).append("> get")
						.append(StringUtils.capitalize(getPluralVarName(getTableClassName(ref[6])))).append("() {\n")
						.append("\t\tif (").append(getPluralVarName(getTableClassName(ref[6]))).append(" == null) {\n")
						.append("\t\t\t").append(getPluralVarName(getTableClassName(ref[6])))
						.append(" = new ArrayList<>();\n").append("\t\t}\n").append("\t\treturn ")
						.append(getPluralVarName(getTableClassName(ref[6]))).append(";\n").append("\t}\n\n");
			}
		}
		sb.append("}\n\n");
//
//		for (Table tableRef : tableRefs) {
//			if (!tableRef.equals(table)) {
//
//				sb.append("\n\n\n\n");
//				sb.append(buildPojo(tableRef));
//			}
//		}

		return sb.toString();

	}

	private boolean isAutoIncrement(String schemaName, String tableName, String columnName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();
		boolean ret = false;
		try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, columnName)) {
			while (rs.next()) {
				ret = rs.getString("IS_AUTOINCREMENT") != null
						&& rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("yes");
			}
		}

		return ret;
	}

	private boolean isPk(String schemaName, String tableName, String columnName) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		boolean ret = false;
		try (ResultSet rs = metaData.getPrimaryKeys(conn.getCatalog(), schemaName, tableName)) {

			while (rs.next()) {
				if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
					ret = true;
					break;
				}
			}
		}

		return ret;
	}

	private String getTableClassName(String tableName) {
		String className = StringUtils.capitalize(getVarName(tableName));

		if (!className.endsWith("tes") && !className.endsWith("des") && className.endsWith("es")) {
			className = className.substring(0, className.length() - 2);
		} else if (className.endsWith("s")) {
			className = className.substring(0, className.length() - 1);
		}
		return className;
	}

	private String getVarName(String name) {
		String[] partes = name.trim().split("\\s|\\_");

		return StringUtils.uncapitalize(Arrays.asList(partes).stream().map(s -> StringUtils.capitalize(s.toLowerCase()))
				.collect(Collectors.joining()));
	}

	private String getPluralVarName(String name) {

		String ret = StringUtils.uncapitalize(name);

		if (ret.endsWith("n") || ret.endsWith("s") || ret.endsWith("d") || ret.endsWith("b")) {
			ret += "es";
		} else {
			ret += "s";
		}

		return ret;
	}

	private boolean haveColumnCodigo(String schemaName, String tableName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		boolean ret = false;

		try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, "codigo")) {
			while (rs.next()) {
				ret = true;
			}
		}

		return ret;
	}

	private boolean haveColumnNombre(String schemaName, String tableName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		boolean ret = false;

		try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, "nombre")) {
			while (rs.next()) {
				ret = true;
			}
		}

		return ret;
	}

	private boolean haveColumnObservaciones(String schemaName, String tableName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		boolean ret = false;

		try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schemaName, tableName, "observaciones")) {
			while (rs.next()) {
				ret = true;
			}
		}

		return ret;
	}

	private String fkTableName(String schemaName, String tableName, String columName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		String ret = null;

		try (ResultSet rs = metaData.getImportedKeys(conn.getCatalog(), schemaName, tableName)) {
			while (rs.next()) {
				if (rs.getString("FKCOLUMN_NAME").equalsIgnoreCase(columName)) {
					ret = rs.getString("PKTABLE_NAME");
					break;
				}
			}
		}

		return ret;
	}

	private List<String[]> importedRef(String schemaName, String tableName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		List<String[]> ret = new ArrayList<>();

		try (ResultSet rs = metaData.getImportedKeys(conn.getCatalog(), schemaName, tableName)) {
			while (rs.next()) {
				ret.add(new String[] { rs.getString("PK_NAME"), rs.getString("PKTABLE_SCHEM"),
						rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FK_NAME"),
						rs.getString("FKTABLE_SCHEM"), rs.getString("FKTABLE_NAME"), rs.getString("FKCOLUMN_NAME") });
			}
		}

		return ret;
	}

	private List<String[]> exportedRef(String schemaName, String tableName) throws SQLException {

		DatabaseMetaData metaData = conn.getMetaData();

		List<String[]> ret = new ArrayList<>();

		try (ResultSet rs = metaData.getExportedKeys(conn.getCatalog(), schemaName, tableName)) {
			while (rs.next()) {
				ret.add(new String[] { rs.getString("PK_NAME"), rs.getString("PKTABLE_SCHEM"),
						rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FK_NAME"),
						rs.getString("FKTABLE_SCHEM"), rs.getString("FKTABLE_NAME"), rs.getString("FKCOLUMN_NAME") });
			}
		}

		return ret;
	}

	public static Class<?> SQLToJavaClass(int dataType) {

		switch (dataType) {
		case Types.BIGINT:
			return Long.class;
		case Types.LONGVARBINARY:
			return byte[].class;
		case Types.BINARY:
			return byte[].class;
		case Types.BOOLEAN:
		case Types.BIT:
			return Boolean.class;
		case Types.CHAR:
			return String.class;
		case Types.DATE:
			return java.util.Date.class;
		case Types.TIMESTAMP:
			return java.sql.Timestamp.class;
		case Types.DECIMAL:
//			return java.math.BigDecimal.class;
			return java.lang.Double.class;
		case Types.DOUBLE:
			return double.class;
		case Types.INTEGER:
			return Integer.class;
		case Types.NCHAR:
			return String.class;
		case Types.LONGVARCHAR:
			return String.class;
		case Types.NUMERIC:
//			return java.math.BigDecimal.class;
			return java.lang.Double.class;
		case Types.VARCHAR:
			return String.class;
		case Types.NVARCHAR:
			return String.class;
		case Types.REAL:
			return Float.class;
		case Types.SMALLINT:
			return Short.class;
		case Types.TINYINT:
			return Short.class;
		case Types.VARBINARY:
			return byte[].class;
		}

		Logger.getLogger(PojoGen.class.getName()).log(Level.WARNING, "tipo no reconocido: " + dataType);

		return Object.class;
	}
}
