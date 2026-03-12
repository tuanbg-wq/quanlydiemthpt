package com.quanly.webdiem.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vai_tro")
    private Integer idVaiTro;

    @Column(name = "ten_vai_tro", nullable = false, unique = true, length = 50)
    private String tenVaiTro;

    @Column(name = "mo_ta")
    private String moTa;
}
