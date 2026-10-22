package cn.edu.mju.band.dal.impl;

import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.entity.Task;
import cn.edu.mju.band.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDal implements ITaskDal {
    @Override
    public List<Task> findAll() {
        List<Task> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "select id,title,status,priority,due_date,create_date from t_task";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setStatus(rs.getByte("status"));
                task.setPriority(rs.getString("priority"));
                task.setDueDate(rs.getDate("due_date"));
                task.setCreateDate(rs.getTimestamp("create_date"));
                list.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public Task findById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Task task = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "select id,title,status,priority,due_date,create_date from t_task where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setStatus(rs.getByte("status"));
                task.setPriority(rs.getString("priority"));
                task.setDueDate(rs.getDate("due_date"));
                task.setCreateDate(rs.getTimestamp("create_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return task;
    }

    @Override
    public int add(Task task) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rows = 0;
        try {
            conn = DBUtil.getConnection();
            String sql = "insert into t_task(title,status,priority,due_date) values(?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, task.getTitle());
            pstmt.setByte(2, task.getStatus());
            pstmt.setString(3, task.getPriority());
            pstmt.setDate(4, new java.sql.Date(task.getDueDate().getTime()));
            rows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return rows;
    }

    @Override
    public int update(Task task) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rows = 0;
        try {
            conn = DBUtil.getConnection();
            String sql = "update t_task set title=?,status=?,priority=?,due_date=? where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, task.getTitle());
            pstmt.setByte(2, task.getStatus());
            pstmt.setString(3, task.getPriority());
            pstmt.setDate(4, new java.sql.Date(task.getDueDate().getTime()));
            pstmt.setInt(5, task.getId());
            rows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return rows;
    }

    @Override
    public int deleteById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rows = 0;
        try {
            conn = DBUtil.getConnection();
            String sql = "delete from t_task where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return rows;
    }
}
