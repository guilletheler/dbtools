package pojogen;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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

	String schemaName;

	String[] tableName;

	String projectPackageName;

	String entitiesSubPackageName;

	Database database;

	public String buildPojo(String tableName) {
		if (database == null) {
			try {
				database = JdbcMetaDecoder.buildDatabase(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Table table = database.getTableByName(tableName, tableName);
		
		if(table== null) {
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
		sb.append("\n");

		sb.append("@Entity");
		sb.append("@Table(");
		if (schemaName != null && !schemaName.isEmpty()) {
			sb.append("schema = \"").append(schemaName).append("\", ");
		}
		sb.append("name = \"").append("\")");
		sb.append("@Data\n");
		sb.append("@EqualsAndHashCode");
		if (table.getColumnByName("codigo") != null && table.getColumnByName("nombre") != null) {
			sb.append("(callSuper = true)");
		}
		sb.append("\n");

		sb.append("public class ").append(StringUtils.capitalize(tableName)).append(" ");

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
			sb.append(DialectSyntaxHelper.SQLToJavaClass(col).getName()).append(" ");
			sb.append(getVarName(col.getName()));
			sb.append(";\n\n");
		}

		sb.append("}");
		return sb.toString();
	}

	private String getVarName(String name) {
		String[] partes = name.split("\\s|_");

		return Arrays.asList(partes).stream().map(s -> StringUtils.capitalize(s.toLowerCase()))
				.collect(Collectors.joining());
	}

}
