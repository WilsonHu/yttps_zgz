package com.eservice.iot.core;


import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * 基于通用MyBatis Mapper插件的Service接口的实现
 */
public abstract class AbstractService<T> implements Service<T> {

    @Autowired
    protected Mapper<T> mapper;

    private Class<T> modelClass;    // 当前泛型真实类型的Class

    public AbstractService() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        modelClass = (Class<T>) pt.getActualTypeArguments()[0];
    }

    public boolean save(T model) {
        if(mapper.insertSelective(model)>0){
            return true;
        }else {
            return false;
        }
    }

    public boolean save(List<T> models) {
        if(mapper.insertList(models)>0){
            return true;
        }else {
            return false;
        }
    }

    public boolean deleteById(Integer id) {
        if(mapper.deleteByPrimaryKey(id)>0){
            return true;
        }else {
            return false;
        }
    }

    public boolean deleteByIds(String ids) {
        if(mapper.deleteByIds(ids)>0){
            return true;
        }else {
            return false;
        }
    }

    public boolean update(T model) {
        if(mapper.updateByPrimaryKeySelective(model)>0){
            return true;
        }else {
            return false;
        }
    }

    public T findById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public T findBy(String fieldName, Object value) throws TooManyResultsException {
        try {
            T model = modelClass.newInstance();
            Field field = modelClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(model, value);
            return mapper.selectOne(model);
        } catch (ReflectiveOperationException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<T> findByIds(String ids) {
        return mapper.selectByIds(ids);
    }

    public List<T> findByCondition(Condition condition) {
        return mapper.selectByCondition(condition);
    }

    public List<T> findAll() {
        return mapper.selectAll();
    }
}
