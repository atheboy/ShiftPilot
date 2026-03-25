package com.atheboy.shiftpilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_skills")
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    private int skillLevel;

    protected EmployeeSkill() {
    }

    public EmployeeSkill(Employee employee, Skill skill, int skillLevel) {
        this.employee = employee;
        this.skill = skill;
        this.skillLevel = skillLevel;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getSkillLevel() {
        return skillLevel;
    }
}
