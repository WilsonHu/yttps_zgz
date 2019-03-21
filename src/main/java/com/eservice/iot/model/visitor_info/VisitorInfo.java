package com.eservice.iot.model.visitor_info;

import java.util.Date;
import javax.persistence.*;

@Table(name = "visitor_info")
public class VisitorInfo {
    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证
     */
    @Column(name = "IdCard")
    private String idcard;

    /**
     * 公司名称
     */
    private String company;

    /**
     * 日期
     */
    @Column(name = "dateTime")
    private String datetime;

    /**
     * 是否来过，0、没来，1、来过
     */
    private Integer status;

    /**
     * 获取编号
     *
     * @return id - 编号
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置编号
     *
     * @param id 编号
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取姓名
     *
     * @return name - 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置姓名
     *
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取身份证
     *
     * @return IdCard - 身份证
     */
    public String getIdcard() {
        return idcard;
    }

    /**
     * 设置身份证
     *
     * @param idcard 身份证
     */
    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    /**
     * 获取公司名称
     *
     * @return company - 公司名称
     */
    public String getCompany() {
        return company;
    }

    /**
     * 设置公司名称
     *
     * @param company 公司名称
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * 获取日期
     *
     * @return dateTime - 日期
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     * 设置日期
     *
     * @param datetime 日期
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    /**
     * 获取是否来过，0、没来，1、来过
     *
     * @return status - 是否来过，0、没来，1、来过
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置是否来过，0、没来，1、来过
     *
     * @param status 是否来过，0、没来，1、来过
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}