package lunch.record.repository;

import lombok.extern.slf4j.Slf4j;
import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class LunchRecordRepository {

    // [DI + OCP]
    // LunchRecordRepository 는 DataSource 인터페이스에만 의존하기 때문에 DataSource 구현체를 변경해도 LunchRecordRepository 의 코드는 전혀 변경하지 않아도 된다.
    private final DataSource dataSource;

    public LunchRecordRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public LunchRecord save(LunchRecord lunchRecord) throws SQLException {
        String sql = "insert into lunchRecord(restaurant, menu, image, price, grade, averageGrade, updateAt, createAt) values(?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, lunchRecord.getRestaurant());
            pstmt.setString(2, lunchRecord.getMenu());
            pstmt.setBinaryStream(3, lunchRecord.getImage().getBinaryStream());
            pstmt.setBigDecimal(4, lunchRecord.getPrice());
            pstmt.setFloat(5, lunchRecord.getGrade());
            pstmt.setFloat(6, lunchRecord.getAverageGrade());
            pstmt.setTime(7, Time.valueOf(lunchRecord.getUpdateAt()));
            pstmt.setTime(8, Time.valueOf(lunchRecord.getCreateAt()));
            pstmt.executeUpdate();
            return lunchRecord;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public List<LunchRecord> findAll() throws SQLException {
        String sql = "select * from LunchRecord";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            rs = pstmt.executeQuery();

            List<LunchRecord> lunchRecordList = new ArrayList<>();

            while (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = DBConnectionUtil.getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
                lunchRecordList.add(lunchRecord);
            }

            if (lunchRecordList.isEmpty()) {
                throw new NoSuchElementException();
            }

            return lunchRecordList;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public List<LunchRecord> findAll(Connection con) throws SQLException {
        String sql = "select * from LunchRecord";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            rs = pstmt.executeQuery();

            List<LunchRecord> lunchRecordList = new ArrayList<>();

            while (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = DBConnectionUtil.getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
                lunchRecordList.add(lunchRecord);
            }

            if (lunchRecordList.isEmpty()) {
                throw new NoSuchElementException();
            }

            return lunchRecordList;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // Connection 은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public LunchRecord findById(int id) throws SQLException {
        String sql = "select * from lunchRecord where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = DBConnectionUtil.getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
                return lunchRecord;
            } else {
                throw new NoSuchElementException();
            }
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public List<LunchRecord> findByRestaurantMenu(String restaurant, String menu) throws SQLException {
        String sql = "select * from LunchRecord where restaurant = ? and menu = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, restaurant);
            pstmt.setString(2, menu);
            rs = pstmt.executeQuery();

            List<LunchRecord> lunchRecordList = new ArrayList<>();

            while (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = DBConnectionUtil.getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());

                lunchRecordList.add(lunchRecord);
            }

//            if (lunchRecordList.isEmpty()) {
//                throw new NoSuchElementException();
//            }

            return lunchRecordList;
        } catch (SQLException e) {
            log.info("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(int id, String restaurant, String menu, Blob image, BigDecimal price, float grade) throws SQLException {
        String sql = "update lunchRecord set restaurant = ?, menu = ?, image = ?, price = ?, grade = ?, updateAt = ? where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, restaurant);
            pstmt.setString(2, menu);
            pstmt.setBinaryStream(3, image.getBinaryStream());
            pstmt.setBigDecimal(4, price);
            pstmt.setFloat(5, grade);
            pstmt.setTime(6, Time.valueOf(LocalTime.now()));
            pstmt.setInt(7, id);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void updateAverageGradeByRestaurantMenu(Float averageGrade, String restaurant, String menu) throws SQLException {
        String sql = "update lunchRecord set averageGrade = ?, updateAt = ? where restaurant = ? and menu = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setFloat(1, averageGrade);
            pstmt.setTime(2, Time.valueOf(LocalTime.now()));
            pstmt.setString(3, restaurant);
            pstmt.setString(4, menu);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void updateAverageGradeByRestaurantMenu(Connection con, Float averageGrade, String restaurant, String menu) throws SQLException {
        String sql = "update lunchRecord set averageGrade = ?, updateAt = ? where restaurant = ? and menu = ?";

        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setFloat(1, averageGrade);
            pstmt.setTime(2, Time.valueOf(LocalTime.now()));
            pstmt.setString(3, restaurant);
            pstmt.setString(4, menu);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            // Connection 은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "delete from lunchRecord where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "delete from lunchRecord";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error");
            throw e;
        } finally {
            close(con, pstmt, null);
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
