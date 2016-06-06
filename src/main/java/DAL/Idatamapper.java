package DAL;

import java.sql.ResultSet;
import java.util.ArrayList;

public interface Idatamapper<T> {

	public ArrayList<T> MapDataToLocalEntity(ResultSet resultSet);

}
