package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.TeacherRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherRoleDAO extends JpaRepository<TeacherRole, Integer> {

    @Query(value = """
            SELECT id_loai_vai_tro, ma_vai_tro, ten_vai_tro
            FROM teacher_role_types
            WHERE ma_vai_tro IN ('GVCN', 'GVBM')
            ORDER BY id_loai_vai_tro
            """, nativeQuery = true)
    List<Object[]> findRoleTypesForCreateForm();

    @Query(value = """
            SELECT id_loai_vai_tro, ma_vai_tro, ten_vai_tro
            FROM teacher_role_types
            WHERE ma_vai_tro IN (:roleCodes)
            """, nativeQuery = true)
    List<Object[]> findRoleTypesByCodes(@Param("roleCodes") List<String> roleCodes);

    List<TeacherRole> findByIdGiaoVienOrderByNamHocDescIdDesc(String idGiaoVien);

    List<TeacherRole> findByIdGiaoVienAndNamHocOrderByIdDesc(String idGiaoVien, String namHoc);

    @Modifying
    @Query(value = """
            DELETE FROM teacher_roles
            WHERE LOWER(id_giao_vien) = LOWER(:teacherId)
            """, nativeQuery = true)
    int deleteByTeacherId(@Param("teacherId") String teacherId);
}
