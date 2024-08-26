package org.harvey.batis.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-21 15:27
 */
@Data
@NoArgsConstructor
public class Employee {
    private Long id;
    private String firstName;

    public Employee(Long id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }
}