package com.atheboy.shiftpilot.repository;

import com.atheboy.shiftpilot.domain.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = {"skills.skill", "availabilityWindows"})
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = {"skills.skill", "availabilityWindows"})
    java.util.Optional<Employee> findById(Long id);
}
