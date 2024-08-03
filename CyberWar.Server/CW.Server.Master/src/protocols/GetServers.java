package protocols;

import database.Database;
import lowentry.ue4.libs.jackson.databind.JsonNode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class GetServers {
	public static HashMap<String, Object> Handle(JsonNode InputData) {
		HashMap<String, Object> data = new HashMap<>();
		data.put("success", true);
		data.put("ecode", 0);
		Object[] arrayServers = new Object[0];
		int dataType = 0;
		int CountServers = -1;

		JsonNode dataTypeNode = InputData.get("type");
		if (dataTypeNode != null) {
			dataType = dataTypeNode.intValue();
		}

		// Используем try-with-resources для автоматического закрытия PreparedStatement и ResultSet
		try (PreparedStatement ps = Database.getConnection().prepareStatement(
				"SELECT count(*) FROM servers WHERE type=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY)) {

			ps.setInt(1, dataType);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					CountServers = rs.getInt(1);
					arrayServers = new Object[CountServers];
				}
			}

			if (dataType > 0) {
				try (PreparedStatement ps2 = Database.getConnection().prepareStatement(
						"SELECT * FROM servers WHERE type=?",
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY)) {

					ps2.setInt(1, dataType);
					try (ResultSet rs2 = ps2.executeQuery()) {
						int i = 0;

						while (rs2.next()) {
							HashMap<String, Object> metadata = new HashMap<>();
							metadata.put("id", rs2.getInt("id"));
							metadata.put("name", rs2.getString("name"));
							metadata.put("type", rs2.getInt("type"));
							metadata.put("ip", rs2.getString("ip"));
							metadata.put("port", rs2.getInt("port"));
							metadata.put("max_players", rs2.getInt("max_players"));
							metadata.put("current_players", rs2.getInt("current_players"));
							metadata.put("mode", rs2.getString("game_mode"));

							if (i < arrayServers.length) {
								arrayServers[i] = metadata;
								i++;
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			data.replace("success", false);
		}

		data.put("count", CountServers);
		data.put("server_list", arrayServers);
		return data;
	}
}
