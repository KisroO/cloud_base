package com.kisro.cloud.service;

import com.kisro.cloud.dao.StudentRepository;
import com.kisro.cloud.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public void insert(Student student) {
        studentRepository.save(student);
    }

    public void delete(Long id) {
        studentRepository.deleteById(id);
    }

    public void update(Student student) {
        studentRepository.save(student);
    }

    public Student findById(Long id) {
        Optional<Student> stu = studentRepository.findById(id);
        return stu.orElseGet(Student::new);
    }
}
