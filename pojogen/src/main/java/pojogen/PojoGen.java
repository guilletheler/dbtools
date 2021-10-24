package pojogen;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.gt.jdbcutils.components.Column;
import com.gt.jdbcutils.components.Database;
import com.gt.jdbcutils.components.Table;
import com.gt.jdbcutils.helpers.DialectSyntaxHelper;
import com.gt.jdbcutils.helpers.JdbcMetaDecoder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PojoGen {

	Connection connection;

	String catalogName;

	List<Pair<String, String>> tables = new ArrayList<>();

	String projectPackageName;

	String entitiesSubPackageName = "model";

	Database database;

	public String buildPojo(String schemaName, String tableName) {
		if (database == null) {
			try {
				database = JdbcMetaDecoder.buildDatabase(connection);
			} catch (SQLException e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no se puede construir el metadatos de la db");
			}
		}

		Table table = database.getTableByName(schemaName, tableName);

		if (table == null) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "No se encontró la tabla " + tableName);
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("package ").append(projectPackageName).append(".").append(entitiesSubPackageName).append(";\n\n");
		sb.append("import lombok.Data;\n");
		sb.append("import java.io.Serializable;\n");
		sb.append("\n");
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
		if (table.getColumnByName("codigo") != null && table.getColumnByName("nombre") != null) {
			sb.append("import ").append(projectPackageName).append(".").append("model.CodigoNombre;\n");
		}
		sb.append("import ").append(projectPackageName).append(".").append("model.IWithIntegerId;\n");
		if (table.getColumnByName("observaciones") != null) {
			sb.append("import ").append(projectPackageName).append(".").append("model.IWithObservaciones;\n");
		}
		sb.append("\n");
		sb.append("import lombok.Data;\n");
		sb.append("import lombok.EqualsAndHashCode");
		sb.append("\n\n");

		sb.append("@Entity\n");
		sb.append("@Table(");
		if (schemaName != null && !schemaName.isEmpty()) {
			sb.append("schema = \"").append(schemaName).append("\", ");
		}
		sb.append("name = \"").append(tableName).append("\")\n");
		sb.append("@Data\n");
		sb.append("@EqualsAndHashCode");
		if (table.getColumnByName("codigo") != null && table.getColumnByName("nombre") != null) {
			sb.append("(callSuper = true)");
		}
		sb.append("\n");

		String className = getTableClassName(table);

		sb.append("public class ").append(className).append(" ");

		if (table.getColumnByName("codigo") != null && table.getColumnByName("nombre") != null) {
			sb.append("extends CodigoNombre ");
		}

		sb.append("implements ");
		if (table.getColumnByName("observaciones") != null) {
			sb.append("IWithObservaciones, ");

		}

		sb.append("Serializable {\n\n");

		sb.append("\tprivate static final long serialVersionUID = 1L;\n");
		sb.append("\n\n");

		for (Column col : table.getColumns()) {
			Column manyToOne = database.getManyToOne(table, col);
			if (manyToOne != null) {
				sb.append("\t@ManyToOne\n");
				className = getTableClassName(manyToOne.getTable());
				sb.append("\t").append(className).append(" ");
				sb.append(getVarName(getTableClassName(manyToOne.getTable())));
			} else {
				className = DialectSyntaxHelper.SQLToJavaClass(col).getName();
				if (className.startsWith("java.lang.")) {
					className = className.substring(10);
				}
				if (className.equals("java.util.Date")) {
					sb.append("\t@Temporal(TemporalType.TIMESTAMP)\n");
				}
				sb.append("\t").append(className).append(" ");
				sb.append(getVarName(col.getName()));
			}
			sb.append(";\n\n");
		}

		List<Table> tablesRef = database.getOneToMany(table);

		for (Table tableRef : tablesRef) {
			sb.append("\t@EqualsAndHashCode.Exclude\n").append("\t@ToString.Exclude\n")
					.append("\t@Getter(value = AccessLevel.NONE)\n").append("\t@OneToMany(mappedBy = \"")
					.append(getVarName(getTableClassName(table)))
					.append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n").append("\tprivate List<")
					.append(getTableClassName(tableRef)).append("> ")
					.append(getPluralVarName(getTableClassName(tableRef))).append(";\n").append("\t\n");
		}

		for (Table tableRef : tablesRef) {
			sb.append("\tpublic List<").append(getTableClassName(tableRef)).append("> get")
					.append(StringUtils.capitalize(getPluralVarName(getTableClassName(tableRef)))).append("() {\n")
					.append("\t\tif (").append(getPluralVarName(getTableClassName(tableRef))).append(" == null) {\n")
					.append("\t\t\t").append(getPluralVarName(getTableClassName(tableRef)))
					.append(" = new ArrayList<>();\n").append("\t\t}\n").append("\t\treturn ")
					.append(getPluralVarName(getTableClassName(tableRef))).append(";\n").append("\t}\n\n");
		}

		sb.append("}");

		return sb.toString();

	}

	private String getTableClassName(Table table) {
		String className = StringUtils.capitalize(getVarName(table.getNombre()));

		if (!className.endsWith("tes") && !className.endsWith("des") && className.endsWith("es")) {
			className = className.substring(0, className.length() - 2);
		} else if (className.endsWith("s")) {
			className = className.substring(0, className.length() - 1);
		}
		return className;
	}

	private String getVarName(String name) {
		String[] partes = name.split("\\s|\\_");

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

}
