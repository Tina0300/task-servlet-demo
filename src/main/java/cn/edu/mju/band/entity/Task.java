package cn.edu.mju.band.entity;

import java.util.Date;

public class Task {
    private Integer id;
    private String title;        // 对应 title
    private Byte status;         // 对应 status
    private String priority;     // 对应 priority (varchar)
    private Date dueDate;        // 对应 due_date
    private Date createDate;     // 对应 create_date

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
}
