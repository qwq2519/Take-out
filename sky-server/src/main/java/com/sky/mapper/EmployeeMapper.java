package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("INSERT INTO employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    List<Employee> list(String name);

    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    @Select("SELECT * FROM employee WHERE id =#{id}")
    Employee getById(Long id);



    // 带条件更新密码（修正SQL语法和参数绑定）
    @Update("UPDATE employee SET password = #{newPassword} WHERE id = #{empId} AND password = #{oldPassword}")
    void updatePassword(PasswordEditDTO passwordEditDTO);

    // 复用查询逻辑（与getById保持一致性）
    @Select("SELECT * FROM employee WHERE id = #{id}")
    Employee selectById(Long id);


}
